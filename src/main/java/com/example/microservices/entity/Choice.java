package com.example.microservices.entity;

import jakarta.persistence.*;

@Entity
@Table(indexes = @Index(name = "idx_choice_question", columnList = "question_id"))
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String choiceText;

    @Column(nullable = false)
    private Boolean isCorrect;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    // getters & setters
}