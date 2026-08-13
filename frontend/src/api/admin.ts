import http from '../utils/http'
import type { ApiResponse, PageResponse, User } from '../types/api'
import type { AdminStatistics, FishProfileTask, RescueOrder, VolunteerApplication } from '../types/business'

export const getAdminStatisticsApi = () => http.get<ApiResponse<AdminStatistics>>('/admin/statistics/overview')
export const listAdminUsersApi = (params: object) => http.get<ApiResponse<PageResponse<User>>>('/admin/users', { params })
export const updateUserStatusApi = (id: number, status: string) => http.put<ApiResponse<User>>(`/admin/users/${id}/status`, { status })
export const listAdminVolunteerApplicationsApi = (params: object) => http.get<ApiResponse<PageResponse<VolunteerApplication>>>('/admin/volunteer-applications', { params })
export const reviewVolunteerApplicationApi = (id: number, status: string, reviewComment: string) => http.put<ApiResponse<VolunteerApplication>>(`/admin/volunteer-applications/${id}/review`, { status: status === 'APPROVE' ? 'APPROVED' : 'REJECTED', reviewComment })
export const listAdminRescuesApi = (params: object) => http.get<ApiResponse<PageResponse<RescueOrder>>>('/admin/rescue-orders', { params })
export const reviewRescueApi = (id: number, decision: string, reviewComment: string) => http.put<ApiResponse<RescueOrder>>(`/admin/rescue-orders/${id}/review`, { decision: decision === 'APPROVE' ? 'APPROVED' : 'REJECTED', reviewComment })
export const confirmRescueApi = (id: number, decision: string, confirmComment: string) => http.put<ApiResponse<RescueOrder>>(`/admin/rescue-orders/${id}/completion-confirmation`, { decision: decision === 'CONFIRM' ? 'CONFIRMED' : 'REJECTED', confirmComment })
export const listFishProfileTasksApi = (params: object) => http.get<ApiResponse<PageResponse<FishProfileTask>>>('/admin/fish-profile-tasks', { params })
export const reviewFishProfileTaskApi = (id: number, decision: string, fishId: number | null, reviewComment: string) => http.put<ApiResponse<FishProfileTask>>(`/admin/fish-profile-tasks/${id}/review`, { decision, fishId, reviewComment })
