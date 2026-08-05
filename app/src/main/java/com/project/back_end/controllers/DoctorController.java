package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.GeneralService;
import com.project.back_end.DTO.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalTime;
import java.time.LocalDate;
java.time.format.DateTimeParseException

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final GeneralService service;

    public DoctorController(DoctorService doctorService, GeneralService service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId, @PathVariable String date, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, user).getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            LocalDate localDate = LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Invalid date format. Please use YYYY-MM-DD.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
            List<LocalTime> availability = doctorService.getDoctorAvailability(doctorId, localDate);
            response.put("success", true);
            response.put("availability", availability);
            return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getDoctors();
        
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "admin").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Doctor> doctors = doctorService.findDoctorByName(doctor.getName());

        if (doctors.size() > 0) {
            response.put("success", false);
            response.put("message", "The doctor is already logged.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        doctorService.saveDoctor(doctor);
        response.put("success", true);
        response.put("message", "Doctor logged successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> doctorLogin(@RequestBody Login login) {
        Map<String, Object> response = new HashMap<>();
        
        // Delegar autenticación al servicio de doctores
        String loginResult = doctorService.validateDoctor(login.getEmail(), login.getPassword());

        if (Boolean.parseBoolean(loginResult)) {
            response.put("success", true);
            response.put("message", "Login exitoso.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid Credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "admin").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int isUpdated = doctorService.updateDoctor(doctor);

        if (isUpdated == 1) {
            response.put("success", true);
            response.put("message", "Doctor information updated successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Doctor not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(@PathVariable Long id, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "admin").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int isDeleted = doctorService.deleteDoctor(id);

        if (isDeleted == 1) {
            response.put("success", true);
            response.put("message", "Doctor deleted successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Doctor Not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public ResponseEntity<Map<String, Object>> filter(@PathVariable String name, @PathVariable String time, @PathVariable String speciality) {
        Map<String, Object> response = new HashMap<>();

        List<Doctor> filteredDoctors = service.filterDoctor(name, speciality, time);

        response.put("success", true);
        response.put("doctors", filteredDoctors);
        return ResponseEntity.ok(response);
    }
}
