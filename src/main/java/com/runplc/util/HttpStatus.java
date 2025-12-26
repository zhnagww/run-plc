package com.runplc.util;

/**
 * 返回状态码
 *
 */
public class HttpStatus
{
    /**
     * 操作成功
     */
    public static final int SUCCESS = 200;

    /**
     * 系统内部错误
     */
    public static final int ERROR = 500;

    /**
     *用户token失效，或者未授权情况
     */
    public static int UNAUTHORIZED = 401;

    /**
     *508表示前端需要将后台反传回的信息进行展示
     * 例如参数校验
     * 或者用户名重复返回等等
     */
    public static int PARAMINFO = 508;

    /**
     *资源未找到
     */
    public static int NOT_FOUND = 404;

}
