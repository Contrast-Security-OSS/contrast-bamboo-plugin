package com.contrastsecurity;

import net.java.ao.Entity;
import net.java.ao.OneToMany;
import net.java.ao.Preload;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by donaldpropst on 12/30/16.
 */

public class BuildResults{

    private ArrayList<Finding> findings;
    private String buildId;
    private int noteCount;
    private int lowCount;
    private int mediumCount;
    private int highCount;
    private int criticalCount;

    public BuildResults(String buildId, Finding f){
        this.findings = new ArrayList<Finding>();
        this.buildId = buildId;
        noteCount = 0;
        lowCount = 0;
        highCount = 0;
        mediumCount = 0;
        criticalCount = 0;
        addFinding(f);

    }
    public void setBuildId(String buildId){
        this.buildId = buildId;
    }

    public ArrayList<Finding> getFindings() {
        return findings;
    }
    public void addFinding(Finding f){
        findings.add(f);
        String severity = f.getSeverity();
        if(severity.equals("Note")){
            noteCount++;
        }else if(severity.equals("Low")){
            lowCount++;
        }else if(severity.equals("Medium")){
            mediumCount++;
        }else if(severity.equals("High")){
            highCount++;
        }else {
            criticalCount++;
        }
    }

    public void setFindings(ArrayList<Finding> findings) {
        this.findings = findings;
    }

    public String getBuildId() {
        return buildId;
    }

    public int getNoteCount() {
        return noteCount;
    }

    public int getLowCount() {
        return lowCount;
    }

    public int getMediumCount() {
        return mediumCount;
    }

    public int getHighCount() {
        return highCount;
    }

    public int getCriticalCount() {
        return criticalCount;
    }
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"buildId\":\"" + simplifyBuildId()+"\",");
        builder.append("\"lowCount\":" + lowCount+",");
        builder.append("\"mediumCount\":" + mediumCount+",");
        builder.append("\"highCount\":" + highCount+",");
        builder.append("\"noteCount\":" + noteCount+",");
        builder.append("\"criticalCount\":" + criticalCount+",");
        builder.append("\"totalCount\":" + getTotal()+",");
        builder.append("\"findings\":" + "["+getFindingsJson() +"]}");

        return builder.toString();

    }

    private String getFindingsJson(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < findings.size(); i++){
            Finding f = findings.get(i);
            builder.append("{\"type\":\""+f.getType()+"\",\"severity\":\""+f.getSeverity() +"\"}");
            if(i < findings.size()-1){
                builder.append(",");
            }

        }
        return builder.toString();
    }
    private String simplifyBuildId(){
        String re1="(com\\.contrastsecurity\\.bambooplugin)(:)((?:[a-z][a-z0-9_]*))(-)((?:[a-z][a-z0-9_]*))(-)(\\d+)";
        Pattern p = Pattern.compile(re1, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(buildId);
        if (m.find()) {
           return m.group(7);
        }
        return "0";
    }
    private int getTotal(){
        return noteCount + lowCount + highCount + mediumCount + criticalCount;
    }

}
