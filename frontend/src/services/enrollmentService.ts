import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { CancelEnrollmentPayload, Enrollment, EnrollmentPayload } from '@/types/enrollment'

interface EnrollmentApiResponse {
  id: string
  student: { id: string; name: string }
  plan: { id: string; name: string }
  startDate: string
  endDate: string
  status: Enrollment['status']
  createdAt: string
}

function normalizeEnrollment(data: EnrollmentApiResponse): Enrollment {
  return {
    id: data.id,
    studentId: data.student.id,
    studentName: data.student.name,
    planId: data.plan.id,
    planName: data.plan.name,
    startDate: data.startDate,
    endDate: data.endDate,
    status: data.status,
    createdAt: data.createdAt,
    updatedAt: data.createdAt,
  }
}

const base = {
  list: async (params?: PageParams): Promise<Page<Enrollment>> => {
    const { data } = await api.get<Page<EnrollmentApiResponse>>('/enrollments', { params })
    return { ...data, content: (data.content ?? []).map(normalizeEnrollment) }
  },
  create: async (payload: EnrollmentPayload): Promise<Enrollment> => {
    const { data } = await api.post<EnrollmentApiResponse>('/enrollments', payload)
    return normalizeEnrollment(data)
  },
}

export const enrollmentService = {
  ...base,
  renew: async (id: string): Promise<Enrollment> => {
    const { data } = await api.post<EnrollmentApiResponse>(`/enrollments/${id}/renew`)
    return normalizeEnrollment(data)
  },
  cancel: async (id: string, payload: CancelEnrollmentPayload): Promise<Enrollment> => {
    const { data } = await api.post<EnrollmentApiResponse>(`/enrollments/${id}/cancel`, payload)
    return normalizeEnrollment(data)
  },
  freeze: async (id: string): Promise<Enrollment> => {
    const { data } = await api.post<EnrollmentApiResponse>(`/enrollments/${id}/freeze`)
    return normalizeEnrollment(data)
  },
  reactivate: async (id: string): Promise<Enrollment> => {
    const { data } = await api.post<EnrollmentApiResponse>(`/enrollments/${id}/reactivate`)
    return normalizeEnrollment(data)
  },
}
