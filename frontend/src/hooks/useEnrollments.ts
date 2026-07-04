import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { enrollmentService } from '@/services/enrollmentService'
import type { PageParams } from '@/types/common'
import type { CancelEnrollmentPayload, EnrollmentPayload } from '@/types/enrollment'

const ENROLLMENTS_KEY = 'enrollments'

export function useEnrollments(params?: PageParams) {
  return useQuery({
    queryKey: [ENROLLMENTS_KEY, params],
    queryFn: () => enrollmentService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateEnrollment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: EnrollmentPayload) => enrollmentService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLMENTS_KEY] })
      toast.success('Matrícula criada com sucesso.')
    },
    onError: () => toast.error('Não foi possível criar a matrícula.'),
  })
}

export function useRenewEnrollment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enrollmentService.renew(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLMENTS_KEY] })
      toast.success('Matrícula renovada com sucesso.')
    },
    onError: () => toast.error('Não foi possível renovar a matrícula.'),
  })
}

export function useCancelEnrollment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: CancelEnrollmentPayload }) =>
      enrollmentService.cancel(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLMENTS_KEY] })
      toast.success('Matrícula cancelada.')
    },
    onError: () => toast.error('Não foi possível cancelar a matrícula.'),
  })
}

export function useFreezeEnrollment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enrollmentService.freeze(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLMENTS_KEY] })
      toast.success('Matrícula congelada.')
    },
    onError: () => toast.error('Não foi possível congelar a matrícula.'),
  })
}

export function useReactivateEnrollment() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => enrollmentService.reactivate(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [ENROLLMENTS_KEY] })
      toast.success('Matrícula reativada.')
    },
    onError: () => toast.error('Não foi possível reativar a matrícula.'),
  })
}
