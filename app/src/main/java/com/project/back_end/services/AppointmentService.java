package com.project.back_end.services;

import org.springframework.stereotype.Service;
import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.DoctorRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@Service
public class AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final GeneralService service;
    private final TokenService tokenService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              GeneralService service,
                              TokenService tokenService,
                              PatientRepository patientRepository,
                              DoctorRepository doctorRepository) {
        this.appointmentRepository = appointmentRepository;
        this.service = service;
        this.tokenService = tokenService;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }

    public int bookAppointment(Appointment appointment) {
        try {
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> updateAppointment(Appointment appointment) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> result = appointmentRepository.findById(appointment.getId());
        if (!result.isPresent()) {
            response.put("message", "Appointment not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        if (result.get().getPatient().getId != appointment.getPatient().getId()) {
            response.put("message", "Patient not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        int validatedAppointment = service.validateAppointment(appointment);
        if (validatedAppointment == 1) {
            try {
                appointmentRepository.save(appointment);
                response.put("message", "Appointment updated successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } catch(Exception e) {
                response.put("message", "Appointment update failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } else if (validatedAppointment == -1) {
            response.put("message", "Invalid Appointment");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        response.put("message", "Appointment already booked");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    public ResponseEntity<Map<String, String>> cancelAppointment(Long appointmentId, Long patientId) {
        Map<String, String> response = new HashMap<>();
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        String recoveredToken = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(recoveredToken);
        if (patient.getId() != appointmentOpt.get().getPatient().getId()) {
            response.put("message", "Patient not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        if (appointmentOpt.isPresent()) {
            try {
                appointmentRepository.delete(appointmentOpt.get());
                response.put("message", "Appointment cancelled successfully");
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } catch (Exception e) {
                response.put("message", "Appointment cancel failed");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        }
        response.put("message", "Patient not found");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @Transactional
    public Map<String, Object> getAppointments(Long doctorId, LocalDate day, String patientName) {
        Map<String, Object> response = new HashMap<>();
        String recoveredEmail = tokenService.extractEmail(token);
        Long doctorId = doctorRepository.findByEmail(recoveredEmail).getId();
        LocalDateTime startOfDay = day.atStartOfDay();
        LocalDateTime endOfDay = day.atTime(23, 59, 59, 999999999);
        List<Appointment> appointmentList;
        if (patientName.equals("null")) {
            appointmentList = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);
        } else {
            appointmentList = appointmentRepository.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(doctorId, patientName, startOfDay, endOfDay);
        }
        List<AppointmentDTO> appointmentDTOList = appointmentList.stream().map(
                app -> new AppointmentDTO(
                        app.getId(),
                        app.getDoctor().getId(),
                        app.getDoctor().getName(),
                        app.getPatient().getId(),
                        app.getPatient().getName(),
                        app.getPatient().getEmail(),
                        app.getPatient().getPhone(),
                        app.getPatient().getAddress(),
                        app.getAppointmentTime(),
                        app.getStatus())).collect(Collectors.toList());
        response.put("appointments", appointmentDTOList);
        return response;
    }

    @Transactional
    public void changeStatus(int status, long appointmentId) {
        appointmentRepository.updateStatus(status, appointmentId);
    }
}
