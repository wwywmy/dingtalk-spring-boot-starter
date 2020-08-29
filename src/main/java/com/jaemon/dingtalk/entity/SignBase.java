package com.jaemon.dingtalk.entity;

/**
 * 签名返回体基础类
 *
 * @author Jaemon@answer_ljm@163.com
 * @version 1.0
 */
public abstract class SignBase {
    protected final static String SEPERATOR = "&";

    /**
     * 签名对象转字符串
     *
     * @return 返回转换后结果
     */
    public abstract String transfer();
}