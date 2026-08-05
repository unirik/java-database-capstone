package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.models.Patient;
import com.project.back_end.services.GeneralService;
import com.project.back_end.services.PatientService;
import com.project.back_end.DTO.Login;
import com.project.back_end.DTO.AppointmentDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final GeneralService service;

    public PatientController(PatientService patientService, GeneralService service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "patient").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Patient patientInfo = patientService.getPatientDetails(token);
        response.put("success", true);
        response.put("patient", patientInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPatient(@RequestBody Patient patient) {
        Map<String, Object> response = new HashMap<>();

        if (service.validatePatient(patient.getEmail(), patient.getPhone())) {
            response.put("success", false);
            response.put("message", "The patient is already registered.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        int isCreated = patientService.createPatient(patient);

        if (isCreated == 1) {
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

        boolean loginResult = service.validatePatient(login.getEmail(), login.getPassword());

        if (loginResult) {
            response.put("success", true);
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

        if (!service.validateToken(token, userRole).getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<AppointmentDTO> appointments = patientService.getPatientAppointment(patientId);
        response.put("success", true);
        response.put("appointments", appointments);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/appointments/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "patient").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<AppointmentDTO> filteredAppointments = service.filterPatient(token, condition, name);
        response.put("success", true);
        response.put("appointments", filteredAppointments);
        return ResponseEntity.ok(response);
    }
}


