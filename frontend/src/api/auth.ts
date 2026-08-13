import http from '../utils/http'
import type { ApiResponse, CurrentUserResponse, LoginResponse, User } from '../types/api'

export interface LoginRequest { username: string; password: string }
export interface RegisterRequest { username: string; password: string; nickname?: string; email?: string; phone?: string }
export interface UpdateProfileRequest { nickname?: string; email?: string; phone?: string; avatarUrl?: string }

export const loginApi = (request: LoginRequest) => http.post<ApiResponse<LoginResponse>>('/auth/login', request)
export const registerApi = (request: RegisterRequest) => http.post<ApiResponse<User>>('/auth/register', request)
export const getCurrentUserApi = () => http.get<ApiResponse<CurrentUserResponse>>('/users/me')
export const updateProfileApi = (request: UpdateProfileRequest) => http.put<ApiResponse<User>>('/users/me', request)
