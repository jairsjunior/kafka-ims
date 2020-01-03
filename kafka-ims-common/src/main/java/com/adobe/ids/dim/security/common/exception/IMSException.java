package com.adobe.ids.dim.security.common.exception;

public class IMSException extends Exception {

    public IMSException(int errCode, String message) {
        super("Error Code: " + String.valueOf(errCode) +", Error Message: " + message);
    }

}
