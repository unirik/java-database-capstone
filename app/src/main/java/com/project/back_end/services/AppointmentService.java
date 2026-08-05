package com.project.back_end.services;

import org.springframework.stereotype.Service;
import com.project.back_end.models.Appointment;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.services.GeneralService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final Service service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              Service service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            // Aquí se podría añadir un log del error (e.g., logger.error("...", e))
            return 0;
        }
    }

    @Transactional
    public String updateAppointment(Long appointmentId, Long patientId, LocalDateTime newTime) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return "Error: La cita no existe.";
        }

        Appointment appointment = appointmentOpt.get();

        // 1. Validar si el ID del paciente coincide con el dueño de la cita
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            return "Error: El paciente no está autorizado para modificar esta cita.";
        }

        // 2. Comprobar si el estado actual permite actualización (ej: Cancelada: 2 o Completada: 1 no se editan)
        if (appointment.getStatus() != 0) {
            return "Error: La cita no está disponible para actualización (ya ha sido procesada o cancelada).";
        }

        // 3. Asegurar que el médico esté disponible en el rango del nuevo horario (duración de 1 hora)
        Long doctorId = appointment.getDoctor().getId();
        LocalDateTime endTime = newTime.plusHours(1);
        
        List<Appointment> conflictingAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, newTime, endTime);

        // Filtrar la propia cita actual para evitar un falso positivo de conflicto si se mantiene la misma hora
        boolean hasConflict = conflictingAppointments.stream()
                .anyMatch(a -> !a.getId().equals(appointmentId));

        if (hasConflict) {
            return "Error: El médico no está disponible en el horario especificado.";
        }

        // Aplicar cambios y guardar de manera transaccional
        appointment.setAppointmentTime(newTime);
        appointmentRepository.save(appointment);

        return "Cita actualizada exitosamente.";
    }

    @Transactional
    public String cancelAppointment(Long appointmentId, Long patientId) {
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            return "Error: Cita no encontrada.";
        }

        Appointment appointment = appointmentOpt.get();

        // Verificar la coincidencia de propiedad del paciente
        if (appointment.getPatient() == null || !appointment.getPatient().getId().equals(patientId)) {
            return "Error: No tiene permisos para cancelar esta cita.";
        }

        try {
            appointmentRepository.delete(appointment);
            return "Cita cancelada y eliminada con éxito.";
        } catch (Exception e) {
            return "Error al intentar eliminar la cita de la base de datos.";
        }
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointments(Long doctorId, LocalDate day, String patientName) {
        // Establecer los límites de tiempo para cubrir las 24 horas del día solicitado
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(23, 59, 59, 999999999);

        try {
            if (patientName != null && !patientName.trim().isEmpty()) {
                // Filtrado por rango diario, doctor y coincidencia de nombre de paciente (usando repositorio previo)
                return appointmentRepository
                        .findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                                doctorId, patientName, startOfDay, endOfDay);
            } else {
                // Búsqueda general diaria del doctor (usando repositorio previo)
                return appointmentRepository
                        .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional
    public void changeStatus(int status, long appointmentId) {
        appointmentRepository.updateStatus(status, appointmentId);
    }
}
