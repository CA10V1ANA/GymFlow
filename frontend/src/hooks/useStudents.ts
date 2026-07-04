import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { studentService } from '@/services/studentService'
import type { PageParams } from '@/types/common'
import type { StudentPayload } from '@/types/student'

const STUDENTS_KEY = 'students'

export function useStudents(params?: PageParams) {
  return useQuery({
    queryKey: [STUDENTS_KEY, params],
    queryFn: () => studentService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useStudent(id?: string) {
  return useQuery({
    queryKey: [STUDENTS_KEY, id],
    queryFn: () => studentService.getById(id as string),
    enabled: !!id,
  })
}

export function useCreateStudent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: StudentPayload) => studentService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [STUDENTS_KEY] })
      toast.success('Aluno cadastrado com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar o aluno.'),
  })
}

export function useUpdateStudent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: StudentPayload }) =>
      studentService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [STUDENTS_KEY] })
      toast.success('Aluno atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o aluno.'),
  })
}

export function useDeleteStudent() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => studentService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [STUDENTS_KEY] })
      toast.success('Aluno removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o aluno.'),
  })
}
