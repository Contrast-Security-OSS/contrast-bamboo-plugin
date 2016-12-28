package com.contrastsecurity;


import net.java.ao.Entity;
import org.apache.maven.model.Build;

import java.io.Serializable;
import java.util.ArrayList;

public interface BuildResults extends Entity {

    public void setFindings(ArrayList<Finding> findings);
    public void setBuildId(String buildId);
    public void getFindings();
    public void getBuildId();

}
