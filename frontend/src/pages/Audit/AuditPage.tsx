import { useState } from 'react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { Badge } from '@/components/ui/badge'
import { useAuditLogs } from '@/hooks/useAudit'
import type { AuditLog } from '@/types/audit'
import { formatDateTime } from '@/utils/formatDate'

export function AuditPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useAuditLogs({ page, size: 15, sort: 'timestamp,desc' })

  const columns: DataTableColumn<AuditLog>[] = [
    { key: 'timestamp', header: 'Data/Hora', render: (row) => formatDateTime(row.timestamp) },
    { key: 'userName', header: 'Usuário', render: (row) => row.userName },
    { key: 'entity', header: 'Entidade', render: (row) => <Badge variant="outline">{row.entity}</Badge> },
    { key: 'action', header: 'Ação', render: (row) => row.action },
    { key: 'details', header: 'Detalhes', render: (row) => row.details ?? '-' },
  ]

  return (
    <div className="space-y-6">
      <PageHeader title="Auditoria" description="Histórico de ações realizadas no sistema." />

      <DataTable
        columns={columns}
        data={data?.content ?? []}
        totalElements={data?.totalElements ?? 0}
        totalPages={data?.totalPages ?? 0}
        page={page}
        size={15}
        isLoading={isLoading}
        onPageChange={setPage}
        rowKey={(row) => row.id}
        emptyMessage="Nenhum registro de auditoria."
      />
    </div>
  )
}
