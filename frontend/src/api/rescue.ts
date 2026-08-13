import http from '../utils/http'
import type { ApiResponse, PageResponse } from '../types/api'
import type { RescueOrder, VolunteerApplication } from '../types/business'

export interface CreateRescueRequest { issueType: string; title: string; description: string; locationText?: string; latitude?: number; longitude?: number; imageUrls?: string[] }
export const listPublicRescuesApi = (page = 1, size = 12) => http.get<ApiResponse<PageResponse<RescueOrder>>>('/rescue-orders/public', { params: { page, size } })
export const listMyRescuesApi = (page = 1, size = 10) => http.get<ApiResponse<PageResponse<RescueOrder>>>('/rescue-orders/me', { params: { page, size } })
export const createRescueApi = (request: CreateRescueRequest) => http.post<ApiResponse<RescueOrder>>('/rescue-orders', request)
export const createVolunteerApplicationApi = (request: { realName: string; phone: string; region: string; skills?: string; reason: string }) => http.post<ApiResponse<VolunteerApplication>>('/volunteer-applications', request)
export const listMyVolunteerApplicationsApi = () => http.get<ApiResponse<VolunteerApplication[]>>('/volunteer-applications/me')
export const listVolunteerOrdersApi = (page = 1, size = 10) => http.get<ApiResponse<PageResponse<RescueOrder>>>('/volunteer/rescue-orders/me', { params: { page, size } })
export const acceptRescueApi = (id: number) => http.put<ApiResponse<RescueOrder>>(`/volunteer/rescue-orders/${id}/accept`)
export const completeRescueApi = (id: number, completionDescription: string, imageUrls: string[] = []) => http.put<ApiResponse<RescueOrder>>(`/volunteer/rescue-orders/${id}/completion`, { completionDescription, imageUrls })
export async function uploadRescueImageApi(image: File) {
  const form = new FormData(); form.append('image', image)
  return http.post<ApiResponse<{ fileName:string; url:string }>>('/files/rescue-images', form, { headers:{ 'Content-Type':'multipart/form-data' } })
}
