package net.m127.vpm.repo.service;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException() {
    }
    
    public UserAlreadyExistsException(String message) {
        super(message);
    }
    
    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UserAlreadyExistsException(Throwable cause) {
        super(cause);
    }
}
