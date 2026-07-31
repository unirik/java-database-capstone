package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final Service service;

    public AppointmentController(AppointmentService appointmentService, Service generalService) {
        this.appointmentService = appointmentService;
        this.generalService = generalService;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable String date, @PathVariable String patientName, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "doctor")) {
            response.put("success", false);
            response.put("message", "Token inválido o expirado. Acceso denegado.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        var appointments = appointmentService.getAppointmentsByDateAndPatient(date, patientName);
        response.put("success", true);
        response.put("data", appointments);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "patient")) {
            response.put("success", false);
            response.put("message", "Invalid Token.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String bookingResult = appointmentService.bookAppointment(appointment);

        if ("SUCCESS".equals(bookingResult)) {
            response.put("success", true);
            response.put("message", "Appointment reserved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if ("INVALID_DOCTOR".equals(bookingResult)) {
            response.put("success", false);
            response.put("message", "Invalid Doctor ID.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            response.put("success", false);
            response.put("message", "The selected time slot is already taken.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "patient")) {
            response.put("success", false);
            response.put("message", "Not authorized for modifying this appointment.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean isUpdated = appointmentService.updateAppointment(appointment);

        if (isUpdated) {
            response.put("success", true);
            response.put("message", "Appointment updated successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "The appointment could not be updated.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!generalService.validateToken(token, "patient")) {
            response.put("success", false);
            response.put("message", "Not authorized for avoid this appointment.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean isCanceled = appointmentService.cancelAppointment(id);

        if (isCanceled) {
            response.put("success", true);
            response.put("message", "Appointment canceled succesfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "The appointment not exists.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
