package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import com.project.back_end.models.Prescription;
import com.project.back_end.services.GeneralService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final GeneralService service;

    public PrescriptionController(PrescriptionService prescriptionService, GeneralService service, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> savePrescription(@RequestBody Prescription prescription, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "doctor").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        ResponseEntity<Object> isSaved = prescriptionService.savePrescription(prescription);

        if (isSaved != null) {
            response.put("success", true);
            response.put("message", "Prescription successfully created.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("success", false);
            response.put("message", "Error occurs while saving data.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validateToken(token, "doctor").getStatusCode().is2xxSuccessful()) {
            response.put("success", false);
            response.put("message", "Denied Access.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        ResponseEntity<Object> prescription = prescriptionService.getPrescription(appointmentId);

        if (prescription != null) {
            response.put("success", true);
            response.put("prescription", prescription);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Not Prescription found bind to this appointment.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}
