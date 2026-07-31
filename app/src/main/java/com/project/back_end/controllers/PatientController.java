package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.Service;
import com.project.back_end.services.PatientService;
import com.project.back_end.DTO.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "patient")) {
            response.put("success", false);
            response.put("message", "Invalid Token or expired. Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var patientInfo = patientService.getPatientDetailsByToken(token);
        response.put("success", true);
        response.put("patient", patientInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Patient patient) {
        Map<String, Object> response = new HashMap<>();

        if (generalService.checkPatientExists(patient)) {
            response.put("success", false);
            response.put("message", "The patient is already registered.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        boolean isCreated = patientService.createPatient(patient);

        if (isCreated) {
            response.put("success", true);
            response.put("message", "Patient successfully registered.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("success", false);
            response.put("message", "An Error ocurred while processing the record.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        Map<String, Object> response = new HashMap<>();

        Map<String, Object> loginResult = service.validatePatientLogin(login);

        if ((boolean) loginResult.get("success")) {
            response.put("success", true);
            response.put("token", loginResult.get("token"));
            response.put("message", "Login success.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/appointments/{patientId}/{token}/{userRole}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long patientId, @PathVariable String token, @PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, userRole)) {
            response.put("success", false);
            response.put("message", "Validation Error: token or rol not authorized.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var appointments = patientService.getAppointmentsByPatientId(patientId);
        response.put("success", true);
        response.put("appointments", appointments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "patient")) {
            response.put("success", false);
            response.put("message", "Invalid Token inválido.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var filteredAppointments = generalService.filterPatientAppointments(condition, name);
        response.put("success", true);
        response.put("appointments", filteredAppointments);
        return ResponseEntity.ok(response);
    }
}


