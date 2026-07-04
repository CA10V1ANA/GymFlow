import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate } from 'react-router-dom'
import { Loader2, Save } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useStudents } from '@/hooks/useStudents'
import { usePlans } from '@/hooks/usePlans'
import { useCreateEnrollment } from '@/hooks/useEnrollments'

const enrollmentSchema = z.object({
  studentId: z.string().min(1, 'Selecione um aluno.'),
  planId: z.string().min(1, 'Selecione um plano.'),
  startDate: z.string().min(1, 'Informe a data de início.'),
})

type EnrollmentFormData = z.infer<typeof enrollmentSchema>

export function NewEnrollmentPage() {
  const navigate = useNavigate()
  const { data: students } = useStudents({ size: 100, sort: 'name,asc' })
  const { data: plans } = usePlans({ size: 100, sort: 'name,asc' })
  const createEnrollment = useCreateEnrollment()

  const {
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<EnrollmentFormData>({
    resolver: zodResolver(enrollmentSchema),
    defaultValues: { studentId: '', planId: '', startDate: new Date().toISOString().slice(0, 10) },
  })

  async function onSubmit(data: EnrollmentFormData) {
    await createEnrollment.mutateAsync(data)
    navigate('/enrollments')
  }

  return (
    <div className="max-w-xl space-y-6">
      <PageHeader title="Nova matrícula" />

      <Card>
        <CardHeader>
          <CardTitle>Dados da matrícula</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Aluno</Label>
              <Select value={watch('studentId')} onValueChange={(value) => setValue('studentId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione um aluno" />
                </SelectTrigger>
                <SelectContent>
                  {students?.content.map((student) => (
                    <SelectItem key={student.id} value={student.id}>
                      {student.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.studentId && <p className="mt-1 text-xs text-destructive">{errors.studentId.message}</p>}
            </div>

            <div>
              <Label className="mb-1.5 block">Plano</Label>
              <Select value={watch('planId')} onValueChange={(value) => setValue('planId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione um plano" />
                </SelectTrigger>
                <SelectContent>
                  {plans?.content.map((plan) => (
                    <SelectItem key={plan.id} value={plan.id}>
                      {plan.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.planId && <p className="mt-1 text-xs text-destructive">{errors.planId.message}</p>}
            </div>

            <div>
              <Label className="mb-1.5 block">Data de início</Label>
              <Input type="date" value={watch('startDate')} onChange={(event) => setValue('startDate', event.target.value)} />
              {errors.startDate && <p className="mt-1 text-xs text-destructive">{errors.startDate.message}</p>}
            </div>

            <div className="flex justify-end gap-2">
              <Button type="button" variant="outline" onClick={() => navigate('/enrollments')}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                Criar matrícula
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
