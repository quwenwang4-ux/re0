import http from '../utils/http'
import type { ApiResponse, FishInfo, PageResponse } from '../types/api'

export interface FishQuery { page?: number; size?: number; keyword?: string; category?: string }
export const listFishesApi = (query: FishQuery) => http.get<ApiResponse<PageResponse<FishInfo>>>('/fishes', { params: query })
export const getFishApi = (id: number) => http.get<ApiResponse<FishInfo>>(`/fishes/${id}`)
