package net.m127.vpm.repo.service;

import net.m127.vpm.repo.jpa.entity.User;
import net.m127.vpm.repo.jwt.TokenResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public interface UserService {
    Supplier<NoSuchUserException> noSuchUser(String username);
    Optional<User> getCurrentUser(String token);
    Optional<User> getUser(String username);
    List<User> getUsers();
    TokenResponse createUser(String token, String username, String email, String password)
        throws UsernameTakenException, EmailTakenException, IllegalAccessException;
    TokenResponse loginUser(String username, String password)
        throws NoSuchUserException, IllegalAccessException;
    boolean validateUser(String username) throws NoSuchUserException;
    boolean approveUser(String token, String username) throws NoSuchUserException, IllegalAccessException;
    boolean makeAdmin(String token, String username) throws NoSuchUserException, IllegalAccessException;
}
