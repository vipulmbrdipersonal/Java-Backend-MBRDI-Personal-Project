package com.mercedes.vehicle.repository;
import com.mercedes.vehicle.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RevokedTokenRepository extends JpaRepository<RevokedToken,String> {}
