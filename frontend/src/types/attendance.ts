export interface Attendance {
  id: string
  studentId: string
  studentName: string
  checkInAt: string
  checkOutAt?: string | null
  method?: string
  permanenceMinutes?: number | null
}

export interface CheckInPayload {
  registrationCode: string
}
