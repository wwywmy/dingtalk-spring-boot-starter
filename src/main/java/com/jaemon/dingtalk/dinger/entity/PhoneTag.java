package com.jaemon.dingtalk.dinger.entity;

import lombok.Setter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * PhoneTag
 *
 * @author Jaemon@answer_ljm@163.com
 * @version 2.0
 */
@XmlRootElement(name = "phone")
@Setter
public class PhoneTag {
    private String value;

    @XmlAttribute(required = true, name = "value")
    public String getValue() {
        return value;
    }
}