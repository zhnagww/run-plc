package com.runplc.controller;

import com.runplc.entity.PlcAddr;
import com.runplc.entity.PlcInfo;
import com.runplc.service.PlcAddrService;
import com.runplc.service.PlcInfoService;
import com.runplc.util.AjaxResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/plc-import")
@Api(tags = "PLC导入接口")
public class PlcImportController {

    @Autowired
    private PlcInfoService plcInfoService;

    @Autowired
    private PlcAddrService plcAddrService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    @PostMapping("/excel")
    @ApiOperation("上传Excel导入PLC与点位")
    public AjaxResult importExcel(
            @RequestParam("file") @ApiParam("Excel文件(.xlsx)") MultipartFile file,
            @RequestParam(value = "strict", required = false, defaultValue = "false") @ApiParam("严格模式：有错误则回滚") boolean strict
    ) {
        if (file == null || file.isEmpty()) {
            return AjaxResult.error(508, "文件不能为空");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            return AjaxResult.error(508, "仅支持.xlsx格式");
        }

        TransactionTemplate tt = new TransactionTemplate(transactionManager);
        ImportReport report = tt.execute(status -> {
            ImportReport r;
            try {
                r = doImport(file);
            } catch (Exception e) {
                r = new ImportReport();
                r.errors.add(new ImportError(0, "解析失败：" + (e.getMessage() == null ? "" : e.getMessage())));
            }

            if (strict && !r.errors.isEmpty()) {
                status.setRollbackOnly();
            }
            return r;
        });

        if (report == null) {
            return AjaxResult.error(508, "导入失败");
        }

        if (strict && !report.errors.isEmpty()) {
            return new AjaxResult(508, "导入失败（严格模式已回滚）", report);
        }

        return AjaxResult.success("导入完成", report);
    }

