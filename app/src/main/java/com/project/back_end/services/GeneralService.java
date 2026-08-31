package com.project.back_end.services;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Appointment;
import com.project.back_end.DTO.Login;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.TokenService;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.DoctorRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeneralService {

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;

    public GeneralService(TokenService tokenService,
                          AdminRepository adminRepository, 
                          DoctorService doctorService, 
                          DoctorRepository doctorRepository,
                          PatientService patientService,
                          PatientRepository patientRepository) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorService = doctorService;
        this.doctorRepository = doctorRepository;
        this.patientService = patientService;
        this.patientRepository = patientRepository;
    }

    public ResponseEntity<Map<String, String>> validateToken(String token, String username) {
        Map<String, String> response = new HashMap<>();
        if (!tokenService.validateToken(token, username)) {
            response.put("error", "Invalid token");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    public ResponseEntity<Map<String, String>> validateAdmin(Admin administrator) {
        Map<String, String> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(administrator.getUsername());
            if (admin != null) {
                if (admin.getPassword().equals(administrator.getPassword())) {
                    response.put("token", tokenService.generateToken(admin.getUsername()));
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("error", "Wrong password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
            response.put("error", "Admin not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public Map<String, Object> filterDoctor(String name, String specialty, String timePeriod) {
        Map <String, Object> response = new HashMap<>();
        if (!name.equals("") && !specialty.equals("") && !timePeriod.equals("")) {
            response = doctorService.filterDoctorsByNameSpecilityandTime(name, specialty, timePeriod);
        } else if (!name.equals("null") && !timePeriod.equals("null")) {
            response = doctorService.filterDoctorByNameAndTime(name, timePeriod);
        } else if (!name.equals("null") && !specialty.equals("null")) {
            response = doctorService.filterDoctorByNameAndSpecility(name, specialty);
        } else if (!specialty.equals("null") && !timePeriod.equals("null")) {
            response = doctorService.filterDoctorByTimeAndSpecialty(specialty, timePeriod);
        } else if (!name.equals("null")) {
            response = doctorService.findDoctorByName(name);
        } else if (!specialty.equals("null")) {
            response = doctorService.filterDoctorBySpecility(specialty);
        } else if (!timePeriod.equals("null")) {
            response = doctorService.filterDoctorsByTime(timePeriod);
        } else {
            response.put("doctors", doctorService.getDoctors());
        }
        return response;
    }

    public int validateAppointment(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Optional<Doctor> result = doctorRepository.findById(doctor.getId());
        if (result.isEmpty()) return -1;
        LocalDateTime appointmentDate = appointment.getAppointmentTime();
        LocalTime appointmentTime = appointment.getAppointmentTimeOnly();
        List<String> availableTime = doctorService.getDoctorAvailability(doctor.getId(), appointmentDate);
        for (String timeSlot : availableTime) {
            String[] times = timeSlot.split("-");
            LocalTime startTime = LocalTime.parse(times[0]);
            if (appointmentTime.equals(startTime)) return -1;
        }
        return 0;
    }

    public boolean validatePatient(Patient patient) {
        Patient existingPatient = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        if (existingPatient != null) {
            return false;
        }
        return true;
    }

    public ResponseEntity<Map<String, String>> validatePatientLogin(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Patient result = patientRepository.findByEmail(login.getEmail());
            if (result != null) {
                if (result.getPassword().equals(login.getPassword())) {
                    response.put("token", tokenService.generateToken(login.getEmail()));
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("error", "Wrong password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
            response.put("error", "Invalid patient login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (java.lang.Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterPatient(String token, String condition, String doctorName) {
        String recoveredEmail = tokenService.extractEmail(token);
        Long patientId = patientRepository.findByEmail(recoveredEmail).getId();
        if (doctorName.equals("null") && !condition.equals("null")) {
            return patientService.filterByCondition(condition, patientId);
        } else if (condition.equals("null") && !doctorName.equals("null")) {
            return patientService.filterByDoctor(doctorName, patientId);
        } else if (!condition.equals("null") && !doctorName.equals("null")) {
            return patientService.filterByDoctorAndCondition(condition, doctorName, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}
