import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { financialService } from '@/services/financialService'
import type { PageParams } from '@/types/common'
import type { FinancialTransactionPayload } from '@/types/financial'

const FINANCIAL_KEY = 'financial-transactions'

export function useFinancialTransactions(params?: PageParams) {
  return useQuery({
    queryKey: [FINANCIAL_KEY, params],
    queryFn: () => financialService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateFinancialTransaction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: FinancialTransactionPayload) => financialService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [FINANCIAL_KEY] })
      toast.success('Lançamento criado com sucesso.')
    },
    onError: () => toast.error('Não foi possível criar o lançamento.'),
  })
}

export function useMarkTransactionAsPaid() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => financialService.markAsPaid(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [FINANCIAL_KEY] })
      toast.success('Lançamento marcado como pago.')
    },
    onError: () => toast.error('Não foi possível atualizar o lançamento.'),
  })
}

export function useDeleteFinancialTransaction() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => financialService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [FINANCIAL_KEY] })
      toast.success('Lançamento removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o lançamento.'),
  })
}
