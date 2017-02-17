package com.contrastsecurity.util;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyGenerator {

    public static String generate(String c){
        String re1="((?:[a-z][a-z0-9_]*))(-)((?:[a-z][a-z0-9_]*))(-)";
        Pattern p = Pattern.compile(re1,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(c);
        if(m.find()){
            return m.group(1) + m.group(2) + m.group(3) + m.group(4);
        }
        return c;
    }


}
