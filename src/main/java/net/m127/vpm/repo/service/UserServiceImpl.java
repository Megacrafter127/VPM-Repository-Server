package net.m127.vpm.repo.service;

import lombok.RequiredArgsConstructor;
import net.m127.vpm.repo.jpa.UserRepository;
import net.m127.vpm.repo.jpa.entity.User;
import net.m127.vpm.repo.jwt.TokenManager;
import net.m127.vpm.repo.jwt.TokenResponse;
import net.m127.vpm.repo.permission.AccountCreation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    
    private final PasswordEncoder encoder;
    
    private final TokenManager tokenManager;
    
    @Value("${permission.account.creation}")
    protected AccountCreation accountCreationPermission;
    
    public Supplier<NoSuchUserException> noSuchUser(String username) {
        return () -> new NoSuchUserException(String.format("User '%s' does not exist.", username));
    }
    
    @Override
    public Optional<User> getCurrentUser(String token) {
        return Optional.ofNullable(token)
                       .map(tokenManager::getUsernameFromValidToken)
                       .flatMap(users::findByName);
    }
    
    @Override
    public Optional<User> getUser(String username) {
        return users.findByName(username);
    }
    
    @Override
    public List<User> getUsers() {
        return users.findAll();
    }
    
    @Override
    public TokenResponse createUser(String token, String username, String email, String password)
        throws UsernameTakenException, EmailTakenException, IllegalAccessException {
        Optional<User> creator = getCurrentUser(token);
        switch (accountCreationPermission) {
        case ANY:
            break;
        case ADMIN:
            creator = creator.filter(User::isAdmin);
        case USER:
            creator.orElseThrow(IllegalAccessException::new);
            break;
        }
        if(users.existsByName(username)) throw new UsernameTakenException();
        if(users.existsByEmail(email)) throw new EmailTakenException();
        return tokenManager.generateJwtToken(
            users.save(new User(
                null,
                username,
                null,
                email,
                encoder.encode(password),
                false,
                creator.orElse(null),
                false,
                null
            )).getName());
    }
    
    
    
    @Override
    public TokenResponse loginUser(String username, String password) throws NoSuchUserException, IllegalAccessException {
        User user = users.findByName(username).orElseThrow(noSuchUser(username));
        if(!encoder.matches(password, user.getPassword())) throw new IllegalAccessException("Incorrect password");
        return tokenManager.generateJwtToken(username);
    }
    
    @Override
    public boolean validateUser(String username) throws NoSuchUserException {
        User user = users.findByName(username).orElseThrow(noSuchUser(username));
        if(user.isValidated()) return false;
        user.setValidated(true);
        users.save(user);
        return true;
    }
    
    @Override
    public boolean approveUser(String token, String username) throws NoSuchUserException, IllegalAccessException {
        if(token == null) throw new IllegalAccessException("Not authenticated");
        String adminName = tokenManager.getUsernameFromValidToken(token);
        if(adminName == null) throw new IllegalAccessException("Invalid token");
        User admin = users.findByName(adminName).orElseThrow(noSuchUser(adminName));
        if(!admin.isAdmin()) throw new IllegalAccessException("Not admin");
        User user = users.findByName(username).orElseThrow(noSuchUser(username));
        if(user.isApproved()) return false;
        user.setApprover(admin);
        users.save(user);
        return true;
    }
    
    @Override
    public boolean makeAdmin(String token, String username) throws NoSuchUserException, IllegalAccessException {
        if(token == null) throw new IllegalAccessException("Not authenticated");
        String adminName = tokenManager.getUsernameFromValidToken(token);
        if(adminName == null) throw new IllegalAccessException("Invalid token");
        User admin = users.findByName(adminName).orElseThrow(noSuchUser(adminName));
        if(!admin.isAdmin()) throw new IllegalAccessException("Not admin");
        User user = users.findByName(username).orElseThrow(noSuchUser(username));
        if(user.isAdmin()) return false;
        user.setAdmin(true);
        users.save(user);
        return true;
    }
}
