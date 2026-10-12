package com.jansetu.backend.profile.fact;

/** Describes where a profile fact originated; higher tiers may protect a value from a weaker update. */
public enum ProfileFactProvenance {
    CITIZEN_DECLARED(1),
    DOCUMENT_VERIFIED(2),
    OFFICIAL_RECORD(3);

    private final int assuranceRank;

    ProfileFactProvenance(int assuranceRank) {
        this.assuranceRank = assuranceRank;
    }

    public int assuranceRank() {
        return assuranceRank;
    }
}
