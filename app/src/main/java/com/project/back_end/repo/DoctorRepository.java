package com.project.back_end.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.back_end.models.Doctor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long>{
    /**
     * This method retrieves a Doctor by their email.
     * @param String email
     * @return Doctor
     */
    Doctor findByEmail(String email);

    /**
     * This method retrieves a list of Doctors whose name contains the provided search string (case-sensitive).
     * @param String name
     * @return List<Doctor>
     */
    @Query("SELECT d FROM Doctor d WHERE d.name LIKE CONCAT('%', :name, '%')")
    List<Doctor> findByNameLike(String name);
    
    /**
     * This method retrieves a list of Doctors where the name contains the search string (case-insensitive) and the specialty matches exactly (case-insensitive).
     * @param String name
     * @param String specialty
     * @return List<Doctor>
     */
    @Query("SELECT d FROM Doctor d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND LOWER(d.specialty) = LOWER(:specialty)")
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);

    /**
     * This method retrieves a list of Doctors with the specified specialty, ignoring case sensitivity.
     * @param String specialty
     * @return List<Doctor>
     */
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
}
