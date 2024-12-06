package com.iuh.fit.readhub.services;

import com.iuh.fit.readhub.models.User;
import com.iuh.fit.readhub.repositories.ChallengeMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeMemberService {
    private final ChallengeMemberRepository memberRepository;
    private final UserService userService;

    public boolean isMember(Long challengeId, Authentication auth) {
        User user = userService.getCurrentUser(auth);
        return memberRepository.findByChallengeIdAndUserId(challengeId, user.getUserId()).isPresent();
    }
}