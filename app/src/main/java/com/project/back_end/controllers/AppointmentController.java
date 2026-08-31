package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.GeneralService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointmentService;
    private final GeneralService service;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, GeneralService service) {
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @GetMapping("/{date}/{patientName}/{token}")
    public ResponseEntity<Map<String, Object>> getAppointments(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, @PathVariable String patientName, @PathVariable String token) {
        Map<String,Object> response = new HashMap<>();
        ResponseEntity<Map<String,String>> temp = service.validateToken(token, "doctor");
        if (!temp.getBody().isEmpty()) {
            map.putAll(temp.getBody());
            return new ResponseEntity<>(map, temp.getStatusCode());
        }
        map = appointmentService.getAppointment(patientName, date, token);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }
    
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> bookAppointment(@RequestBody @Valid Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "patient");
        if (!temp.getBody().isEmpty()) return temp;
        Map<String, String> response = new HashMap<>();
        int validated = service.validateAppointment(appointment);
        if (validated == 1) {
            int result = appointmentService.bookAppointment(appointment);
            if (result == 1) {
                response.put("message", "Appointment registered successfully");
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            }
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else if (validated == -1) {
            response.put("message", "invalid data");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        response.put("message", "Appointment already exists");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PutMapping("/{token}")
    public ResponseEntity<Map<String, String>> updateAppointment(@RequestBody @Valid Appointment appointment, @PathVariable String token) {
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "patient");
        if (!temp.getBody().isEmpty()) return temp;
        return appointmentService.updateAppointment(appointment);
    }

    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id, @PathVariable String token) {
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "patient");
        if (!temp.getBody().isEmpty()) return temp;
        return appointmentService.cancelAppointment(id, token);
    }
}
