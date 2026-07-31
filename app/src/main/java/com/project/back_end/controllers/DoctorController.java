package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import com.project.back_end.DTO.Login;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {
    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAvailability(@PathVariable String user, @PathVariable Long doctorId, @PathVariable String date, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, user)) {
            response.put("success", false);
            response.put("message", "Invalid Token or expired for user: " + user);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var availability = doctorService.getDoctorAvailability(doctorId, date);
        response.put("success", true);
        response.put("availability", availability);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctor() {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.getAllDoctors();
        
        response.put("doctors", doctors);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "admin")) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (doctorService.exists(doctor)) {
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
        Map<String, Object> loginResult = doctorService.login(login);

        if ((boolean) loginResult.get("authenticated")) {
            response.put("success", true);
            response.put("token", loginResult.get("token"));
            response.put("message", "Login exitoso.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Credenciales incorrectas.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(@RequestBody Doctor doctor, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "admin")) {
            response.put("success", false);
            response.put("message", "Not authorized to perform that action.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean isUpdated = doctorService.updateDoctor(doctor);

        if (isUpdated) {
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

        if (!generalService.validateToken(token, "admin")) {
            response.put("success", false);
            response.put("message", "Not authorized for deleting logs.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean isDeleted = doctorService.deleteDoctorById(id);

        if (isDeleted) {
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

        List<Doctor> filteredDoctors = generalService.filterDoctors(name, time, speciality);

        response.put("success", true);
        response.put("doctors", filteredDoctors);
        return ResponseEntity.ok(response);
    }
}
