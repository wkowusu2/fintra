package com.mobileApp.finTra.Controller;

import com.mobileApp.finTra.Entity.UserModel;
import com.mobileApp.finTra.Repository.UserRepository;
import com.mobileApp.finTra.SecurityConfi.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(
                loginRequest.email(), loginRequest.password());

        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        // Generate JWT token
        String token = jwtUtil.generateToken(loginRequest.email());
        System.out.println("token from sign in:"+token);
        return ResponseEntity.ok(token); // ✅ Token returned to client
    }

    public record LoginRequest(String email, String password) {}

        @PostMapping("/register")
        public ResponseEntity<String> register(@RequestBody RegisterRequest request) {

            if (userRepository.findByEmail(request.email()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already registered");
            }

            UserModel user = new UserModel();
            user.setEmail(request.email());
            user.setPassword(passwordEncoder.encode(request.password()));
            user.setAccount_type(request.account_type());
            user.setPhone(request.phone());
            user.setFirst_name(request.first_name());
            user.setLast_name(request.last_name());
            user.setMiddle_name(request.middle_name());
            user.setDob(request.dob());
            user.setCountry(request.country());


            userRepository.save(user);
            String token = jwtUtil.generateToken(user.getEmail());
            System.out.println("token from register: "+token);
            return ResponseEntity.ok(Map.of("token", token).toString());
        }

        public record RegisterRequest(String email, String password, String first_name, String last_name, String middle_name, String phone, String account_type, String country, LocalDate dob) {}
    }

