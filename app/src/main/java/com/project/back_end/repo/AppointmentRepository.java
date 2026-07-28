package com.project.back_end.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.back_end.models.Appointment;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    /**
     * This method retrieves a list of appointments for a specific doctor within a given time range.
     * @param Long doctorId
     * @param LocalDateTime start
     * @param LocalDateTime end
     * @return List<Appointment>
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d WHERE d.id = :doctorId AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );

    /**
     * This method retrieves appointments for a specific doctor and patient name (ignoring case) within a given time range.
     * @param Long doctorId
     * @param String patientName
     * @param LocalDateTime start
     * @param LocalDateTime end
     * @return List<Appointment>
     */
    @Query("SELECT a FROM Appointment a LEFT JOIN FETCH a.doctor d LEFT JOIN FETCH a.patient p " +
           "WHERE d.id = :doctorId AND UPPER(p.name) LIKE UPPER(CONCAT('%', :patientName, '%')) " +
           "AND a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
            @Param("doctorId") Long doctorId, 
            @Param("patientName") String patientName, 
            @Param("start") LocalDateTime start, 
            @Param("end") LocalDateTime end
    );

    /**
     * This method deletes all appointments associated with a particular doctor.
     * @param Long doctorId
     * @return void
     */
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);

    /**
     * This method retrieves all appointments for a specific patient.
     * @param Long patientId
     * @return List<Appointment>
     */
    List<Appointment> findByPatientId(Long patientId);    

    /**
     * This method retrieves all appointments for a specific patient with a given status, ordered by the appointment time.
     * @param Long patientId
     * @param int status
     * @return List<Appointment>
     */
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    /**
     * This method retrieves appointments based on a doctor’s name (using a LIKE query) and the patient’s ID.
     * @param String doctorName
     * @param Long patientId
     * @return List<Appointment>
     */
    @Query("SELECT a FROM Appointment a JOIN a.doctor d JOIN a.patient p " +
           "WHERE UPPER(d.name) LIKE UPPER(CONCAT('%', :doctorName, '%')) AND p.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId
    );

    /**
     * This method retrieves appointments based on a doctor’s name (using a LIKE query), patient’s ID, and a specific appointment status.
     * @param String doctorName
     * @param Long patientId
     * @param int status
     * @return List<Appointment>
     */
    @Query("SELECT a FROM Appointment a JOIN a.doctor d JOIN a.patient p " +
           "WHERE UPPER(d.name) LIKE UPPER(CONCAT('%', :doctorName, '%')) AND p.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(
            @Param("doctorName") String doctorName, 
            @Param("patientId") Long patientId, 
            @Param("status") int status
    );

    /**
     * This method updates the status of a specific appointment based on its ID.
     * @param int status
     * @param id
     * @return void
     */
    @Modifying
    @Transactional
    @Query("UPDATE Appointment a SET a.status = :status WHERE a.id = :id")
    void updateStatus(@Param("status") int status, @Param("id") long id);
}
