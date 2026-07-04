import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { exerciseService } from '@/services/exerciseService'
import type { PageParams } from '@/types/common'
import type { ExercisePayload } from '@/types/exercise'

const EXERCISES_KEY = 'exercises'

export function useExercises(params?: PageParams) {
  return useQuery({
    queryKey: [EXERCISES_KEY, params],
    queryFn: () => exerciseService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateExercise() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: ExercisePayload) => exerciseService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EXERCISES_KEY] })
      toast.success('Exercício cadastrado com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar o exercício.'),
  })
}

export function useUpdateExercise() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: ExercisePayload }) =>
      exerciseService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EXERCISES_KEY] })
      toast.success('Exercício atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o exercício.'),
  })
}

export function useDeleteExercise() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => exerciseService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EXERCISES_KEY] })
      toast.success('Exercício removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o exercício.'),
  })
}
