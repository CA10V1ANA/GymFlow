import { useState } from 'react'
import { CheckCircle2, Plus } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useFinancialTransactions, useMarkTransactionAsPaid } from '@/hooks/useFinancial'
import type { FinancialTransaction, TransactionStatus, TransactionType } from '@/types/financial'
import { formatCurrency } from '@/utils/formatCurrency'
import { formatDate } from '@/utils/formatDate'
import { toast } from 'sonner'

const statusLabels: Record<TransactionStatus, string> = {
  PENDING: 'Pendente',
  PAID: 'Pago',
  OVERDUE: 'Vencido',
  CANCELLED: 'Cancelado',
}

const statusVariant: Record<TransactionStatus, 'warning' | 'success' | 'destructive' | 'secondary'> = {
  PENDING: 'warning',
  PAID: 'success',
  OVERDUE: 'destructive',
  CANCELLED: 'secondary',
}

const typeLabels: Record<TransactionType, string> = {
  INCOME: 'Receita',
  EXPENSE: 'Despesa',
}

export function FinancialPage() {
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState('ALL')
  const [type, setType] = useState('ALL')

  const { data, isLoading } = useFinancialTransactions({
    page,
    size: 10,
    sort: 'dueDate,desc',
    status: status === 'ALL' ? undefined : status,
    type: type === 'ALL' ? undefined : type,
  })
  const markAsPaid = useMarkTransactionAsPaid()

  const columns: DataTableColumn<FinancialTransaction>[] = [
    { key: 'description', header: 'Descrição', render: (row) => row.description },
    { key: 'studentName', header: 'Aluno', render: (row) => row.studentName ?? '-' },
    { key: 'type', header: 'Tipo', render: (row) => <Badge variant="outline">{typeLabels[row.type]}</Badge> },
    { key: 'amount', header: 'Valor', render: (row) => formatCurrency(row.amount) },
    { key: 'dueDate', header: 'Vencimento', render: (row) => formatDate(row.dueDate) },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge variant={statusVariant[row.status]}>{statusLabels[row.status]}</Badge>,
    },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) =>
        row.status !== 'PAID' &&
        row.status !== 'CANCELLED' && (
          <Button variant="outline" size="sm" onClick={() => markAsPaid.mutate(row.id)}>
            <CheckCircle2 className="h-4 w-4" />
            Marcar como pago
          </Button>
        ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Financeiro"
        description="Acompanhe receitas, despesas e mensalidades."
        actions={
          <Button onClick={() => toast('Cadastro de lançamentos em breve.')}>
            <Plus className="h-4 w-4" />
            Novo lançamento
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
        toolbar={
          <div className="flex gap-2">
            <Select value={status} onValueChange={(value) => { setStatus(value); setPage(0) }}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="Status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">Todos os status</SelectItem>
                {Object.entries(statusLabels).map(([key, label]) => (
                  <SelectItem key={key} value={key}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={type} onValueChange={(value) => { setType(value); setPage(0) }}>
              <SelectTrigger className="w-36">
                <SelectValue placeholder="Tipo" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">Todos os tipos</SelectItem>
                {Object.entries(typeLabels).map(([key, label]) => (
                  <SelectItem key={key} value={key}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        }
      />
    </div>
  )
}
