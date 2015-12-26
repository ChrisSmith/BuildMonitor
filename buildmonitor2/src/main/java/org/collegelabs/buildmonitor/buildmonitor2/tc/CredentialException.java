package org.collegelabs.buildmonitor.buildmonitor2.tc;

import java.security.GeneralSecurityException;

/**
 */
public class CredentialException extends Exception {
    public CredentialException(String message, Exception e) {
        super(message, e);
    }

    public CredentialException(String message) {
        super(message);
    }
}
