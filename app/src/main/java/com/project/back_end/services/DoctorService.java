package com.project.back_end.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Appointment;
import com.project.back_end.DTO.Login;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Collections;

@Service
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public DoctorService(DoctorRepository doctorRepository,
                         AppointmentRepository appointmentRepository,
                         TokenService tokenService) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public List<String> getDoctorAvailability(Long doctorId, LocalDateTime date) {
        Optional<Doctor> doctor = doctorRepository.findById(doctorId);
        List<String> availableSlots = doctor.get().getAvailableTimes().stream().map(Object::toString).collect(Collectors.toList());

        LocalDateTime startOfDay = date.with(LocalTime.MIN);
        LocalDateTime endOfDay = date.with(LocalTime.MAX);

        List<Appointment> bookedAppointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay);

        Set<String> bookedTimes = bookedAppointments.stream()
                .map(appointment -> {
                    LocalDateTime start = appointment.getAppointmentTime();
                    LocalDateTime end = start.plusDays(1);
                    return formatDate(start, end);
                })
                .collect(Collectors.toSet());

        return availableSlots.stream().filter(slot -> !bookedTimes.contains(slot)).collect(Collectors.toList());
    };

    private String formatDate(LocalDateTime start, LocalDateTime end) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return start.format(formatter) + " - " + end.format(formatter);
    }

    public int saveDoctor(Doctor doctor) {
        Doctor existingDoctor = doctorRepository.findByEmail(doctor.getEmail());
        if (existingDoctor != null) return -1;
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public int updateDoctor(Doctor doctor) {
        Optional<Doctor> existingDoctor = doctorRepository.findById(doctor.getId());
        if (!existingDoctor.isPresent()) return -1;
        try {
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional
    public List<Doctor> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        doctors.forEach(d -> d.getAvailableTimes().size());
        return doctors;
    }

    public int deleteDoctor(Long doctorId) {
        Optional<Doctor> existingDoctor = doctorRepository.findById(doctorId);
        if (!existingDoctor.isPresent()) return -1;
        try {
            if (!doctorRepository.existsById(doctorId)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(existingDoctor.get().getId());
            doctorRepository.delete(existingDoctor.get());
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    public ResponseEntity<Map<String, String>> validateDoctor(Login login) {
        Map<String, String> response = new HashMap<>();
        try {
            Doctor doctor = doctorRepository.findByEmail(login.getEmail());
            if (doctor != null) {
                if (doctor.getPassword().equals(login.getPassword())) {
                    response.put("token", tokenService.generateToken(doctor.getEmail()));
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("error", "Wrong email or password");
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
                }
            }
            response.put("error", "Unexpected error");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            response.put("error", "Interal Server Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Transactional
    public Map<String, Object> findDoctorByName(String name) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        doctors.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctors);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorsByNameSpecilityandTime(String name, String specialty, String timePeriod) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, timePeriod);
        response.put("doctors", filteredDoctors);
        return response;
    }

    public List<Doctor> filterDoctorByTime(List<Doctor> doctors, String timePeriod) {
        if (doctors == null || timePeriod == null) return Collections.emptyList();

        return doctors.stream().filter(doctor -> {
            List<String> availableTimes = doctor.getAvailableTimes();
            if (availableTimes == null || availableTimes.isEmpty()) return false;

            if ("AM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(time -> LocalTime.parse(time).isBefore(LocalTime.of(12, 0)));
            } else if ("PM".equalsIgnoreCase(timePeriod)) {
                return availableTimes.stream().anyMatch(time -> !LocalTime.parse(time).isBefore(LocalTime.of(12, 0)));
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndTime(String name, String timePeriod) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findByNameLike(name);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, timePeriod);
        response.put("doctors", filteredDoctors);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorByNameAndSpecility(String name, String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctorList = doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
        doctorList.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctorList);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorByTimeAndSpecility(String timePeriod, String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        List<Doctor> filteredDoctors = filterDoctorByTime(doctors, timePeriod);
        response.put("doctors", filteredDoctors);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorBySpecility(String specialty) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctorList = doctorRepository.findBySpecialtyIgnoreCase(specialty);
        doctorList.forEach(d -> d.getAvailableTimes().size());
        response.put("doctors", doctorList);
        return response;
    }

    @Transactional
    public Map<String, Object> filterDoctorsByTime(String timePeriod) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> allDoctors = doctorRepository.findAll();
        List<Doctor> filteredDoctors = filterDoctorByTime(allDoctors, timePeriod);
        response.put("doctors", filteredDoctors);
        return response;
    }
}
