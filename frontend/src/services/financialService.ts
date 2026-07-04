import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { FinancialTransaction, FinancialTransactionPayload } from '@/types/financial'

interface FinancialApiResponse {
  id: string
  type: FinancialTransaction['type']
  category: string
  description: string
  amount: number
  netAmount?: number
  paymentMethod?: string
  status: FinancialTransaction['status']
  dueDate: string
  paidAt?: string | null
  student?: { id: string; name: string } | null
  createdAt: string
}

function normalizeTransaction(data: FinancialApiResponse): FinancialTransaction {
  return {
    id: data.id,
    description: data.description,
    type: data.type,
    status: data.status,
    amount: data.amount,
    dueDate: data.dueDate,
    paidAt: data.paidAt,
    studentId: data.student?.id ?? null,
    studentName: data.student?.name ?? null,
    category: data.category,
    paymentMethod: data.paymentMethod,
    netAmount: data.netAmount,
    createdAt: data.createdAt,
  }
}

export const financialService = {
  list: async (params?: PageParams): Promise<Page<FinancialTransaction>> => {
    const { data } = await api.get<Page<FinancialApiResponse>>('/financial/transactions', { params })
    return { ...data, content: (data.content ?? []).map(normalizeTransaction) }
  },
  create: async (payload: FinancialTransactionPayload): Promise<FinancialTransaction> => {
    const { data } = await api.post<FinancialApiResponse>('/financial/transactions', payload)
    return normalizeTransaction(data)
  },
  markAsPaid: async (id: string): Promise<FinancialTransaction> => {
    const { data } = await api.post<FinancialApiResponse>(`/financial/transactions/${id}/pay`, {
      paymentMethod: 'PIX',
    })
    return normalizeTransaction(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.post(`/financial/transactions/${id}/cancel`)
  },
}
