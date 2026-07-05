import { CalendarCheck, Loader2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatCard } from '@/components/shared/StatCard'
import { EmptyState } from '@/components/shared/EmptyState'
import { Badge } from '@/components/ui/badge'
import { useMyAttendanceFrequency, useMyAttendanceHistory } from '@/hooks/useAttendances'
import { formatDateTime } from '@/utils/formatDate'

export function MyAttendancePage() {
  const { data: history, isLoading: isHistoryLoading } = useMyAttendanceHistory()
  const { data: frequency, isLoading: isFrequencyLoading } = useMyAttendanceFrequency()

  return (
    <div className="space-y-6">
      <PageHeader title="Minha Presenca" description="Acompanhe seu historico de check-in e frequencia." />

      <div className="grid gap-4 sm:grid-cols-2">
        <StatCard
          label="Check-ins hoje"
          value={String(frequency?.daily ?? 0)}
          icon={CalendarCheck}
          isLoading={isFrequencyLoading}
        />
        <StatCard
          label="Check-ins no mes"
          value={String(frequency?.monthly ?? 0)}
          icon={CalendarCheck}
          isLoading={isFrequencyLoading}
        />
      </div>

      {isHistoryLoading ? (
        <div className="flex min-h-[30vh] items-center justify-center">
          <Loader2 className="h-6 w-6 animate-spin text-primary" />
        </div>
      ) : !history || history.length === 0 ? (
        <EmptyState
          icon={CalendarCheck}
          title="Nenhum registro de presenca"
          description="Seus check-ins aparecerao aqui assim que voce frequentar a academia."
        />
      ) : (
        <div className="space-y-2">
          {history.map((attendance) => (
            <div
              key={attendance.id}
              className="flex items-center justify-between rounded-lg border border-border p-3 text-sm"
            >
              <span>Entrada: {formatDateTime(attendance.checkInAt)}</span>
              {attendance.checkOutAt ? (
                <span>Saida: {formatDateTime(attendance.checkOutAt)}</span>
              ) : (
                <Badge variant="warning">Em aberto</Badge>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
