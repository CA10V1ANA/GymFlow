export type TransactionType = 'INCOME' | 'EXPENSE'
export type TransactionStatus = 'PENDING' | 'PAID' | 'OVERDUE' | 'CANCELLED'

export interface FinancialTransaction {
  id: string
  description: string
  type: TransactionType
  status: TransactionStatus
  amount: number
  dueDate: string
  paidAt?: string | null
  studentId?: string | null
  studentName?: string | null
  category?: string
  paymentMethod?: string
  netAmount?: number
  createdAt: string
}

export type FinancialTransactionPayload = Omit<
  FinancialTransaction,
  'id' | 'createdAt' | 'studentName' | 'paidAt' | 'netAmount'
>
