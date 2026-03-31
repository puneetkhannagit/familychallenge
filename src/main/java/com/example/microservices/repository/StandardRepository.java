package com.example.microservices.repository;

import com.example.microservices.entity.Standard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StandardRepository extends JpaRepository<Standard, Long> {
}
