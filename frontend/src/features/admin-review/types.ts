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
    "POLICY_SUBMITTED_FOR_REVIEW" | "POLICY_APPROVED" | "POLICY_PUBLISHED" |
    "AGENT_INVESTIGATION_APPROVED" | "AGENT_INVESTIGATION_REJECTED" | "AGENT_INVESTIGATION_FORWARDED";
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

export type AgentExecutionMode = "DETERMINISTIC_FIXTURE" | "LIVE_MODEL";
export type AgentRunStatus = "RUNNING" | "COMPLETED" | "FAILED" | "CANCELLED";
export type AgentReviewStatus = "PENDING_REVIEW" | "APPROVED" | "REJECTED" | "FORWARDED_TO_POLICY_DRAFT";

export interface EvidenceCitation {
  snapshotId: string;
  sourceTitle: string;
  sourceUrl: string;
  contentHash: string;
  exactSpan: string;
}

export interface PolicyValidationIssue {
  ruleId: string | null;
  code: string;
  message: string;
}

export interface PolicyInvestigationResult {
  investigationId: string;
  status: AgentRunStatus;
  sourceCandidateId: string;
  schemeId: string;
  snapshotIds: string[];
  previousPolicyVersionId: string | null;
  proposedChangeSummary: string;
  proposedPolicyRule: Record<string, unknown> | null;
  evidenceCitations: EvidenceCitation[];
  unverifiedClaims: string[];
  validationIssues: PolicyValidationIssue[];
  reviewRequired: boolean;
  recommendedNextAction: string;
  deterministicFixture: boolean;
}

export interface PolicyAgentRun {
  id: string;
  candidateId: string;
  schemeId: string;
  schemeName: string;
  executionMode: AgentExecutionMode;
  status: AgentRunStatus;
  reviewStatus: AgentReviewStatus;
  reviewReason: string | null;
  reviewedAt: string | null;
  maxToolCalls: number;
  toolCallCount: number;
  maxModelCalls: number;
  modelCallCount: number;
  maxEstimatedTokens: number;
  estimatedTokensUsed: number;
  result: PolicyInvestigationResult | null;
  failureMessage: string | null;
  startedAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
}

export interface PolicyAgentEvent {
  id: string;
  sequenceNumber: number;
  eventType: "RUN_STARTED" | "TOOL_EXECUTED" | "VALIDATION_COMPLETED" | "RUN_COMPLETED" | "RUN_FAILED" | "RUN_CANCELLED";
  toolName: string | null;
  outcome: "SUCCESS" | "REJECTED" | "ERROR";
  durationMs: number | null;
  evidenceReferences: string[];
  details: Record<string, unknown>;
  recordedAt: string;
}
