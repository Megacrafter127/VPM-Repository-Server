package net.m127.vpm.repo.service;

public class UsernameTakenException extends UserAlreadyExistsException {
    public UsernameTakenException() {
    }
    
    public UsernameTakenException(String message) {
        super(message);
    }
    
    public UsernameTakenException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public UsernameTakenException(Throwable cause) {
        super(cause);
    }
}
