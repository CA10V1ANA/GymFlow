import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { LogIn, LogOut, ScanLine } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { useAttendances, useCheckIn, useCheckOut } from '@/hooks/useAttendances'
import type { Attendance } from '@/types/attendance'
import { formatDateTime } from '@/utils/formatDate'

const checkInSchema = z.object({
  registrationCode: z.string().min(1, 'Informe o código de matrícula.'),
})

type CheckInFormData = z.infer<typeof checkInSchema>

export function AttendancePage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useAttendances({ page, size: 10, sort: 'checkInAt,desc' })
  const checkIn = useCheckIn()
  const checkOut = useCheckOut()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CheckInFormData>({ resolver: zodResolver(checkInSchema) })

  async function onSubmit(data: CheckInFormData) {
    await checkIn.mutateAsync(data)
    reset()
  }

  const columns: DataTableColumn<Attendance>[] = [
    { key: 'studentName', header: 'Aluno', render: (row) => row.studentName },
    { key: 'checkInAt', header: 'Entrada', render: (row) => formatDateTime(row.checkInAt) },
    {
      key: 'checkOutAt',
      header: 'Saída',
      render: (row) => (row.checkOutAt ? formatDateTime(row.checkOutAt) : <Badge variant="warning">Em aberto</Badge>),
    },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) =>
        !row.checkOutAt && (
          <Button variant="outline" size="sm" onClick={() => checkOut.mutate(row.id)}>
            <LogOut className="h-4 w-4" />
            Check-out
          </Button>
        ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader title="Presença" description="Registre check-in e check-out dos alunos." />

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <ScanLine className="h-4 w-4" /> Check-in rápido
          </CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-3 sm:flex-row sm:items-end">
            <div className="flex-1">
              <Label className="mb-1.5 block">Código de matrícula</Label>
              <Input {...register('registrationCode')} placeholder="Digite ou escaneie o código" autoFocus />
              {errors.registrationCode && (
                <p className="mt-1 text-xs text-destructive">{errors.registrationCode.message}</p>
              )}
            </div>
            <Button type="submit" disabled={isSubmitting}>
              <LogIn className="h-4 w-4" />
              Check-in
            </Button>
          </form>
        </CardContent>
      </Card>

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        totalElements={data?.totalElements ?? 0}
        totalPages={data?.totalPages ?? 0}
        page={page}
        size={10}
        isLoading={isLoading}
        onPageChange={setPage}
        rowKey={(row) => row.id}
        emptyMessage="Nenhum registro de presença."
      />
    </div>
  )
}
