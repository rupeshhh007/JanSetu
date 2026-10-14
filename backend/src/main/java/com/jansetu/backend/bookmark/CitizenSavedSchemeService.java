package com.jansetu.backend.bookmark;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.auth.repository.UserRepository;
import com.jansetu.backend.auth.service.CurrentUserService;
import com.jansetu.backend.scheme.entity.Scheme;
import com.jansetu.backend.scheme.entity.SchemeStatus;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

/** A private, durable shortlist scoped entirely by the current authenticated user. */
@Service
public class CitizenSavedSchemeService {
    private final CitizenSavedSchemeRepository saved;
    private final SchemeRepository schemes;
    private final CurrentUserService current;
    private final UserRepository users;

    public CitizenSavedSchemeService(CitizenSavedSchemeRepository saved, SchemeRepository schemes,
                                     CurrentUserService current, UserRepository users) {
        this.saved = saved;
        this.schemes = schemes;
        this.current = current;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<UUID> mine() {
        UUID actorId = current.getCurrentUser().getId();
        return saved.findByCitizenIdOrderByCreatedAtDesc(actorId)
                .stream().map(item -> item.getScheme().getId()).toList();
    }

    @Transactional
    public void save(UUID schemeId) {
        User actor = lockCurrentUser();
        Scheme scheme = schemes.findById(schemeId)
                .filter(item -> item.getStatus() == SchemeStatus.PUBLISHED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Published scheme not found"));
        if (saved.findByCitizenIdAndSchemeId(actor.getId(), schemeId).isPresent()) return;

        CitizenSavedScheme item = new CitizenSavedScheme();
        item.setCitizen(actor);
        item.setScheme(scheme);
        saved.save(item);
    }

    @Transactional
    public void remove(UUID schemeId) {
        User actor = lockCurrentUser();
        saved.findByCitizenIdAndSchemeId(actor.getId(), schemeId)
                .ifPresent(saved::delete);
    }

    private User lockCurrentUser() {
        UUID actorId = current.getCurrentUser().getId();
        return users.lockById(actorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account unavailable"));
    }
}
