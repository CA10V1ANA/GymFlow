import type { Role } from './common'

export interface Employee {
  id: string
  name: string
  email: string
  phone: string
  cpf: string
  role: Role
  active: boolean
  position: string
  hiredAt: string
  salary?: number
  createdAt: string
}

export interface EmployeePayload {
  name: string
  email: string
  password?: string
  role: Role
  phone: string
  position: string
  hiredAt: string
  salary?: number
  cpf: string
}
