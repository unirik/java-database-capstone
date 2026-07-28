package com.project.back_end.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Service {
    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public GeneralService(TokenService tokenService, 
                          AdminRepository adminRepository, 
                          DoctorService doctorService, 
                          PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    public ResponseEntity<Object> validateToken(String token, String username) {
        boolean isValid = tokenService.validateToken(token, username);
        if (!isValid) {
            Map<String, String> errorBody = new HashMap<>();
            errorBody.put("error", "Unauthorized");
            errorBody.put("message", "El token provisto es inválido o ha expirado.");
            return new ResponseEntity<>(errorBody, HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<Object> validateAdmin(String username, String password) {
        try {
            Admin admin = adminRepository.findByUsername(username);
            if (admin == null) {
                return buildUnauthorizedResponse("El nombre de usuario de administrador no existe.");
            }

            if (!admin.getPassword().equals(password)) {
                return buildUnauthorizedResponse("La contraseña de administrador es incorrecta.");
            }

            String token = tokenService.generateToken(username, "ADMIN");
            Map<String, String> successBody = new HashMap<>();
            successBody.put("token", token);
            successBody.put("role", "ADMIN");

            return new ResponseEntity<>(successBody, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error crítico inesperado durante la autenticación del administrador", e);
            return buildInternalServerErrorResponse("Error interno del servidor durante la autenticación.");
        }
    }

    public List<Doctor> filterDoctor(String name, String specialty, String timePeriod) {
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasSpecialty = specialty != null && !specialty.trim().isEmpty();
        boolean hasTime = timePeriod != null && !timePeriod.trim().isEmpty();

        if (hasName && hasSpecialty && hasTime) {
            return doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, timePeriod);
        }

        if (hasName && hasTime && !hasSpecialty) {
            return doctorService.filterDoctorByNameAndTime(name, timePeriod);
        }

        if (hasName && hasSpecialty && !hasTime) {
            return doctorService.filterDoctorByNameAndSpecility(name, specialty);
        }

        if (hasTime && hasSpecialty && !hasName) {
            return doctorService.filterDoctorByTimeAndSpecility(timePeriod, specialty);
        }

        if (hasName) {
            return doctorService.findDoctorByName(name);
        }

        if (hasSpecialty) {
            return doctorService.filterDoctorBySpecility(specialty);
        }

        if (hasTime) {
            return doctorService.filterDoctorsByTime(timePeriod);
        }

        return doctorService.getDoctors();
    }

    public int validateAppointment(Long doctorId, LocalDateTime requestedTime) {
        boolean doctorExists = doctorService.getDoctors().stream()
                .anyMatch(d -> d.getId().equals(doctorId));
        
        if (!doctorExists) {
            return -1;
        }

        LocalDate date = requestedTime.toLocalDate();
        LocalTime requestedTimeOnly = requestedTime.toLocalTime();

        List<LocalTime> availableSlots = doctorService.getDoctorAvailability(doctorId, date);

        boolean isAvailable = availableSlots.contains(requestedTimeOnly);

        return isAvailable ? 1 : 0;
    }

    public boolean validatePatient(String email, String phone) {
        Patient existingPatient = patientService.getPatientDetails(tokenService.generateToken(email, "TEMP"));
        if (existingPatient != null) {
            return false;
        }
        return true;
    }

    public ResponseEntity<Object> validatePatientLogin(String email, String password) {
        try {
            String lookupToken = tokenService.generateToken(email, "PATIENT_LOOKUP");
            Patient patient = patientService.getPatientDetails(lookupToken);

            if (patient == null) {
                return buildUnauthorizedResponse("El correo electrónico no se encuentra registrado.");
            }

            if (!patient.getPassword().equals(password)) {
                return buildUnauthorizedResponse("La contraseña ingresada es incorrecta.");
            }

            String accessToken = tokenService.generateToken(email, "PATIENT");
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("token", accessToken);
            responseBody.put("role", "PATIENT");

            return new ResponseEntity<>(responseBody, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error crítico inesperado en el inicio de sesión del paciente para el correo: {}", email, e);
            return buildInternalServerErrorResponse("Ocurrió un error en el servidor al procesar el login de paciente.");
        }
    }

    public List<AppointmentDTO> filterPatient(String token, String condition, String doctorName) {
        try {
            // 1. Identificar de manera segura al paciente desde el JWT provisto
            Patient patient = patientService.getPatientDetails(token);
            if (patient == null) {
                return Collections.emptyList();
            }
            
            Long patientId = patient.getId();
            boolean hasCondition = condition != null && !condition.trim().isEmpty();
            boolean hasDoctor = doctorName != null && !doctorName.trim().isEmpty();

            // 2. Delegar la lógica de filtrado de acuerdo con las variables provistas
            if (hasCondition && hasDoctor) {
                return patientService.filterByDoctorAndCondition(patientId, doctorName, condition);
            }
            if (hasCondition) {
                return patientService.filterByCondition(patientId, condition);
            }
            if (hasDoctor) {
                return patientService.filterByDoctor(patientId, doctorName);
            } else {
                return patientService.getPatientAppointment(patientId);
            }
        } catch (Exception e) {
            logger.error("Error al procesar el filtrado de citas del paciente desde la interfaz general", e);
            return Collections.emptyList();
        }
    }

    private ResponseEntity buildUnauthorizedResponse(String message) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "Unauthorized");errorBody.put("message", message);
        return new ResponseEntity<>(errorBody, HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity buildInternalServerErrorResponse(String message) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("error", "Internal Server Error");
        errorBody.put("message", message);
        return new ResponseEntity<>(errorBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
