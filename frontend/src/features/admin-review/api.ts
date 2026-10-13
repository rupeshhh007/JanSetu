import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import type { AdminAuditRecord, AgentExecutionMode, PolicyAgentEvent, PolicyAgentRun, PolicyDraftRequest, PolicyVersion, SourceChangeCandidate, SourceChangeReviewRequest } from "./types";

export async function getPendingSourceChanges() {
  return (await axiosClient.get<SourceChangeCandidate[]>(API_ENDPOINTS.ADMIN_SOURCE_CHANGES)).data;
}
export async function getSourceChangeHistory() {
  return (await axiosClient.get<SourceChangeCandidate[]>(`${API_ENDPOINTS.ADMIN_SOURCE_CHANGES}/history`)).data;
}
export async function getAdminAuditHistory() {
  return (await axiosClient.get<AdminAuditRecord[]>(API_ENDPOINTS.ADMIN_AUDIT)).data;
}
export async function acceptSourceChange(candidateId: string, request: SourceChangeReviewRequest) {
  return (await axiosClient.post<SourceChangeCandidate>(`${API_ENDPOINTS.ADMIN_SOURCE_CHANGES}/${candidateId}/accept`, request)).data;
}
export async function rejectSourceChange(candidateId: string, request: SourceChangeReviewRequest) {
  return (await axiosClient.post<SourceChangeCandidate>(`${API_ENDPOINTS.ADMIN_SOURCE_CHANGES}/${candidateId}/reject`, request)).data;
}
export async function createPolicyDraft(candidateId: string, request: PolicyDraftRequest) {
  return (await axiosClient.post<PolicyVersion>(`${API_ENDPOINTS.ADMIN_SOURCE_CHANGES}/${candidateId}/policy-draft`, request)).data;
}
export async function getPolicyVersions(schemeId: string) {
  return (await axiosClient.get<PolicyVersion[]>(`/api/v1/admin/schemes/${schemeId}/policy-versions`)).data;
}
export async function transitionPolicy(schemeId: string, policyVersionId: string,
  action: "submit-review" | "approve" | "publish", reason: string) {
  return (await axiosClient.post<PolicyVersion>(
    `/api/v1/admin/schemes/${schemeId}/policy-versions/${policyVersionId}/${action}`, { reason })).data;
}

export async function getPolicyInvestigations() {
  return (await axiosClient.get<PolicyAgentRun[]>(API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS)).data;
}

export async function getPolicyInvestigationEvents(runId: string) {
  return (await axiosClient.get<PolicyAgentEvent[]>(`${API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS}/${runId}/events`)).data;
}

export async function startPolicyInvestigation(candidateId: string, schemeId: string, executionMode: AgentExecutionMode) {
  return (await axiosClient.post<PolicyAgentRun>(API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS,
    { candidateId, schemeId, executionMode }, { headers: { "Idempotency-Key": crypto.randomUUID() } })).data;
}

export async function retryPolicyInvestigation(runId: string, executionMode: AgentExecutionMode) {
  return (await axiosClient.post<PolicyAgentRun>(`${API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS}/${runId}/retry`, null,
    { params: { executionMode }, headers: { "Idempotency-Key": crypto.randomUUID() } })).data;
}

export async function cancelPolicyInvestigation(runId: string) {
  return (await axiosClient.post<PolicyAgentRun>(`${API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS}/${runId}/cancel`)).data;
}

export async function reviewPolicyInvestigation(runId: string, decision: "APPROVE" | "REJECT", reason: string,
  proposedPolicyRule?: Record<string, unknown>) {
  return (await axiosClient.post<PolicyAgentRun>(`${API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS}/${runId}/review`,
    { decision, reason, proposedPolicyRule })).data;
}

export async function forwardInvestigationToPolicyDraft(runId: string, effectiveFrom: string) {
  return (await axiosClient.post<PolicyVersion>(`${API_ENDPOINTS.ADMIN_AI_INVESTIGATIONS}/${runId}/policy-draft`,
    { effectiveFrom })).data;
}
