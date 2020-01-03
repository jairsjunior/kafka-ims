package com.adobe.ids.dim.security.common.exception;

import org.apache.kafka.common.errors.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IMSValidatorException extends AuthorizationException {

    private final Logger log = LoggerFactory.getLogger(IMSValidatorException.class);
    public static String KAFKA_EXCEPTION_WITHOUT_SCOPE_MSG = "Client without the required scope";

    public IMSValidatorException(String message) {
        super(message);
        log.error("IMS Validator Exception: " + message);
    }

}
