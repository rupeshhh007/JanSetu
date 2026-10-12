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
  action: "SOURCE_CHANGE_ACCEPTED" | "SOURCE_CHANGE_REJECTED" | "POLICY_DRAFT_CREATED" |
    "POLICY_SUBMITTED_FOR_REVIEW" | "POLICY_APPROVED" | "POLICY_PUBLISHED";
  entityType: string;
  entityId: string;
  decisionReason: string;
  metadata: Record<string, string>;
  recordedAt: string;
}

export type PolicyVersionStatus = "DRAFT" | "IN_REVIEW" | "APPROVED" | "PUBLISHED" | "SUPERSEDED";

export interface BooleanPolicyRule {
  type: "booleanMatch";
  id: string;
  label: string;
  fact: "IS_STUDENT" | "IS_FARMER" | "HAS_DISABILITY";
  expectedValue: boolean;
}

export interface PolicyVersion {
  id: string;
  schemeId: string;
  versionNumber: number;
  status: PolicyVersionStatus;
  effectiveFrom: string;
  effectiveTo: string | null;
  approvedAt: string | null;
  publishedAt: string | null;
  policyRule: BooleanPolicyRule | Record<string, unknown>;
  sourceReferences: { snapshotId: string; contentHash: string; sourceName: string; sourceUrl: string }[];
  revision: number;
}

export interface PolicyDraftRequest {
  effectiveFrom: string;
  policyRule: BooleanPolicyRule;
}

export interface SourceChangeReviewRequest {
  schemeId?: string;
  reason: string;
}
