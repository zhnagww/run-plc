package com.runplc.runner;

import com.runplc.util.DatabaseInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

@Component
public class StartupRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupRunner.class);

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("run-plc服务已启动成功~");
        // 初始化数据库表
        databaseInitializer.initializeTables();
        
        // 启动浏览器打开页面
        openBrowser("http://localhost:8080");
    }
    
    private void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI.create(url));
                }
            }else {
                openBrowserWindows(url);
            }
        } catch (Exception e) {
            logger.warn("无法自动打开浏览器: {}", e.getMessage());
        }
    }
    private void openBrowserWindows(String url) throws IOException {
        // 方法1：使用 start 命令（最常用）
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
            return;
        } catch (IOException e1) {
            System.err.println("start 命令失败，尝试其他方法");
        }

        // 方法2：使用 rundll32 命令（传统方法）
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            return;
        } catch (IOException e2) {
            System.err.println("rundll32 命令失败");
        }

        // 方法3：尝试直接启动常见浏览器
        try {
            String[] browsers = {
                    "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe",
                    "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe",
                    "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe",
                    "C:\\Program Files\\Mozilla Firefox\\firefox.exe",
                    "C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe"
            };

            for (String browser : browsers) {
                try {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                    return;
                } catch (IOException ignored) {
                    // 继续尝试下一个浏览器
                }
            }
        } catch (Exception e3) {
            System.err.println("直接启动浏览器失败");
        }

        // 方法4：使用 PowerShell
        try {
            Runtime.getRuntime().exec(new String[]{"powershell", "-Command", "Start-Process '" + url + "'"});
        } catch (IOException e4) {
            System.err.println("PowerShell 命令也失败了");
        }
    }
}