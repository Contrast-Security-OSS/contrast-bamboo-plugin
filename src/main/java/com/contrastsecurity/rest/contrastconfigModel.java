package com.contrastsecurity.rest;

import javax.xml.bind.annotation.*;
@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class contrastconfigModel {

    @XmlElement(name = "value")
    private String message;

    public contrastconfigModel() {
    }

    public contrastconfigModel(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}