import http from '../utils/http'
import type { ApiResponse, PageResponse } from '../types/api'
import type { FishInfoApplication } from '../types/business'

export type FishApplicationForm = Omit<FishInfoApplication, 'id'|'applicantId'|'status'|'reviewerId'|'reviewComment'|'createdAt'>
export const createFishApplicationApi = (request: FishApplicationForm) => http.post<ApiResponse<FishInfoApplication>>('/fish-applications', request)
export const listMyFishApplicationsApi = () => http.get<ApiResponse<PageResponse<FishInfoApplication>>>('/fish-applications/me', { params: { page: 1, size: 20 } })
export const listAdminFishApplicationsApi = (params: object) => http.get<ApiResponse<PageResponse<FishInfoApplication>>>('/admin/fish-applications', { params })
export const reviewFishApplicationApi = (id: number, decision: string, reviewComment: string) => http.put<ApiResponse<FishInfoApplication>>(`/admin/fish-applications/${id}/review`, { decision, reviewComment })
