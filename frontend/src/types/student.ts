export type StudentStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED'
export type Gender = 'MALE' | 'FEMALE' | 'OTHER'

export interface EmergencyContact {
  name: string
  phone: string
  relationship?: string
}

export interface Student {
  id: string
  name: string
  photoUrl?: string | null
  cpf: string
  rg?: string
  gender: Gender
  phone: string
  email: string
  zipCode: string
  address: string
  addressNumber?: string
  neighborhood?: string
  city?: string
  state?: string
  emergencyContact: EmergencyContact
  birthDate: string
  notes?: string
  status: StudentStatus
  registrationCode: string
  createdAt: string
  updatedAt: string
}

export type StudentPayload = Omit<Student, 'id' | 'createdAt' | 'updatedAt' | 'registrationCode'>
