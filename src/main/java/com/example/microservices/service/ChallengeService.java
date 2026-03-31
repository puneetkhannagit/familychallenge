package com.example.microservices.service;

import com.example.microservices.entity.Challenge;
import com.example.microservices.entity.ChallengeInvitation;
import com.example.microservices.entity.Topic;
import com.example.microservices.entity.User;
import com.example.microservices.repository.ChallengeInvitationRepository;
import com.example.microservices.repository.ChallengeRepository;
import com.example.microservices.repository.TopicRepository;
import com.example.microservices.repository.UserRepository;
import com.example.microservices.dto.CreateChallengeRequest;
import com.example.microservices.dto.InviteUsersRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeInvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;

    public ChallengeService(
            ChallengeRepository challengeRepository,
            ChallengeInvitationRepository invitationRepository,
            UserRepository userRepository,
            TopicRepository topicRepository
    ) {
        this.challengeRepository = challengeRepository;
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public Challenge createChallenge(CreateChallengeRequest req, User createdBy) {
        if (req.getEndsAt().isBefore(req.getStartsAt())) {
            throw new IllegalArgumentException("endsAt must be after startsAt");
        }
        if (req.getEndsAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("endsAt must be in the future");
        }

        Challenge c = new Challenge();
        c.setName(req.getTitle());
        c.setStartDatetime(req.getStartsAt());
        c.setEndDatetime(req.getEndsAt());
        c.setCreatedBy(createdBy);

        if (req.getTopicId() == null) {
            throw new IllegalArgumentException("topicId is required");
        }
        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + req.getTopicId()));
        c.setTopic(topic);

        return challengeRepository.save(c);
    }

    @Transactional
    public List<ChallengeInvitation> invite(Long challengeId, InviteUsersRequest req) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        List<ChallengeInvitation> created = new ArrayList<>();

        for (String emailRaw : req.getEmails()) {
            String email = emailRaw.trim().toLowerCase();

            if (invitationRepository.findByChallenge_IdAndEmail(challengeId, email).isPresent()) {
                continue;
            }

            ChallengeInvitation inv = new ChallengeInvitation();
            inv.setChallenge(challenge);
            inv.setEmail(email);

            userRepository.findByEmailIgnoreCase(email).ifPresent(inv::setInvitedUser);

            created.add(invitationRepository.save(inv));

            // TODO: send email here (either invitation link or "you were invited")
        }

        return created;
    }
}
