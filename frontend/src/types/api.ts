export interface ApiResponse<T> { code: number; message: string; data: T }
export interface PageResponse<T> { records: T[]; total: number; page: number; size: number; pages: number }
export interface User {
  id: number; username: string; nickname: string | null; email: string | null
  phone: string | null; avatarUrl: string | null; status: 'ACTIVE' | 'DISABLED'; createdAt: string
}
export interface LoginResponse { accessToken: string; tokenType: string; expiresInSeconds: number; user: User; roles: string[] }
export interface CurrentUserResponse { user: User; roles: string[] }
export interface FishInfo {
  id: number; chineseName: string; scientificName: string | null; category: string | null
  appearance: string | null; habits: string | null; habitat: string | null; distribution: string | null
  protectionLevel: string | null; coverImageUrl: string | null; sourceType: string
  sourceDescription: string | null; createdBy: number | null; createdAt: string; updatedAt: string
}
