package net.m127.vpm.repo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication
@EnableWebSecurity
@EnableJpaRepositories
public class VpmRepoServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(VpmRepoServerApplication.class, args);
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic().realmName("VPM Repository Server");
        http.rememberMe().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.csrf().disable();
        http.authorizeHttpRequests()
            .mvcMatchers("/user").permitAll()
            .mvcMatchers(
                HttpMethod.GET,
                "/vpm/index.json",
                "/vpm/packages/{packageId}",
                "/vpm/packages/{packageId}/{packageVersion}.zip"
            ).permitAll()
            .mvcMatchers("/vpm/packages/{packageId}").authenticated()
            .mvcMatchers(HttpMethod.POST, "/vpm/packages").authenticated()
            .anyRequest().denyAll();
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
