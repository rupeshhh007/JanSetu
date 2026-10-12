package com.jansetu.backend.profile.fact;

/** Verification is tracked independently of the source that supplied a fact. */
public enum ProfileFactVerificationStatus {
    SELF_DECLARED(1),
    DOCUMENT_VERIFIED(2),
    AUTHORITATIVE(3);

    private final int assuranceRank;

    ProfileFactVerificationStatus(int assuranceRank) {
        this.assuranceRank = assuranceRank;
    }

    public int assuranceRank() {
        return assuranceRank;
    }
}
