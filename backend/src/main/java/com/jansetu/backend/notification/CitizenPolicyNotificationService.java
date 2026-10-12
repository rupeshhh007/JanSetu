package com.jansetu.backend.notification;

import com.jansetu.backend.assessment.PolicyReassessment;
import com.jansetu.backend.auth.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class CitizenPolicyNotificationService {
    private final CitizenPolicyNotificationRepository notifications;

    public CitizenPolicyNotificationService(CitizenPolicyNotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional
    public void record(PolicyReassessment reassessment) {
        CitizenPolicyNotification notification = new CitizenPolicyNotification();
        notification.setUser(reassessment.profile().getUser());
        notification.setScheme(reassessment.assessment().getPolicyVersion().getScheme());
        notification.setPolicyVersion(reassessment.assessment().getPolicyVersion());
        notification.setPreviousResult(reassessment.previousResult());
        notification.setCurrentResult(reassessment.assessment().getPolicyResult());
        notification.setTitle("Eligibility reassessed for " + notification.getScheme().getName());
        notification.setMessage(message(reassessment));
        notifications.save(notification);
    }

    @Transactional(readOnly = true)
    public List<CitizenPolicyNotification> inbox(User user) {
        return notifications.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional
    public CitizenPolicyNotification markRead(User user, UUID id) {
        CitizenPolicyNotification notification = notifications.findById(id)
                .filter(item -> item.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CitizenNotificationNotFoundException("Notification not found"));
        if (notification.getReadAt() == null) notification.setReadAt(Instant.now());
        return notification;
    }

    private String message(PolicyReassessment reassessment) {
        String result = reassessment.assessment().getPolicyResult().name().toLowerCase().replace('_', ' ');
        if (reassessment.previousResult() == null) return "A new policy version was published. Your current result is " + result + ".";
        if (reassessment.previousResult() == reassessment.assessment().getPolicyResult()) {
            return "A new policy version was published. Your eligibility result remains " + result + ".";
        }
        return "A new policy version was published. Your result changed from "
                + reassessment.previousResult().name().toLowerCase().replace('_', ' ') + " to " + result + ".";
    }
}
