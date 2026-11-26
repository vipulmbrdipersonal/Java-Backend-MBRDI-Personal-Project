package com.mercedes.dashboard.repository;

import com.mercedes.dashboard.entity.VehicleHealthSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleHealthSnapshotRepository
        extends JpaRepository<VehicleHealthSnapshot, Long> {

    List<VehicleHealthSnapshot> findAllByOrderByAssessedAtDesc();

    long countByStatus(String status);
}