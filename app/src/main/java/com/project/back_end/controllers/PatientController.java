package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.models.Patient;
import com.project.back_end.services.GeneralService;
import com.project.back_end.services.PatientService;
import com.project.back_end.DTO.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patientService;
    private final GeneralService service;

    @Autowired
    public PatientController(PatientService patientService, GeneralService service) {
        this.patientService = patientService;
        this.service = service;
    }

    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatient(@PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, "patient");
        if (!temp.getBody().isEmpty()) {
            response.putAll(temp.getBody());
            return new ResponseEntity<>(response, temp.getStatusCode());
        }
        return patientService.getPatientDetails(token);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> createPatient(@RequestBody @Valid Patient patient) {
        Map<String, String> response = new HashMap<>();
        if (service.validatePatient(patient)) {
            int isCreated = patientService.createPatient(patient);
            if (isCreated == 1) {
                response.put("status", "success");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            if (isCreated == 0) {
                response.put("status", "failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        response.put("status", "Un Unexpected error occured");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/appointments/{patientId}/{userRole}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointment(@PathVariable Long patientId, @PathVariable String token, @PathVariable String userRole) {
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, userRole);
        if (!temp.getBody().isEmpty()) {
            response.putAll(temp.getBody());
            return new ResponseEntity<>(response, temp.getStatusCode());
        }
        return patientService.getPatientAppointment(patientId,token);
    }

    @GetMapping("/appointments/filter/{condition}/{name}/{token}")
    public ResponseEntity<Map<String, Object>> filterPatientAppointment(@PathVariable String condition, @PathVariable String name, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, "patient");
        if (!temp.getBody().isEmpty()) {
            response.putAll(temp.getBody());
            return new ResponseEntity<>(response, temp.getStatusCode());
        }
        return service.filterPatient(token, condition, name);
    }
}


