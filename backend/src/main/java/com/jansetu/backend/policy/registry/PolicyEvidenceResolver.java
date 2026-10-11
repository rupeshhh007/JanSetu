package com.jansetu.backend.policy.registry;

import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.source.SourceSnapshotRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/** Converts an administrative snapshot selection into canonical immutable policy evidence. */
@Component
class PolicyEvidenceResolver {
    private final SourceSnapshotRepository snapshots;

    PolicyEvidenceResolver(SourceSnapshotRepository snapshots) {
        this.snapshots = snapshots;
    }

    List<PolicySourceReference> resolve(List<PolicySourceReference> references) {
        if (references == null || references.isEmpty()) {
            throw new PolicyLifecycleException("At least one immutable official-source snapshot is required for a policy version.");
        }
        return references.stream().map(this::resolve).toList();
    }

    private PolicySourceReference resolve(PolicySourceReference reference) {
        if (reference == null || reference.snapshotId() == null) {
            throw new PolicyLifecycleException("Every policy source reference must select an official-source snapshot.");
        }
        SourceSnapshot snapshot = snapshots.findById(reference.snapshotId())
                .orElseThrow(() -> new PolicyLifecycleException("A selected official-source snapshot no longer exists."));
        assertMatchesIfProvided(reference.sourceUrl(), snapshot.getSourceUrlAtCapture(), "URL");
        assertMatchesIfProvided(reference.sourceTitle(), snapshot.getSourceNameAtCapture(), "title");
        assertMatchesIfProvided(reference.contentHash(), snapshot.getContentHash(), "content hash");
        String excerpt = reference.excerpt() == null || reference.excerpt().isBlank()
                ? snapshot.getContentExcerpt()
                : reference.excerpt().trim();
        return new PolicySourceReference(snapshot.getId(), snapshot.getSourceUrlAtCapture(), snapshot.getSourceNameAtCapture(),
                snapshot.getContentHash(), excerpt);
    }

    private void assertMatchesIfProvided(String submitted, String captured, String fieldName) {
        if (submitted != null && !submitted.isBlank() && !submitted.equals(captured)) {
            throw new PolicyLifecycleException("The submitted source " + fieldName + " does not match its immutable snapshot.");
        }
    }
}
