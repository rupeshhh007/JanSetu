package com.jansetu.backend.source.change;

import com.jansetu.backend.source.OfficialSource;
import com.jansetu.backend.source.SourceSnapshot;
import com.jansetu.backend.audit.AdminAuditService;
import com.jansetu.backend.scheme.repository.SchemeRepository;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SourceChangeCandidateServiceTest {
    @Test
    void createsCandidateForMeaningfulMainContentChangeButIgnoresStylingAndNavigation() {
        SourceChangeCandidateRepository candidates = mock(SourceChangeCandidateRepository.class);
        when(candidates.save(any(SourceChangeCandidate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        SourceChangeCandidateService service = new SourceChangeCandidateService(candidates, new SourceContentNormalizer(),
                mock(SchemeRepository.class), mock(AdminAuditService.class));

        SourceSnapshot baseline = snapshot("<nav>Old navigation</nav><main><h1>Scholarship</h1><p>Income below 200000</p></main>", "sha256:old");
        SourceSnapshot stylingOnly = snapshot("<nav>New navigation</nav><style>.x { color: blue; }</style><main><h1>Scholarship</h1><p>Income below 200000</p></main>", "sha256:styled");
        SourceSnapshot changed = snapshot("<main><h1>Scholarship</h1><p>Income below 300000</p></main>", "sha256:new");

        assertFalse(service.detect(baseline, stylingOnly).isPresent());
        var candidate = service.detect(baseline, changed);

        assertTrue(candidate.isPresent());
        assertTrue(candidate.orElseThrow().getChangeSummary().contains("Administrative review is required"));
        assertTrue(candidate.orElseThrow().getStatus() == SourceChangeCandidateStatus.PENDING_REVIEW);
    }

    private SourceSnapshot snapshot(String html, String hash) {
        OfficialSource source = new OfficialSource();
        source.setCanonicalUrl("https://example.gov.in/scholarship");
        SourceSnapshot snapshot = new SourceSnapshot();
        snapshot.setSource(source);
        snapshot.setContentType("text/html");
        snapshot.setContentBytes(html.getBytes(StandardCharsets.UTF_8));
        snapshot.setContentHash(hash);
        return snapshot;
    }
}
