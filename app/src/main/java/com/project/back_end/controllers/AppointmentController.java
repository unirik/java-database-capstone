package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.TokenService;
import com.project.back_end.services.GeneralService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final GeneralService service;
    private final TokenService tokenService;

    public AppointmentController(AppointmentService appointmentService, GeneralService service, TokenService tokenService) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable String date, @PathVariable String patientName, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "doctor").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            LocalDate localDate = LocalDate.parse(date);

            Long doctorId = tokenService.extractIdFromClaim(token, "doctorId");
            if (doctorId == null) {
                response.put("success", false);
                response.put("message", "Token inválido o ID de usuario no encontrado.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            var appointments = appointmentService.getAppointments(doctorId, localDate, patientName);
            response.put("success", true);
            response.put("data", appointments);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("message", "Formato de fecha inválido. Use YYYY-MM-DD.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Appointment appointment, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "patient").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        int bookingResult = appointmentService.bookAppointment(appointment);

        if (bookingResult == 1) {
            response.put("success", true);
            response.put("message", "Appointment reserved successfully");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else if (bookingResult == 0) {
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

        if (!service.validateToken(token, "patient").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            LocalDateTime date = appointment.getAppointmentTime();

            Long patientId = tokenService.extractIdFromClaim(token, "patientId");
            if (patientId == null) {
                response.put("success", false);
                response.put("message", "Invalid ID.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            Long appointmentId = tokenService.extractIdFromClaim(token, "appointmentId");
            if (appointmentId == null) {
                response.put("success", false);
                response.put("message", "Invalid ID.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String isUpdated = appointmentService.updateAppointment(appointmentId, patientId, date);

            if (Boolean.parseBoolean(isUpdated)) {
                response.put("success", true);
                response.put("message", "Appointment updated successfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "The appointment could not be updated.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected Error.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "patient").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Long patientId = tokenService.extractIdFromClaim(token, "patientId");
            if (patientId == null) {
                response.put("success", false);
                response.put("message", "Invalid ID.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String isCanceled = appointmentService.cancelAppointment(id, patientId);

            if (Boolean.parseBoolean(isCanceled)) {
                response.put("success", true);
                response.put("message", "Appointment canceled succesfully.");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "The appointment not exists.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (java.lang.Exception e) {
            response.put("success", false);
            response.put("message", "Unexpected Error.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
