package com.mercedes.telemetry.repository;

import com.mercedes.telemetry.entity.UserProfilePhoto;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfilePhotoRepository
        extends JpaRepository<UserProfilePhoto, UUID> {

    Optional<UserProfilePhoto> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);
}