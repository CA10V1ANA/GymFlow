export type PlanType = 'MONTHLY' | 'QUARTERLY' | 'SEMIANNUAL' | 'ANNUAL' | 'CUSTOM'

export interface Plan {
  id: string
  name: string
  type: PlanType
  description?: string
  price: number
  durationMonths: number
  discountPercentage?: number
  active: boolean
  createdAt: string
  updatedAt: string
}

export type PlanPayload = Omit<Plan, 'id' | 'createdAt' | 'updatedAt'>
