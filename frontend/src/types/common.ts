export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface PageParams {
  page?: number
  size?: number
  sort?: string
  search?: string
  [key: string]: string | number | boolean | undefined
}

export type Role = 'ADMIN' | 'RECEPTIONIST' | 'INSTRUCTOR' | 'STUDENT'

export interface ApiError {
  message: string
  status?: number
  errors?: Record<string, string>
}
