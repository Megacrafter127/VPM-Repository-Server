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

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    
    private final PasswordEncoder encoder;
    
    private final TokenManager tokenManager;
    
    @Value("${permission.account.creation}")
    protected AccountCreation accountCreationPermission;
    
    @Override
    public TokenResponse createUser(String token, String username, String email, String password)
        throws UsernameTakenException, EmailTakenException, IllegalAccessException {
        if(accountCreationPermission != AccountCreation.ANY) {
            if(token == null) throw new IllegalAccessException();
            String creator = tokenManager.getUsernameFromValidToken(token);
            if(creator == null) throw new IllegalAccessException();
            if(!users.existsByName(creator)) throw new IllegalAccessException();
            if(accountCreationPermission == AccountCreation.ADMIN) throw new IllegalAccessException();
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
                null
            )).getName());
    }
    
    @Override
    public TokenResponse loginUser(String username, String password) throws NoSuchUserException, IllegalAccessException {
        if(!users.existsByName(username)) throw new NoSuchUserException("User does not exist");
        User user = users.findByName(username);
        if(!encoder.matches(password, user.getPassword())) throw new IllegalAccessException("Incorrect password");
        return tokenManager.generateJwtToken(username);
    }
}
