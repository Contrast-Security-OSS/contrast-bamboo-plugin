package com.contrastsecurity;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyGenerator {

    final static Pattern lastIntPattern = Pattern.compile("[^0-9]+([0-9]+)$");

    public static String generate(String c){
        String re1="((?:[a-z][a-z0-9_]*))(-)((?:[a-z][a-z0-9_]*))(-)";
        Pattern p = Pattern.compile(re1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(c);
        if(m.find()){
            return m.group(1) + m.group(2) + m.group(3) + m.group(4);
        }
        return c;
    }

    public static int retrieveId(String input){
        Matcher matcher = lastIntPattern.matcher(input);
        int id = 0;
        if (matcher.find()) {
            String foundResult = matcher.group(1);
            return Integer.parseInt(foundResult);
        }
        return id;
    }
}
