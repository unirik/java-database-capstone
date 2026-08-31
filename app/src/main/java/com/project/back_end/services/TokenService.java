package com.project.back_end.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class TokenService {
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public TokenService(AdminRepository adminRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7))
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token, String role) {
        try {
            String emailOrUsername = extractEmail(token);
            if (emailOrUsername == null || emailOrUsername.isEmpty()) {
                return false;
            }

            if ("admin".equalsIgnoreCase(role)) {
                Admin admin = adminRepository.findByUsername(emailOrUsername);
                return admin != null;
            } 
            
            if ("doctor".equalsIgnoreCase(role)) {
                Doctor doctor = doctorRepository.findByEmail(emailOrUsername);
                return doctor != null;
            } 
            
            if ("patient".equalsIgnoreCase(role)) {
                Patient patient = patientRepository.findByEmail(emailOrUsername);
                return patient != null;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
