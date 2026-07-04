import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { Employee, EmployeePayload } from '@/types/employee'

interface EmployeeApiResponse {
  id: string
  user: {
    name: string
    email: string
    role: 'ADMIN' | 'RECEPTIONIST' | 'INSTRUCTOR' | 'STUDENT'
    phone?: string
    active: boolean
    createdAt: string
  }
  position: string
  hiredAt: string
  salary?: number
  cpf: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
}

function normalizeEmployee(data: EmployeeApiResponse): Employee {
  return {
    id: data.id,
    name: data.user.name,
    email: data.user.email,
    phone: data.user.phone ?? '',
    cpf: data.cpf,
    role: data.user.role,
    active: data.user.active,
    position: data.position,
    hiredAt: data.hiredAt,
    salary: data.salary,
    createdAt: data.user.createdAt,
  }
}

function toEmployeeRequest(payload: EmployeePayload) {
  return {
    name: payload.name,
    email: payload.email,
    password: payload.password,
    role: payload.role,
    phone: payload.phone,
    position: payload.position,
    hiredAt: payload.hiredAt,
    salary: payload.salary,
    cpf: payload.cpf,
  }
}

export const employeeService = {
  list: async (params?: PageParams): Promise<Page<Employee>> => {
    const { data } = await api.get<Page<EmployeeApiResponse>>('/employees', { params })
    return { ...data, content: (data.content ?? []).map(normalizeEmployee) }
  },
  create: async (payload: EmployeePayload): Promise<Employee> => {
    const { data } = await api.post<EmployeeApiResponse>('/employees', toEmployeeRequest(payload))
    return normalizeEmployee(data)
  },
  update: async (id: string, payload: EmployeePayload): Promise<Employee> => {
    const { data } = await api.put<EmployeeApiResponse>(`/employees/${id}`, toEmployeeRequest(payload))
    return normalizeEmployee(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/employees/${id}`)
  },
}
