package com.contrastsecurity.model;


import net.java.ao.Entity;
import net.java.ao.Preload;

@Preload
public interface Finding extends Entity {

    public void setSeverity(String severity);
    public void setType(String type);
    public void setBuildId(String buildId);
    public String getSeverity();
    public String getType();
    public String getBuildId();

}
