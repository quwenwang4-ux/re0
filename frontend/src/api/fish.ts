import http from '../utils/http'
import type { ApiResponse, FishInfo, PageResponse } from '../types/api'

export interface FishQuery { page?: number; size?: number; keyword?: string; category?: string }
export const listFishesApi = (query: FishQuery) => http.get<ApiResponse<PageResponse<FishInfo>>>('/fishes', { params: query })
export const getFishApi = (id: number) => http.get<ApiResponse<FishInfo>>(`/fishes/${id}`)
export type FishForm = Omit<FishInfo, 'id' | 'createdBy' | 'createdAt' | 'updatedAt'>
export const createFishApi = (request: FishForm) => http.post<ApiResponse<FishInfo>>('/fishes', request)
export const updateFishApi = (id: number, request: FishForm) => http.put<ApiResponse<FishInfo>>(`/fishes/${id}`, request)
export const deleteFishApi = (id: number) => http.delete<ApiResponse<null>>(`/fishes/${id}`)
