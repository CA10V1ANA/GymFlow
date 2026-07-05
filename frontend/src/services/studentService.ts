import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { Student, StudentPayload } from '@/types/student'

interface StudentApiResponse {
  id: string
  name: string
  photoUrl?: string | null
  cpf: string
  rg?: string
  gender: 'MALE' | 'FEMALE' | 'OTHER'
  phone: string
  email: string
  zipCode: string
  address: string
  addressNumber?: string
  addressComplement?: string
  neighborhood?: string
  city?: string
  state?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  birthDate: string
  notes?: string
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
  registrationCode: string
  createdAt: string
  updatedAt: string
}

function normalizeStudent(data: StudentApiResponse): Student {
  return {
    id: data.id,
    name: data.name,
    photoUrl: data.photoUrl,
    cpf: data.cpf,
    rg: data.rg,
    gender: data.gender,
    phone: data.phone,
    email: data.email,
    zipCode: data.zipCode,
    address: data.address,
    addressNumber: data.addressNumber,
    neighborhood: data.neighborhood,
    city: data.city,
    state: data.state,
    emergencyContact: {
      name: data.emergencyContactName ?? '',
      phone: data.emergencyContactPhone ?? '',
    },
    birthDate: data.birthDate,
    notes: data.notes,
    status: data.status,
    registrationCode: data.registrationCode,
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  }
}

function toStudentRequest(payload: StudentPayload) {
  return {
    name: payload.name,
    photoUrl: payload.photoUrl,
    cpf: payload.cpf,
    rg: payload.rg,
    gender: payload.gender,
    phone: payload.phone,
    email: payload.email,
    password: payload.password || undefined,
    zipCode: payload.zipCode,
    address: payload.address,
    addressNumber: payload.addressNumber,
    neighborhood: payload.neighborhood,
    city: payload.city,
    state: payload.state,
    emergencyContactName: payload.emergencyContact?.name,
    emergencyContactPhone: payload.emergencyContact?.phone,
    birthDate: payload.birthDate,
    notes: payload.notes,
    status: payload.status,
  }
}

export const studentService = {
  list: async (params?: PageParams): Promise<Page<Student>> => {
    const { data } = await api.get<Page<StudentApiResponse>>('/students', { params })
    return { ...data, content: (data.content ?? []).map(normalizeStudent) }
  },
  getById: async (id: string): Promise<Student> => {
    const { data } = await api.get<StudentApiResponse>(`/students/${id}`)
    return normalizeStudent(data)
  },
  create: async (payload: StudentPayload): Promise<Student> => {
    const { data } = await api.post<StudentApiResponse>('/students', toStudentRequest(payload))
    return normalizeStudent(data)
  },
  update: async (id: string, payload: StudentPayload): Promise<Student> => {
    const { data } = await api.put<StudentApiResponse>(`/students/${id}`, toStudentRequest(payload))
    return normalizeStudent(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/students/${id}`)
  },
}
