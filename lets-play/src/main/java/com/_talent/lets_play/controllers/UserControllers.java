package com._talent.lets_play.controllers;

import com._talent.lets_play.config.JwtUtils;
import com._talent.lets_play.models.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com._talent.lets_play.services.impl.UserService;
import lombok.RequiredArgsConstructor;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserControllers {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String pwd = loginRequest.getPassword();
            // 1. Création d'un objet Authentication avec les identifiants bruts
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()  // Le mot de passe non hashé
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Cette ligne lancera une UsernameNotFoundException si l'utilisateur n'existe pas
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            System.out.println("usert pwd: " + user.getPassword());
            String jwtToken = jwtUtils.generateJwtToken(user);
            // Si on arrive ici, c'est que l'utilisateur existe
            return ResponseEntity.ok(new JwtResponse(
                    jwtToken,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
            ));
        } catch (UsernameNotFoundException ex) {
            // On capture l'exception si l'utilisateur n'est pas trouvé
            return ResponseEntity.badRequest().body("Aucune correspondance !");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiants incorrects");
        }
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signupRequest) {
        // Vérifier si l'email existe déjà
        if (userService.getUserbyEmail(signupRequest.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Erreur: Cet email est déjà utilisé!");
        }
        // Créer un nouvel utilisateur
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setName(signupRequest.getUsername());
        // Utiliser le PasswordEncoder de Spring Security
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole("USER");

        // Sauvegarder l'utilisateur
        User savedUser = userService.addUser(user);

        // Retourner une réponse avec statut 201 (Created)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    @PostMapping("/admin")
    public String admin() {
        return "admin";
    }
}
