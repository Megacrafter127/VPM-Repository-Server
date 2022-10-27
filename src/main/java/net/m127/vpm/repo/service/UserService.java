package net.m127.vpm.repo.service;

import net.m127.vpm.repo.jwt.TokenResponse;

public interface UserService {
    TokenResponse createUser(String token, String username, String email, String password)
        throws UsernameTakenException, EmailTakenException, IllegalAccessException;
    
    TokenResponse loginUser(String username, String password)
        throws NoSuchUserException, IllegalAccessException;
}
