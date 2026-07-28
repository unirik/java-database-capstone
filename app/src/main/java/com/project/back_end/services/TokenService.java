package com.project.back_end.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);
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
        byte[] keyBytes = this.jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String email, String role) {
        long expirationTimeInMs = 7L * 24 * 60 * 60 * 1000; // 7 días en milisegundos
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTimeInMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role) // Se añade el rol como propiedad adicional opcional de utilidad
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractEmail(String token) {
        try {
            // Limpiar prefijo Bearer si viene incluido en la cabecera HTTP
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (Exception e) {
            logger.warn("Fallo al intentar extraer el email del token JWT provisto: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token, String role) {
        try {
            // 1. Extraer y verificar la firma del token para obtener la identidad básica
            String emailOrUsername = extractEmail(token);
            if (emailOrUsername == null || emailOrUsername.isEmpty()) {
                return false;
            }

            // 2. Realizar búsquedas dirigidas según el rol solicitado
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

            // Si el rol provisto no pertenece a ninguna de las categorías conocidas
            logger.warn("Se intentó validar un token con un formato de rol desconocido: {}", role);
            return false;

        } catch (Exception e) {
            // Captura de forma segura cualquier excepción de expiración, firmas alteradas o nulos
            logger.error("Error imprevisto durante el proceso de validación del token de rol", e);
            return false;
        }
    }

}
