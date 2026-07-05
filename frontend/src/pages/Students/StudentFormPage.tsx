import { useEffect, type ReactNode } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { Loader2, Save } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useCreateStudent, useStudent, useUpdateStudent } from '@/hooks/useStudents'
import type { StudentPayload } from '@/types/student'

const studentSchema = z.object({
  name: z.string().min(3, 'Informe o nome completo.'),
  photoUrl: z.string().url('URL inválida.').optional().or(z.literal('')),
  cpf: z.string().min(11, 'CPF inválido.'),
  rg: z.string().optional(),
  gender: z.enum(['MALE', 'FEMALE', 'OTHER']),
  phone: z.string().min(8, 'Telefone inválido.'),
  email: z.string().email('Email inválido.'),
  password: z.string().optional(),
  zipCode: z.string().min(8, 'CEP inválido.'),
  address: z.string().min(3, 'Informe o endereço.'),
  addressNumber: z.string().optional(),
  neighborhood: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  emergencyContactName: z.string().min(2, 'Informe o contato de emergência.'),
  emergencyContactPhone: z.string().min(8, 'Telefone inválido.'),
  birthDate: z.string().min(1, 'Informe a data de nascimento.'),
  notes: z.string().optional(),
  status: z.enum(['ACTIVE', 'INACTIVE', 'SUSPENDED']),
})

type StudentFormData = z.infer<typeof studentSchema>

const defaultValues: StudentFormData = {
  name: '',
  photoUrl: '',
  cpf: '',
  rg: '',
  gender: 'MALE',
  phone: '',
  email: '',
  password: '',
  zipCode: '',
  address: '',
  addressNumber: '',
  neighborhood: '',
  city: '',
  state: '',
  emergencyContactName: '',
  emergencyContactPhone: '',
  birthDate: '',
  notes: '',
  status: 'ACTIVE',
}

export function StudentFormPage() {
  const { id } = useParams()
  const isEditing = !!id
  const navigate = useNavigate()
  const { data: student, isLoading } = useStudent(id)
  const createStudent = useCreateStudent()
  const updateStudent = useUpdateStudent()

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<StudentFormData>({ resolver: zodResolver(studentSchema), defaultValues })

  useEffect(() => {
    if (student) {
      reset({
        name: student.name,
        photoUrl: student.photoUrl ?? '',
        cpf: student.cpf,
        rg: student.rg ?? '',
        gender: student.gender,
        phone: student.phone,
        email: student.email,
        password: '',
        zipCode: student.zipCode,
        address: student.address,
        addressNumber: student.addressNumber ?? '',
        neighborhood: student.neighborhood ?? '',
        city: student.city ?? '',
        state: student.state ?? '',
        emergencyContactName: student.emergencyContact?.name ?? '',
        emergencyContactPhone: student.emergencyContact?.phone ?? '',
        birthDate: student.birthDate?.slice(0, 10) ?? '',
        notes: student.notes ?? '',
        status: student.status,
      })
    }
  }, [student, reset])

  async function onSubmit(data: StudentFormData) {
    const payload: StudentPayload = {
      name: data.name,
      photoUrl: data.photoUrl || undefined,
      cpf: data.cpf,
      rg: data.rg,
      gender: data.gender,
      phone: data.phone,
      email: data.email,
      password: data.password || undefined,
      zipCode: data.zipCode,
      address: data.address,
      addressNumber: data.addressNumber,
      neighborhood: data.neighborhood,
      city: data.city,
      state: data.state,
      emergencyContact: {
        name: data.emergencyContactName,
        phone: data.emergencyContactPhone,
      },
      birthDate: data.birthDate,
      notes: data.notes,
      status: data.status,
    }

    if (isEditing && id) {
      await updateStudent.mutateAsync({ id, payload })
    } else {
      await createStudent.mutateAsync(payload)
    }
    navigate('/students')
  }

  if (isEditing && isLoading) {
    return <p className="text-sm text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="space-y-6">
      <PageHeader title={isEditing ? 'Editar aluno' : 'Novo aluno'} />

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Dados pessoais</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <Field label="Nome completo" error={errors.name?.message} className="sm:col-span-2">
              <Input {...register('name')} />
            </Field>
            <Field label="Foto (URL)" error={errors.photoUrl?.message}>
              <Input {...register('photoUrl')} placeholder="https://..." />
            </Field>
            <Field label="CPF" error={errors.cpf?.message}>
              <Input {...register('cpf')} placeholder="000.000.000-00" />
            </Field>
            <Field label="RG" error={errors.rg?.message}>
              <Input {...register('rg')} />
            </Field>
            <Field label="Sexo" error={errors.gender?.message}>
              <Select value={watch('gender')} onValueChange={(value) => setValue('gender', value as StudentFormData['gender'])}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MALE">Masculino</SelectItem>
                  <SelectItem value="FEMALE">Feminino</SelectItem>
                  <SelectItem value="OTHER">Outro</SelectItem>
                </SelectContent>
              </Select>
            </Field>
            <Field label="Data de nascimento" error={errors.birthDate?.message}>
              <Input type="date" {...register('birthDate')} />
            </Field>
            <Field label="Telefone" error={errors.phone?.message}>
              <Input {...register('phone')} placeholder="(00) 00000-0000" />
            </Field>
            <Field label="Email" error={errors.email?.message}>
              <Input type="email" {...register('email')} />
            </Field>
            <Field label={isEditing ? 'Nova senha de acesso' : 'Senha de acesso'} error={errors.password?.message}>
              <Input type="password" {...register('password')} />
            </Field>
            <Field label="Status" error={errors.status?.message}>
              <Select value={watch('status')} onValueChange={(value) => setValue('status', value as StudentFormData['status'])}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ACTIVE">Ativo</SelectItem>
                  <SelectItem value="INACTIVE">Inativo</SelectItem>
                  <SelectItem value="SUSPENDED">Suspenso</SelectItem>
                </SelectContent>
              </Select>
            </Field>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Endereço</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Field label="CEP" error={errors.zipCode?.message}>
              <Input {...register('zipCode')} placeholder="00000-000" />
            </Field>
            <Field label="Endereço" error={errors.address?.message} className="lg:col-span-2">
              <Input {...register('address')} />
            </Field>
            <Field label="Número" error={errors.addressNumber?.message}>
              <Input {...register('addressNumber')} />
            </Field>
            <Field label="Bairro" error={errors.neighborhood?.message}>
              <Input {...register('neighborhood')} />
            </Field>
            <Field label="Cidade" error={errors.city?.message}>
              <Input {...register('city')} />
            </Field>
            <Field label="Estado" error={errors.state?.message}>
              <Input {...register('state')} maxLength={2} />
            </Field>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Contato de emergência</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Field label="Nome" error={errors.emergencyContactName?.message}>
              <Input {...register('emergencyContactName')} />
            </Field>
            <Field label="Telefone" error={errors.emergencyContactPhone?.message}>
              <Input {...register('emergencyContactPhone')} />
            </Field>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Observações</CardTitle>
          </CardHeader>
          <CardContent>
            <Textarea {...register('notes')} rows={4} placeholder="Observações médicas, restrições, etc." />
          </CardContent>
        </Card>

        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={() => navigate('/students')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
            Salvar
          </Button>
        </div>
      </form>
    </div>
  )
}

function Field({
  label,
  error,
  children,
  className,
}: {
  label: string
  error?: string
  children: ReactNode
  className?: string
}) {
  return (
    <div className={className}>
      <Label className="mb-1.5 block">{label}</Label>
      {children}
      {error && <p className="mt-1 text-xs text-destructive">{error}</p>}
    </div>
  )
}
