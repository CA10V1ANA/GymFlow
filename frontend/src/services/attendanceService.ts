import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { Attendance, CheckInPayload } from '@/types/attendance'

interface AttendanceApiResponse {
  id: string
  student: { id: string; name: string }
  checkIn: string
  checkOut?: string | null
  method?: string
  permanenceMinutes?: number | null
}

function normalizeAttendance(data: AttendanceApiResponse): Attendance {
  return {
    id: data.id,
    studentId: data.student.id,
    studentName: data.student.name,
    checkInAt: data.checkIn,
    checkOutAt: data.checkOut,
    method: data.method,
    permanenceMinutes: data.permanenceMinutes,
  }
}

export const attendanceService = {
  list: async (params?: PageParams): Promise<Page<Attendance>> => {
    const { data } = await api.get<Page<AttendanceApiResponse>>('/attendances', { params })
    return { ...data, content: (data.content ?? []).map(normalizeAttendance) }
  },
  listMine: async (): Promise<Attendance[]> => {
    const { data } = await api.get<AttendanceApiResponse[]>('/attendances/me')
    return data.map(normalizeAttendance)
  },
  myFrequency: async (): Promise<{ daily: number; monthly: number }> => {
    const { data } = await api.get<{ daily: number; monthly: number }>('/attendances/me/frequency')
    return data
  },
  checkIn: async (payload: CheckInPayload): Promise<Attendance> => {
    const { data } = await api.post<AttendanceApiResponse>('/attendances/check-in', payload)
    return normalizeAttendance(data)
  },
  checkOut: async (id: string): Promise<Attendance> => {
    const { data } = await api.post<AttendanceApiResponse>(`/attendances/${id}/check-out`)
    return normalizeAttendance(data)
  },
}
