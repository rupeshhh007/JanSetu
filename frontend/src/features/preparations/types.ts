export type PreparationStatus = "PREPARING" | "JANSETU_PREPARATION_READY" | "CITIZEN_REPORTED_APPLICATION";
export type PreparationTaskStatus = "COMPLETE" | "INCOMPLETE" | "CONDITIONAL" | "UNKNOWN";
export type PreparationTaskType = "DOCUMENT" | "ELIGIBILITY" | "PROFILE_FACT" | "UNKNOWN_REQUIREMENT";

export interface PreparationEvidenceReference {
  snapshotId: string | null;
  sourceTitle: string | null;
  sourceUrl: string | null;
  contentHash: string | null;
  excerpt: string | null;
}

export interface PreparationTask {
  id: string;
  taskKey: string;
  type: PreparationTaskType;
  status: PreparationTaskStatus;
  title: string;
  guidance: string | null;
  citizenCompletable: boolean;
  evidenceReferences: PreparationEvidenceReference[];
  completedAt: string | null;
}

export interface PreparationHistory {
  id: string;
  eventType: "JOURNEY_STARTED" | "TASK_COMPLETED" | "TASK_REOPENED" | "PREPARATION_READY" | "CITIZEN_REPORTED_APPLICATION" | "REQUIREMENTS_REFRESHED";
  taskKey: string | null;
  actorEmail: string;
  details: Record<string, string>;
  recordedAt: string;
}

export interface ApplicationPreparation {
  id: string;
  schemeId: string;
  schemeName: string;
  officialUrl: string | null;
  sourceName: string | null;
  status: PreparationStatus;
  officialApplicationStatus: string | null;
  policyVersionId: string | null;
  policyVersionNumber: number | null;
  completedTasks: number;
  totalTasks: number;
  progressPercent: number;
  tasks: PreparationTask[];
  history: PreparationHistory[];
  readyAt: string | null;
  citizenReportedAppliedAt: string | null;
  createdAt: string;
  updatedAt: string;
}
