export type SourceChangeStatus = "PENDING_REVIEW" | "APPLICABLE" | "REJECTED";

export interface SourceChangeCandidate {
  id: string;
  status: SourceChangeStatus;
  sourceId: string;
  sourceName: string;
  sourceUrl: string;
  previousSnapshotId: string;
  previousHash: string;
  previousExcerpt: string;
  previousCapturedAt: string;
  newSnapshotId: string;
  newHash: string;
  newExcerpt: string;
  newCapturedAt: string;
  changeSummary: string;
  schemeId: string | null;
  schemeName: string | null;
  reviewNote: string | null;
  reviewedBy: string | null;
  reviewedAt: string | null;
  proposedPolicyVersionId: string | null;
  draftedBy: string | null;
  draftedAt: string | null;
  createdAt: string;
}

export interface AdminAuditRecord {
  id: string;
  actorEmail: string;
  action: "SOURCE_CHANGE_ACCEPTED" | "SOURCE_CHANGE_REJECTED";
  entityType: string;
  entityId: string;
  decisionReason: string;
  metadata: Record<string, string>;
  recordedAt: string;
}

export interface SourceChangeReviewRequest {
  schemeId?: string;
  reason: string;
}
