package com.project.back_end.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.back_end.models.Prescription;
import com.project.back_end.services.GeneralService;
import com.project.back_end.services.PrescriptionService;
import com.project.back_end.services.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    private final AppointmentService appointmentService;
    private final GeneralService service;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService, GeneralService service, AppointmentService appointmentService) {
        this.prescriptionService = prescriptionService;
        this.appointmentService = appointmentService;
        this.service = service;
    }

    @PostMapping("/{token}")
    public ResponseEntity<Map<String, String>> savePrescription(@RequestBody Prescription prescription, @PathVariable String token) {
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "doctor");
        if (!temp.getBody().isEmpty()) return temp;
        appointmentService.changeStatus(1, prescription.getAppointmentId());
        return prescriptionService.savePrescription(prescription);
    }

    @GetMapping("/{appointmentId}/{token}")
    public ResponseEntity<Map<String, Object>> getPrescription(@PathVariable Long appointmentId, @PathVariable String token) {
        Map<String, Object> map = new HashMap<>();
        ResponseEntity<Map<String, String>> temp = service.validateToken(token, "doctor");
        if (!temp.getBody().isEmpty()) {
            map.putAll(temp.getBody());
            return new ResponseEntity<>(map, temp.getStatusCode());
        }
        return prescriptionService.getPrescription(appointmentId);
    }
}
