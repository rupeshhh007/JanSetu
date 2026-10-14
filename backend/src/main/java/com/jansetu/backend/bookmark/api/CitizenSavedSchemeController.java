package com.jansetu.backend.bookmark.api;

import com.jansetu.backend.bookmark.CitizenSavedSchemeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** Only the caller's saved scheme IDs are returned; no userId parameter is accepted. */
@RestController
@RequestMapping("/api/v1/citizen/saved-schemes")
public class CitizenSavedSchemeController {
    private final CitizenSavedSchemeService bookmarks;

    public CitizenSavedSchemeController(CitizenSavedSchemeService bookmarks) {
        this.bookmarks = bookmarks;
    }

    @GetMapping
    public List<UUID> mine() {
        return bookmarks.mine();
    }

    @PutMapping("/{schemeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void save(@PathVariable UUID schemeId) {
        bookmarks.save(schemeId);
    }

    @DeleteMapping("/{schemeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID schemeId) {
        bookmarks.remove(schemeId);
    }
}
