package com.example.microservices.repository;

import com.example.microservices.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findByStandard_Id(Long standardId);
}
