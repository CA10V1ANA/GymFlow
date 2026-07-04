import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { workoutService } from '@/services/workoutService'
import type { PageParams } from '@/types/common'
import type { WorkoutPayload } from '@/types/workout'

const WORKOUTS_KEY = 'workouts'

export function useWorkouts(params?: PageParams) {
  return useQuery({
    queryKey: [WORKOUTS_KEY, params],
    queryFn: () => workoutService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useWorkout(id?: string) {
  return useQuery({
    queryKey: [WORKOUTS_KEY, id],
    queryFn: () => workoutService.getById(id as string),
    enabled: !!id,
  })
}

export function useCreateWorkout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: WorkoutPayload) => workoutService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WORKOUTS_KEY] })
      toast.success('Treino criado com sucesso.')
    },
    onError: () => toast.error('Não foi possível criar o treino.'),
  })
}

export function useUpdateWorkout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: WorkoutPayload }) =>
      workoutService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WORKOUTS_KEY] })
      toast.success('Treino atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o treino.'),
  })
}

export function useDeleteWorkout() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => workoutService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [WORKOUTS_KEY] })
      toast.success('Treino removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o treino.'),
  })
}
