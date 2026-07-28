package com.project.back_end.repo;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.project.back_end.models.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
    /**
     * This method allows you to find an Admin by their username.
     * @param String username
     * @return an Admin entity that matches the provided username.
     */
    Admin findByUsername(String username);
}
