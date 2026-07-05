import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2, Save } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { useMyProfile, useUpdateMyProfile } from '@/hooks/useMyProfile'

const profileSchema = z.object({
  phone: z.string().min(8, 'Telefone invalido.'),
  email: z.string().email('Email invalido.'),
  zipCode: z.string().optional(),
  address: z.string().optional(),
  addressNumber: z.string().optional(),
  addressComplement: z.string().optional(),
  neighborhood: z.string().optional(),
  city: z.string().optional(),
  state: z.string().optional(),
  emergencyContactName: z.string().optional(),
  emergencyContactPhone: z.string().optional(),
})

type ProfileFormData = z.infer<typeof profileSchema>

export function MyProfilePage() {
  const { data: profile, isLoading } = useMyProfile()
  const updateProfile = useUpdateMyProfile()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ProfileFormData>({ resolver: zodResolver(profileSchema) })

  useEffect(() => {
    if (profile) {
      reset({
        phone: profile.phone,
        email: profile.email,
        zipCode: profile.zipCode ?? '',
        address: profile.address ?? '',
        addressNumber: profile.addressNumber ?? '',
        addressComplement: profile.addressComplement ?? '',
        neighborhood: profile.neighborhood ?? '',
        city: profile.city ?? '',
        state: profile.state ?? '',
        emergencyContactName: profile.emergencyContactName ?? '',
        emergencyContactPhone: profile.emergencyContactPhone ?? '',
      })
    }
  }, [profile, reset])

  async function onSubmit(data: ProfileFormData) {
    await updateProfile.mutateAsync(data)
  }

  if (isLoading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader title="Meu Perfil" description="Visualize e edite seus dados pessoais." />

      {profile && (
        <Card>
          <CardHeader>
            <CardTitle className="flex flex-wrap items-center gap-2 text-base">
              {profile.name}
              <Badge variant={profile.status === 'ACTIVE' ? 'success' : 'secondary'}>{profile.status}</Badge>
            </CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label className="mb-1.5 block text-muted-foreground">CPF</Label>
              <p className="text-sm">{profile.cpf}</p>
            </div>
            <div>
              <Label className="mb-1.5 block text-muted-foreground">Matricula</Label>
              <p className="text-sm">{profile.registrationCode}</p>
            </div>
          </CardContent>
        </Card>
      )}

      <form onSubmit={handleSubmit(onSubmit)}>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Dados de contato</CardTitle>
          </CardHeader>
          <CardContent className="grid gap-4 sm:grid-cols-2">
            <div>
              <Label className="mb-1.5 block">Telefone</Label>
              <Input {...register('phone')} />
              {errors.phone && <p className="mt-1 text-xs text-destructive">{errors.phone.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">Email</Label>
              <Input {...register('email')} />
              {errors.email && <p className="mt-1 text-xs text-destructive">{errors.email.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">CEP</Label>
              <Input {...register('zipCode')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Endereco</Label>
              <Input {...register('address')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Numero</Label>
              <Input {...register('addressNumber')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Complemento</Label>
              <Input {...register('addressComplement')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Bairro</Label>
              <Input {...register('neighborhood')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Cidade</Label>
              <Input {...register('city')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Estado</Label>
              <Input {...register('state')} maxLength={2} />
            </div>
            <div>
              <Label className="mb-1.5 block">Contato de emergencia</Label>
              <Input {...register('emergencyContactName')} />
            </div>
            <div>
              <Label className="mb-1.5 block">Telefone de emergencia</Label>
              <Input {...register('emergencyContactPhone')} />
            </div>
          </CardContent>
        </Card>

        <div className="mt-4 flex justify-end">
          <Button type="submit" disabled={isSubmitting}>
            <Save className="h-4 w-4" />
            Salvar alteracoes
          </Button>
        </div>
      </form>
    </div>
  )
}
