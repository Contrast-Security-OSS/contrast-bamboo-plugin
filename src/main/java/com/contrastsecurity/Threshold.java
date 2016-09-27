package com.contrastsecurity;


public class Threshold {

    private int count;
    private String severity_select;
    private String type_select;

    public Threshold(int count, String severity, String type){
        this.count = count;
        this.severity_select = severity;
        this.type_select = type;
    }

    public String getSeverity_select(){
        return severity_select;
    }

    public String getType_select(){
        return type_select;
    }

    public int getCount(){
        return count;
    }
}
