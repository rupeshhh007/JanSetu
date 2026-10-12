package com.jansetu.backend.source.change;

import com.jansetu.backend.source.SourceSnapshot;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Creates a review queue entry only for a meaningful, deterministic text change. */
@Service
public class SourceChangeCandidateService {
    private final SourceChangeCandidateRepository candidates;
    private final SourceContentNormalizer normalizer;

    public SourceChangeCandidateService(SourceChangeCandidateRepository candidates, SourceContentNormalizer normalizer) {
        this.candidates = candidates;
        this.normalizer = normalizer;
    }

    @Transactional
    public Optional<SourceChangeCandidate> detect(SourceSnapshot previous, SourceSnapshot current) {
        if (previous == null || previous.getContentHash().equals(current.getContentHash())) return Optional.empty();
        Optional<String> previousText = normalizer.normalizedText(previous);
        Optional<String> currentText = normalizer.normalizedText(current);
        if (previousText.isEmpty() || currentText.isEmpty() || previousText.get().equals(currentText.get())) {
            return Optional.empty();
        }
        SourceChangeCandidate candidate = new SourceChangeCandidate();
        candidate.setSource(current.getSource());
        candidate.setPreviousSnapshot(previous);
        candidate.setNewSnapshot(current);
        candidate.setChangeSummary(summary(previous, current, previousText.get(), currentText.get()));
        return Optional.of(candidates.save(candidate));
    }

    private String summary(SourceSnapshot previous, SourceSnapshot current, String previousText, String currentText) {
        return "Meaningful text changed: " + previousText.length() + " to " + currentText.length()
                + " normalized characters (" + previous.getContentHash() + " → " + current.getContentHash() + ")."
                + " Administrative review is required before this evidence can affect a JanSetu policy.";
    }
}
