package com.cogniquest.cogniquest.repository;

import com.cogniquest.cogniquest.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Used by UserService for Teacher login lookup
    AppUser findByUsername(String username);

    // Used by UserService for Student allocation/login lookup
    AppUser findByRollNumber(String rollNumber);

    // Explicitly defining findById which returns an Optional, although it's inherited
    Optional<AppUser> findById(Long id);
}
