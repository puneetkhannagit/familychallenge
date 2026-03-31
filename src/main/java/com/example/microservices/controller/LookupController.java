package com.example.microservices.controller;

import com.example.microservices.entity.Standard;
import com.example.microservices.entity.Topic;
import com.example.microservices.repository.StandardRepository;
import com.example.microservices.repository.TopicRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LookupController {

    private final StandardRepository standardRepository;
    private final TopicRepository topicRepository;

    public LookupController(StandardRepository standardRepository, TopicRepository topicRepository) {
        this.standardRepository = standardRepository;
        this.topicRepository = topicRepository;
    }

    @GetMapping("/standards")
    public List<Standard> standards() {
        return standardRepository.findAll();
    }

    @GetMapping("/topics")
    public List<Topic> topics(@RequestParam(name = "standardId", required = false) Long standardId) {
        if (standardId == null) {
            return topicRepository.findAll();
        }
        return topicRepository.findByStandard_Id(standardId);
    }
}
