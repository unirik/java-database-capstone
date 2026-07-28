package com.project.back_end.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {
    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            logger.error("Error crítico al intentar guardar el paciente en la base de datos", e);
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointment(Long patientId) {
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            return convertToDtoList(appointments);
        } catch (Exception e) {
            logger.error("Error al recuperar las citas del paciente con ID: {}", patientId, e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByCondition(Long patientId, String condition) {
        try {
            int status = determineStatusByCondition(condition);
            List<Appointment> appointments = appointmentRepository
                    .findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);
            return convertToDtoList(appointments);
        } catch (IllegalArgumentException e) {
            logger.warn("Condición de filtrado no válida provista: {}", condition);
            throw e; // El controlador capturará esto para responder un Bad Request
        } catch (Exception e) {
            logger.error("Error al filtrar citas por condición '{}' para el paciente ID: {}", condition, patientId, e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctor(Long patientId, String doctorName) {
        try {
            if (doctorName == null || doctorName.trim().isEmpty()) {
                return getPatientAppointment(patientId);
            }
            List<Appointment> appointments = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
            return convertToDtoList(appointments);
        } catch (Exception e) {
            logger.error("Error al filtrar por médico '{}' para el paciente ID: {}", doctorName, patientId, e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        try {
            int status = determineStatusByCondition(condition);
            List<Appointment> appointments = appointmentRepository
                    .filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
            return convertToDtoList(appointments);
        } catch (IllegalArgumentException e) {
            logger.warn("Condición de filtrado combinada no válida provista: {}", condition);
            throw e;
        } catch (Exception e) {
            logger.error("Error en filtro combinado (Médico: '{}', Condición: '{}') para el paciente ID: {}", 
                    doctorName, condition, patientId, e);
            return Collections.emptyList();
        }
    }

    @Transactional(readOnly = true)
    public Patient getPatientDetails(String token) {
        try {
            // Extraer email desde el Token (Asumiendo que soporta remover el prefijo 'Bearer ' si existiera)
            String email = tokenService.getEmailFromToken(token);
            if (email == null || email.isEmpty()) {
                throw new IllegalArgumentException("El token provisto no contiene un correo electrónico válido.");
            }
            
            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                logger.warn("No se encontró ningún paciente asociado al correo extraído del token: {}", email);
            }
            return patient;
        } catch (Exception e) {
            logger.error("Error crítico al recuperar los detalles del paciente mediante el token", e);
            throw e; 
        }
    }

    private int determineStatusByCondition(String condition) {
        if ("future".equalsIgnoreCase(condition)) {
            return 0; // Scheduled / Pendiente
        } else if ("past".equalsIgnoreCase(condition)) {
            return 1; // Completed / Completada
        } else {
            throw new IllegalArgumentException("La condición debe ser estrictamente 'past' o 'future'.");
        }
    }

    private List<AppointmentDTO> convertToDtoList(List<Appointment> appointments) {
        if (appointments == null) {
            return Collections.emptyList();
        }
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AppointmentDTO convertToDto(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor() != null ? appointment.getDoctor().getId() : null,
                appointment.getDoctor() != null ? appointment.getDoctor().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getId() : null,
                appointment.getPatient() != null ? appointment.getPatient().getName() : null,
                appointment.getPatient() != null ? appointment.getPatient().getEmail() : null,
                appointment.getPatient() != null ? appointment.getPatient().getPhone() : null,
                appointment.getPatient() != null ? appointment.getPatient().getAddress() : null,
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}
