package com.zuzex.crudapplication.security.auth;

import com.zuzex.crudapplication.dto.UserRequestDto;
import com.zuzex.crudapplication.model.User;
import com.zuzex.crudapplication.repository.UserRepository;
import com.zuzex.crudapplication.security.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserRequestDto registrationUser) {
        if (userRepository.existsByName(registrationUser.name())) {
            throw new AuthenticationException("Посльзователь с именем " + registrationUser.name() + " уже существует",
                    HttpStatus.BAD_REQUEST);
        }
        var user = User.builder()
                .name(registrationUser.name())
                .age(registrationUser.age())
                .password(passwordEncoder.encode(registrationUser.password()))
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getName(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByName(request.getName())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
