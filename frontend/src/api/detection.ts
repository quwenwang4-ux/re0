import http from '../utils/http'
import type { ApiResponse, PageResponse } from '../types/api'
import type { DetectionRecord } from '../types/business'

export async function detectFishApi(image: File, confidenceThreshold = 0.5) {
  const form = new FormData(); form.append('image', image)
  return http.post<ApiResponse<DetectionRecord>>('/detections', form, { params: { confidenceThreshold }, headers: { 'Content-Type': 'multipart/form-data' }, timeout: 60_000 })
}
export const listDetectionRecordsApi = (page = 1, size = 10) => http.get<ApiResponse<PageResponse<DetectionRecord>>>('/detections/me', { params: { page, size } })
export const getDetectionRecordApi = (id: number) => http.get<ApiResponse<DetectionRecord>>(`/detections/${id}`)
