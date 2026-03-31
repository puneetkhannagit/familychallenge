package com.example.microservices.repository;

import com.example.microservices.entity.ChallengeInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeInvitationRepository extends JpaRepository<ChallengeInvitation, Long> {
    Optional<ChallengeInvitation> findByChallenge_IdAndEmail(Long challengeId, String email);
}
