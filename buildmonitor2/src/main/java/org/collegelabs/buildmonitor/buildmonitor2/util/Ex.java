package org.collegelabs.buildmonitor.buildmonitor2.util;

public class Ex {

    public static int tryParse(String str){
        return tryParse(str, 0);
    }

    public static int tryParse(String str, int defaultValue){
        try {
            return Integer.parseInt(str);
        }catch (NumberFormatException e){
            return defaultValue;
        }
    }
}
