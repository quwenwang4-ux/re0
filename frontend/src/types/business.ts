export interface DetectionResult {
  id: number; fishId: number | null; className: string; confidence: number
  chineseName: string | null; scientificName: string | null; category: string | null
  habits: string | null; habitat: string | null; distribution: string | null
  protectionLevel: string | null; fishProfileMissing: boolean
}
export interface DetectionRecord {
  id: number; originalFileName: string; modelName: string; confidenceThreshold: number
  status: string; errorCode: string | null; errorMessage: string | null
  originalImageUrl: string; resultImageUrl: string | null
  durationMs: number | null; createdAt: string; results: DetectionResult[]
}
export interface RescueOrder {
  id: number; reporterId?: number; issueType: string; title: string; description: string
  locationText: string | null; latitude: number | null; longitude: number | null; status: string
  reviewerId?: number | null; reviewComment?: string | null; volunteerId?: number | null
  completionDescription?: string | null; confirmComment?: string | null; createdAt?: string
  publishedAt?: string | null; imageUrls?: string[]
}
export interface VolunteerApplication {
  id: number; applicantId: number; realName: string; phone: string; region: string
  skills: string | null; reason: string; status: string; reviewerId: number | null
  reviewComment: string | null; reviewedAt: string | null; createdAt: string
}
export interface LabelCount { label: string; count: number }
export interface AdminStatistics {
  activeUserCount: number; detectionCount: number; rescueOrderCount: number
  completedRescueOrderCount: number; usersByRole: LabelCount[]; detectionsByStatus: LabelCount[]
  topDetectedClasses: LabelCount[]; rescueOrdersByStatus: LabelCount[]
  volunteerAcceptedCounts: LabelCount[]; volunteerCompletedCounts: LabelCount[]
}
export interface FishInfoApplication {
  id: number; applicantId: number; applicationType: 'ADD' | 'CORRECTION'; targetFishId: number | null
  chineseName: string; scientificName: string | null; category: string | null; appearance: string | null
  habits: string | null; habitat: string | null; distribution: string | null; protectionLevel: string | null
  coverImageUrl: string | null; sourceDescription: string; reason: string; status: string
  reviewerId: number | null; reviewComment: string | null; createdAt: string
}
export interface FishProfileTask {
  id: number; detectionResultId: number; className: string; status: string; linkedFishId: number | null
  reviewerId: number | null; reviewComment: string | null; reviewedAt: string | null; createdAt: string
}
