package com.jansetu.backend.notification.api;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.notification.CitizenPolicyNotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class CitizenPolicyNotificationController {
    private final CitizenPolicyNotificationService notifications;

    public CitizenPolicyNotificationController(CitizenPolicyNotificationService notifications) {
        this.notifications = notifications;
    }

    @GetMapping
    public List<CitizenPolicyNotificationResponse> inbox(@AuthenticationPrincipal User user) {
        return notifications.inbox(user).stream().map(CitizenPolicyNotificationResponse::from).toList();
    }

    @PatchMapping("/{notificationId}/read")
    public CitizenPolicyNotificationResponse markRead(@AuthenticationPrincipal User user,
                                                       @PathVariable UUID notificationId) {
        return CitizenPolicyNotificationResponse.from(notifications.markRead(user, notificationId));
    }
}
