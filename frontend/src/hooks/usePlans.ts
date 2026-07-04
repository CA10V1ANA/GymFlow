import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { planService } from '@/services/planService'
import type { PageParams } from '@/types/common'
import type { PlanPayload } from '@/types/plan'

const PLANS_KEY = 'plans'

export function usePlans(params?: PageParams) {
  return useQuery({
    queryKey: [PLANS_KEY, params],
    queryFn: () => planService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreatePlan() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: PlanPayload) => planService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLANS_KEY] })
      toast.success('Plano criado com sucesso.')
    },
    onError: () => toast.error('Não foi possível criar o plano.'),
  })
}

export function useUpdatePlan() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: PlanPayload }) =>
      planService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLANS_KEY] })
      toast.success('Plano atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o plano.'),
  })
}

export function useDeletePlan() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => planService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [PLANS_KEY] })
      toast.success('Plano removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o plano.'),
  })
}