    private ImportReport doImport(MultipartFile file) throws Exception {
        ImportReport report = new ImportReport();

        Map<String, PlcInfo> plcCache = new HashMap<>();
        DataFormatter fmt = new DataFormatter();

        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) {
                report.errors.add(new ImportError(0, "Excel没有Sheet"));
                return report;
            }

            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) {
                report.errors.add(new ImportError(0, "缺少表头"));
                return report;
            }

            Map<String, Integer> col = parseHeader(header, fmt);
            Integer ipIdx = first(col, "ip", "ip_addr", "ipaddr", "ip地址", "ip\u5730\u5740");
            Integer portIdx = first(col, "port", "port_no", "portno", "端口", "端口号");
            Integer plcNoIdx = first(col, "plc_no", "plcno", "点位", "点位号", "plc点位");
            Integer nameIdx = first(col, "name", "名称");
            Integer typeIdx = first(col, "type", "类型");

            if (ipIdx == null || portIdx == null || plcNoIdx == null || nameIdx == null || typeIdx == null) {
                report.errors.add(new ImportError(0, "表头必须包含：ip、port、plc_no、name、type"));
                return report;
            }

            int firstRow = sheet.getFirstRowNum() + 1;
            int lastRow = sheet.getLastRowNum();
            for (int r = firstRow; r <= lastRow; r++) {
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }

                String ip = cellString(row, ipIdx, fmt);
                String portStr = cellString(row, portIdx, fmt);
                String plcNoStr = cellString(row, plcNoIdx, fmt);
                String name = cellString(row, nameIdx, fmt);
                String typeStr = cellString(row, typeIdx, fmt);

                boolean allBlank = isBlank(ip) && isBlank(portStr) && isBlank(plcNoStr) && isBlank(name) && isBlank(typeStr);
                if (allBlank) {
                    continue;
                }

                int excelRowNum = r + 1;

                if (isBlank(ip) || !isValidIp(ip)) {
                    report.errors.add(new ImportError(excelRowNum, "IP地址格式不正确"));
                    continue;
                }

                Integer port;
                if (isBlank(portStr)) {
                    port = 502;
                } else {
                    try {
                        port = Integer.parseInt(portStr.trim());
                    } catch (Exception e) {
                        report.errors.add(new ImportError(excelRowNum, "端口号不合法"));
                        continue;
                    }
                }

                Integer plcNo;
                try {
                    plcNo = Integer.parseInt(plcNoStr.trim());
                } catch (Exception e) {
                    report.errors.add(new ImportError(excelRowNum, "PLC点位不合法"));
                    continue;
                }

                if (isBlank(name)) {
                    report.errors.add(new ImportError(excelRowNum, "名称不能为空"));
                    continue;
                }

                Integer type = parseType(typeStr);
                if (type == null) {
                    report.errors.add(new ImportError(excelRowNum, "类型不合法（写入/读取 或 1/2）"));
                    continue;
                }

                String plcKey = ip.trim() + ":" + port;
                PlcInfo plcInfo = plcCache.get(plcKey);
                if (plcInfo == null) {
                    plcInfo = plcInfoService.getByIpAddrAndPortNo(ip.trim(), port);
                    if (plcInfo == null) {
                        PlcInfo toCreate = new PlcInfo();
                        toCreate.setIpAddr(ip.trim());
                        toCreate.setPortNo(port);
                        try {
                            boolean ok = plcInfoService.save(toCreate);
                            if (ok && toCreate.getId() != null) {
                                report.plcInserted++;
                                plcInfo = toCreate;
                            } else {
                                report.errors.add(new ImportError(excelRowNum, "PLC创建失败"));
                                continue;
                            }
                        } catch (Exception e) {
                            PlcInfo existed = plcInfoService.getByIpAddrAndPortNo(ip.trim(), port);
                            if (existed != null) {
                                report.plcSkipped++;
                                plcInfo = existed;
                            } else {
                                report.errors.add(new ImportError(excelRowNum, "PLC创建失败"));
                                continue;
                            }
                        }
                    } else {
                        report.plcSkipped++;
                    }
                    plcCache.put(plcKey, plcInfo);
                }

                if (plcInfo.getId() == null) {
                    report.errors.add(new ImportError(excelRowNum, "PLC信息缺少ID"));
                    continue;
                }

                PlcAddr existedAddr = plcAddrService.getByPlcInfoIdAndPlcNo(plcInfo.getId(), plcNo);
                if (existedAddr != null) {
                    report.addrSkipped++;
                    continue;
                }

                PlcAddr addr = new PlcAddr();
                addr.setPlcInfoId(plcInfo.getId());
                addr.setPlcNo(plcNo);
                addr.setName(name.trim());
                addr.setType(type);

                try {
                    boolean ok = plcAddrService.save(addr);
                    if (ok) {
                        report.addrInserted++;
                    } else {
                        report.errors.add(new ImportError(excelRowNum, "点位创建失败"));
                    }
                } catch (Exception e) {
                    PlcAddr existed2 = plcAddrService.getByPlcInfoIdAndPlcNo(plcInfo.getId(), plcNo);
                    if (existed2 != null) {
                        report.addrSkipped++;
                    } else {
                        report.errors.add(new ImportError(excelRowNum, "点位创建失败"));
                    }
                }
            }
        }

        return report;
    }

    private Map<String, Integer> parseHeader(Row header, DataFormatter fmt) {
        Map<String, Integer> col = new HashMap<>();
        short last = header.getLastCellNum();
        for (int i = 0; i < last; i++) {
            Cell c = header.getCell(i);
            if (c == null) {
                continue;
            }
            String v = fmt.formatCellValue(c);
            if (v == null) {
                continue;
            }
            String key = v.trim().toLowerCase();
            if (!key.isEmpty()) {
                col.put(key, i);
            }
        }
        return col;
    }

    private Integer first(Map<String, Integer> col, String... keys) {
        for (String k : keys) {
            Integer idx = col.get(k.toLowerCase());
            if (idx != null) {
                return idx;
            }
        }
        return null;
    }

    private String cellString(Row row, int idx, DataFormatter fmt) {
        Cell c = row.getCell(idx);
        if (c == null) {
            return "";
        }
        String v = fmt.formatCellValue(c);
        return v == null ? "" : v.trim();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isValidIp(String ip) {
        return ip != null && IP_PATTERN.matcher(ip.trim()).matches();
    }

    private Integer parseType(String typeStr) {
        if (typeStr == null) {
            return null;
        }
        String t = typeStr.trim();
        if (t.isEmpty()) {
            return null;
        }
        if ("1".equals(t) || t.contains("写")) {
            return 1;
        }
        if ("2".equals(t) || t.contains("读")) {
            return 2;
        }
        return null;
    }

    public static class ImportReport {
        public int plcInserted;
        public int plcSkipped;
        public int addrInserted;
        public int addrSkipped;
        public List<ImportError> errors = new ArrayList<>();
    }

    public static class ImportError {
        public int row;
        public String message;

        public ImportError(int row, String message) {
            this.row = row;
            this.message = message;
        }
    }
}
