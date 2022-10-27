package net.m127.vpm.repo.service;

public class EmailTakenException extends UserAlreadyExistsException {
    public EmailTakenException() {
    }
    
    public EmailTakenException(String message) {
        super(message);
    }
    
    public EmailTakenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public EmailTakenException(Throwable cause) {
        super(cause);
    }
}
