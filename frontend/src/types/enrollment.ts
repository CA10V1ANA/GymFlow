export type EnrollmentStatus = 'ACTIVE' | 'CANCELLED' | 'FROZEN' | 'EXPIRED'

export interface Enrollment {
  id: string
  studentId: string
  studentName: string
  planId: string
  planName: string
  startDate: string
  endDate: string
  status: EnrollmentStatus
  createdAt: string
  updatedAt: string
}

export interface EnrollmentPayload {
  studentId: string
  planId: string
  startDate: string
}

export interface CancelEnrollmentPayload {
  reason: string
}
