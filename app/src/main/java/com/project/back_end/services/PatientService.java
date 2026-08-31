package com.project.back_end.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.models.Patient;
import com.project.back_end.models.Appointment;
import com.project.back_end.DTO.AppointmentDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {
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

    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, String>> getPatientAppointment(Long patientId, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
            List<AppointmentDTO> appointmentDTOList = appointments.stream().map(
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
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(Long patientId, String condition) {
        Map<String, Object> response = new HashMap<>();
        List<Appointment> appointments;
        if (condition.equals("past")) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 1);
        } else if (condition.equals("future")) {
            appointments = appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, 0);
        } else {
            response.put("error", "Invalid filter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        List<AppointmentDTO> appointmentDTOList = appointments.stream().map(
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(Long patientId, String doctorName) {
        Map <String, Object> response = new HashMap<>();
        List<Appointment> appointmentList = appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        Map <String, Object> response = new HashMap<>();
        List<Appointment> appointmentList;
        if (condition.equals("past")) {
            appointmentList = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, 1);
        } else if (condition.equals("future")) {
            appointmentList = appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, 0);
        } else {
            response.put("error", "Invalid filter");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
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
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        String email = tokenService.extractEmail(token);
        Patient patient = patientRepository.findByEmail(email);
        response.put("patient", patient);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
