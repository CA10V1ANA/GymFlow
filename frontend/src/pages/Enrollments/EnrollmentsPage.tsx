import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Plus, RotateCcw, Snowflake, XCircle, PlayCircle } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/label'
import {
  useCancelEnrollment,
  useEnrollments,
  useFreezeEnrollment,
  useReactivateEnrollment,
  useRenewEnrollment,
} from '@/hooks/useEnrollments'
import type { Enrollment, EnrollmentStatus } from '@/types/enrollment'
import { formatDate } from '@/utils/formatDate'

const statusVariant: Record<EnrollmentStatus, 'success' | 'secondary' | 'destructive' | 'warning'> = {
  ACTIVE: 'success',
  CANCELLED: 'destructive',
  FROZEN: 'warning',
  EXPIRED: 'secondary',
}

const statusLabels: Record<EnrollmentStatus, string> = {
  ACTIVE: 'Ativa',
  CANCELLED: 'Cancelada',
  FROZEN: 'Congelada',
  EXPIRED: 'Expirada',
}

export function EnrollmentsPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const { data, isLoading } = useEnrollments({ page, size: 10, sort: 'startDate,desc' })

  const renew = useRenewEnrollment()
  const cancel = useCancelEnrollment()
  const freeze = useFreezeEnrollment()
  const reactivate = useReactivateEnrollment()

  const [enrollmentToCancel, setEnrollmentToCancel] = useState<Enrollment | null>(null)
  const [enrollmentForAction, setEnrollmentForAction] = useState<{ enrollment: Enrollment; action: 'renew' | 'freeze' | 'reactivate' } | null>(null)
  const [cancelReason, setCancelReason] = useState('')

  const columns: DataTableColumn<Enrollment>[] = [
    { key: 'studentName', header: 'Aluno', render: (row) => row.studentName },
    { key: 'planName', header: 'Plano', render: (row) => row.planName },
    { key: 'startDate', header: 'Início', render: (row) => formatDate(row.startDate) },
    { key: 'endDate', header: 'Fim', render: (row) => formatDate(row.endDate) },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge variant={statusVariant[row.status]}>{statusLabels[row.status]}</Badge>,
    },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) => (
        <div className="flex justify-end gap-1">
          {row.status !== 'ACTIVE' && row.status !== 'CANCELLED' && (
            <Button variant="ghost" size="icon" title="Reativar" onClick={() => setEnrollmentForAction({ enrollment: row, action: 'reactivate' })}>
              <PlayCircle className="h-4 w-4" />
            </Button>
          )}
          {row.status === 'ACTIVE' && (
            <>
              <Button variant="ghost" size="icon" title="Renovar" onClick={() => setEnrollmentForAction({ enrollment: row, action: 'renew' })}>
                <RotateCcw className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="icon" title="Congelar" onClick={() => setEnrollmentForAction({ enrollment: row, action: 'freeze' })}>
                <Snowflake className="h-4 w-4" />
              </Button>
              <Button variant="ghost" size="icon" title="Cancelar" onClick={() => setEnrollmentToCancel(row)}>
                <XCircle className="h-4 w-4 text-destructive" />
              </Button>
            </>
          )}
        </div>
      ),
    },
  ]

  async function handleConfirmedAction() {
    if (!enrollmentForAction) return
    const { enrollment, action } = enrollmentForAction
    if (action === 'renew') await renew.mutateAsync(enrollment.id)
    if (action === 'freeze') await freeze.mutateAsync(enrollment.id)
    if (action === 'reactivate') await reactivate.mutateAsync(enrollment.id)
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Matrículas"
        description="Acompanhe e gerencie as matrículas dos alunos."
        actions={
          <Button onClick={() => navigate('/enrollments/new')}>
            <Plus className="h-4 w-4" />
            Nova matrícula
          </Button>
        }
      />

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
        emptyMessage="Nenhuma matrícula encontrada."
      />

      <ConfirmDialog
        open={!!enrollmentForAction}
        onOpenChange={(open) => !open && setEnrollmentForAction(null)}
        title={
          enrollmentForAction?.action === 'renew'
            ? 'Renovar matrícula'
            : enrollmentForAction?.action === 'freeze'
              ? 'Congelar matrícula'
              : 'Reativar matrícula'
        }
        description={`Confirma essa ação para ${enrollmentForAction?.enrollment.studentName}?`}
        onConfirm={handleConfirmedAction}
      />

      <Dialog open={!!enrollmentToCancel} onOpenChange={(open) => !open && setEnrollmentToCancel(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancelar matrícula</DialogTitle>
          </DialogHeader>
          <div>
            <Label className="mb-1.5 block">Motivo do cancelamento</Label>
            <Input value={cancelReason} onChange={(event) => setCancelReason(event.target.value)} />
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEnrollmentToCancel(null)}>
              Voltar
            </Button>
            <Button
              variant="destructive"
              onClick={async () => {
                if (!enrollmentToCancel) return
                if (!cancelReason.trim()) {
                  toast.error('Informe o motivo do cancelamento.')
                  return
                }
                await cancel.mutateAsync({ id: enrollmentToCancel.id, payload: { reason: cancelReason } })
                setEnrollmentToCancel(null)
                setCancelReason('')
              }}
            >
              Confirmar cancelamento
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
