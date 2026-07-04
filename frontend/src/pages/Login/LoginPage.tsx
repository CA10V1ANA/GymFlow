import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { Loader2, Lock, Mail } from 'lucide-react'
import { isAxiosError } from 'axios'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { useAuth } from '@/hooks/useAuth'

const loginSchema = z.object({
  email: z.string().min(1, 'Informe o email.').email('Email inválido.'),
  password: z.string().min(1, 'Informe a senha.').min(6, 'A senha deve ter no mínimo 6 caracteres.'),
})

type LoginFormData = z.infer<typeof loginSchema>

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [serverError, setServerError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({ resolver: zodResolver(loginSchema) })

  async function onSubmit(data: LoginFormData) {
    setServerError(null)
    try {
      await login(data)
      navigate('/dashboard', { replace: true })
    } catch (error) {
      if (isAxiosError(error) && error.response?.status === 401) {
        setServerError('Email ou senha inválidos.')
      } else {
        setServerError('Não foi possível entrar. Tente novamente em instantes.')
      }
    }
  }

  return (
    <div className="rounded-[1.75rem] border border-border/70 bg-card/80 p-6 shadow-[0_30px_80px_-48px_rgba(0,0,0,0.35)] backdrop-blur-xl sm:p-7">
      <p className="text-[0.68rem] font-semibold uppercase tracking-[0.24em] text-primary/75">Acesso da equipe</p>
      <h1 className="mt-3 text-3xl font-semibold">Entrar</h1>
      <p className="mt-2 text-sm leading-6 text-muted-foreground">
        Acesse o painel operacional da academia com sua conta administrativa.
      </p>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-4">
        <div className="space-y-1.5">
          <Label htmlFor="email">Email</Label>
          <div className="relative">
            <Mail className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              id="email"
              type="email"
              placeholder="voce@academia.com"
              className="pl-8"
              autoComplete="email"
              {...register('email')}
            />
          </div>
          {errors.email && <p className="text-xs text-destructive">{errors.email.message}</p>}
        </div>

        <div className="space-y-1.5">
          <Label htmlFor="password">Senha</Label>
          <div className="relative">
            <Lock className="pointer-events-none absolute left-2.5 top-2.5 h-4 w-4 text-muted-foreground" />
            <Input
              id="password"
              type="password"
              placeholder="••••••••"
              className="pl-8"
              autoComplete="current-password"
              {...register('password')}
            />
          </div>
          {errors.password && <p className="text-xs text-destructive">{errors.password.message}</p>}
        </div>

        {serverError && (
          <p className="rounded-md bg-destructive/10 px-3 py-2 text-sm text-destructive">{serverError}</p>
        )}

        <Button type="submit" className="mt-2 h-11 w-full rounded-xl" disabled={isSubmitting}>
          {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
          Entrar
        </Button>
      </form>
    </div>
  )
}
