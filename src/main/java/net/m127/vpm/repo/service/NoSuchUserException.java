package net.m127.vpm.repo.service;

public class NoSuchUserException extends Exception {
    public NoSuchUserException() {
    }
    
    public NoSuchUserException(String message) {
        super(message);
    }
    
    public NoSuchUserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NoSuchUserException(Throwable cause) {
        super(cause);
    }
}
