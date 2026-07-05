export interface StudentSelfProfile {
  id: string
  name: string
  photoUrl?: string | null
  cpf: string
  rg?: string
  gender?: string
  phone: string
  email: string
  zipCode?: string
  address?: string
  addressNumber?: string
  addressComplement?: string
  neighborhood?: string
  city?: string
  state?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
  birthDate: string
  notes?: string
  status: string
  registrationCode: string
  createdAt: string
  updatedAt: string
}

export interface StudentSelfUpdatePayload {
  phone: string
  email: string
  zipCode?: string
  address?: string
  addressNumber?: string
  addressComplement?: string
  neighborhood?: string
  city?: string
  state?: string
  emergencyContactName?: string
  emergencyContactPhone?: string
}
