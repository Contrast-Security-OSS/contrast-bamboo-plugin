package com.contrastsecurity;


import org.apache.maven.model.Build;

public class BuildResult {

    private String buildKey;
    private int[] vulnsCount;
    public BuildResult(){

    }

    public BuildResult(String key){
        this.buildKey = key;
    }
    public int getNoteCount(){
        return vulnsCount[0];
    }
    public int getLowCount(){
        return vulnsCount[1];
    }
    public int getMediumCount(){
        return vulnsCount[2];
    }
    public int getHighCount(){
        return vulnsCount[3];
    }
    public int getCriticalCount(){
        return vulnsCount[4];
    }

    public String getBuildKey() {
        return buildKey;
    }

    public void setBuildKey(String buildKey) {
        this.buildKey = buildKey;
    }

    public int[] getVulnsCount() {
        return vulnsCount;
    }

    public void setVulnsCount(int[] vulnsCount) {
        this.vulnsCount = vulnsCount;
    }
}
