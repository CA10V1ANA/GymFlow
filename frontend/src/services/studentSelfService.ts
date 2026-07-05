import { api } from '@/services/api'
import type { StudentSelfProfile, StudentSelfUpdatePayload } from '@/types/studentSelf'

export const studentSelfService = {
  getMe: async (): Promise<StudentSelfProfile> => {
    const { data } = await api.get<StudentSelfProfile>('/students/me')
    return data
  },
  updateMe: async (payload: StudentSelfUpdatePayload): Promise<StudentSelfProfile> => {
    const { data } = await api.put<StudentSelfProfile>('/students/me', payload)
    return data
  },
}
