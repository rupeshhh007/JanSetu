package com.jansetu.backend.notification;

import com.jansetu.backend.auth.entity.User;
import com.jansetu.backend.policy.domain.PolicyResult;
import com.jansetu.backend.policy.registry.PolicyVersion;
import com.jansetu.backend.scheme.entity.Scheme;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "citizen_policy_notifications", uniqueConstraints = @UniqueConstraint(
        name = "uq_citizen_policy_notification", columnNames = {"user_id", "policy_version_id"}))
public class CitizenPolicyNotification {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "scheme_id", nullable = false)
    private Scheme scheme;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "policy_version_id", nullable = false)
    private PolicyVersion policyVersion;
    @Enumerated(EnumType.STRING) @Column(name = "current_result", nullable = false, length = 20)
    private PolicyResult currentResult;
    @Enumerated(EnumType.STRING) @Column(name = "previous_result", length = 20)
    private PolicyResult previousResult;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, length = 1000) private String message;
    @Column(name = "read_at") private Instant readAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;

    public UUID getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Scheme getScheme() { return scheme; }
    public void setScheme(Scheme scheme) { this.scheme = scheme; }
    public PolicyVersion getPolicyVersion() { return policyVersion; }
    public void setPolicyVersion(PolicyVersion policyVersion) { this.policyVersion = policyVersion; }
    public PolicyResult getCurrentResult() { return currentResult; }
    public void setCurrentResult(PolicyResult currentResult) { this.currentResult = currentResult; }
    public PolicyResult getPreviousResult() { return previousResult; }
    public void setPreviousResult(PolicyResult previousResult) { this.previousResult = previousResult; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public Instant getCreatedAt() { return createdAt; }
}
