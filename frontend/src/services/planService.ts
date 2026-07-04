import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { Plan, PlanPayload } from '@/types/plan'

interface PlanApiResponse {
  id: string
  name: string
  type: Plan['type']
  durationMonths: number
  price: number
  discountPercentage?: number
  description?: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export const planService = {
  list: async (params?: PageParams): Promise<Page<Plan>> => {
    const { data } = await api.get<Page<PlanApiResponse>>('/plans', { params })
    return data
  },
  create: async (payload: PlanPayload): Promise<Plan> => {
    const { data } = await api.post<PlanApiResponse>('/plans', payload)
    return data
  },
  update: async (id: string, payload: PlanPayload): Promise<Plan> => {
    const { data } = await api.put<PlanApiResponse>(`/plans/${id}`, payload)
    return data
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/plans/${id}`)
  },
}
