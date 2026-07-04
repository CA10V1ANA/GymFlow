import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { employeeService } from '@/services/employeeService'
import type { PageParams } from '@/types/common'
import type { EmployeePayload } from '@/types/employee'

const EMPLOYEES_KEY = 'employees'

export function useEmployees(params?: PageParams) {
  return useQuery({
    queryKey: [EMPLOYEES_KEY, params],
    queryFn: () => employeeService.list(params),
    placeholderData: (prev) => prev,
  })
}

export function useCreateEmployee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload: EmployeePayload) => employeeService.create(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EMPLOYEES_KEY] })
      toast.success('Funcionário cadastrado com sucesso.')
    },
    onError: () => toast.error('Não foi possível cadastrar o funcionário.'),
  })
}

export function useUpdateEmployee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }: { id: string; payload: EmployeePayload }) =>
      employeeService.update(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EMPLOYEES_KEY] })
      toast.success('Funcionário atualizado com sucesso.')
    },
    onError: () => toast.error('Não foi possível atualizar o funcionário.'),
  })
}

export function useDeleteEmployee() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id: string) => employeeService.remove(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: [EMPLOYEES_KEY] })
      toast.success('Funcionário removido com sucesso.')
    },
    onError: () => toast.error('Não foi possível remover o funcionário.'),
  })
}
