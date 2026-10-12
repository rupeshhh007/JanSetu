import { axiosClient } from "@/lib/api/axiosClient";
import { API_ENDPOINTS } from "@/lib/api/endpoints";
import type { AdminAuditRecord, PolicyDraftRequest, PolicyVersion, SourceChangeCandidate, SourceChangeReviewRequest } from "./types";

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
