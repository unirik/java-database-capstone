package com.project.back_end.services;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrescriptionService {
    private static final Logger logger = LoggerFactory.getLogger(PrescriptionService.class);
    private final PrescriptionRepository prescriptionRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public ResponseEntity<Object> savePrescription(Prescription prescription) {
        try {
            if (prescription.getAppointmentId() == null) {
                Map<String, String> badRequestResponse = new HashMap<>();
                badRequestResponse.put("error", "El ID de la cita (appointmentId) es obligatorio.");
                return new ResponseEntity<>(badRequestResponse, HttpStatus.BAD_REQUEST);
            }

            List<Prescription> existingPrescriptions = prescriptionRepository
                    .findByAppointmentId(prescription.getAppointmentId());

            if (!existingPrescriptions.isEmpty()) {
                Map<String, String> conflictResponse = new HashMap<>();
                conflictResponse.put("message", "La receta médica ya existe para esta cita.");
                return new ResponseEntity<>(conflictResponse, HttpStatus.BAD_REQUEST);
            }

            Prescription savedPrescription = prescriptionRepository.save(prescription);
            
            Map<String, Object> successResponse = new HashMap<>();
            successResponse.put("message", "Receta médica creada exitosamente.");
            successResponse.put("data", savedPrescription);
            
            return new ResponseEntity<>(successResponse, HttpStatus.CREATED);

        } catch (Exception e) {
            logger.error("Error crítico al intentar guardar la receta médica", e);
            return buildInternalServerErrorResponse("Error al intentar guardar la receta en la base de datos.");
        }
    }

    public ResponseEntity<Object> getPrescription(Long appointmentId) {
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);

            if (prescriptions.isEmpty()) {
                Map<String, String> notFoundResponse = new HashMap<>();
                notFoundResponse.put("message", "No se encontró ninguna receta médica vinculada a la cita provista.");
                return new ResponseEntity<>(notFoundResponse, HttpStatus.OK); 
            }

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("prescription", prescriptions.get(0)); // Retorna la receta principal asociada
            
            return new ResponseEntity<>(responseBody, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error crítico al recuperar la receta de la cita con ID: {}", appointmentId, e);
            return buildInternalServerErrorResponse("Error interno del servidor al procesar la búsqueda de la receta.");
        }
    }

    private ResponseEntity<Object> buildInternalServerErrorResponse(String message) {
        Map<String, String> errorResponseBody = new HashMap<>();
        errorResponseBody.put("error", "Internal Server Error");
        errorResponseBody.put("message", message);
        return new ResponseEntity<>(errorResponseBody, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
