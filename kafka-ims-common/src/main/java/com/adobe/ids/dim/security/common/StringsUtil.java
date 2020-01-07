package com.adobe.ids.dim.security.common;

public class StringsUtil {

    public static boolean isNullOrEmpty(String s){
        if (s == null){
            return true;
        }
        if (s.isEmpty()){
            return true;
        }
        return false;
    }
}
