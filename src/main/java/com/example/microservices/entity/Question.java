package com.example.microservices.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(indexes = {
        @Index(name = "idx_question_topic", columnList = "topic_id"),
        @Index(name = "idx_question_difficulty", columnList = "difficultyLevel")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType questionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DifficultyLevel difficultyLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    private Integer version = 1;

    private Boolean active = true;

    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
}