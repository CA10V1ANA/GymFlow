import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Eye, Pencil, Plus, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { useDeleteStudent, useStudents } from '@/hooks/useStudents'
import type { Student, StudentStatus } from '@/types/student'
import { formatDate } from '@/utils/formatDate'

const statusLabels: Record<StudentStatus, string> = {
  ACTIVE: 'Ativo',
  INACTIVE: 'Inativo',
  SUSPENDED: 'Suspenso',
}

const statusVariant: Record<StudentStatus, 'success' | 'secondary' | 'destructive'> = {
  ACTIVE: 'success',
  INACTIVE: 'secondary',
  SUSPENDED: 'destructive',
}

export function StudentsListPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [sort, setSort] = useState('name,asc')
  const [status, setStatus] = useState<string>('ALL')
  const [studentToDelete, setStudentToDelete] = useState<Student | null>(null)

  const { data, isLoading } = useStudents({
    page,
    size: 10,
    sort,
    search: search || undefined,
    status: status === 'ALL' ? undefined : status,
  })
  const deleteStudent = useDeleteStudent()

  const columns: DataTableColumn<Student>[] = [
    { key: 'name', header: 'Nome', sortable: true, render: (row) => row.name },
    { key: 'email', header: 'Email', render: (row) => row.email },
    { key: 'phone', header: 'Telefone', render: (row) => row.phone },
    {
      key: 'status',
      header: 'Status',
      render: (row) => <Badge variant={statusVariant[row.status]}>{statusLabels[row.status]}</Badge>,
    },
    { key: 'createdAt', header: 'Cadastro', render: (row) => formatDate(row.createdAt) },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) => (
        <div className="flex justify-end gap-1">
          <Button variant="ghost" size="icon" onClick={() => navigate(`/students/${row.id}`)}>
            <Eye className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => navigate(`/students/${row.id}/edit`)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setStudentToDelete(row)}>
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Alunos"
        description="Gerencie o cadastro dos alunos da academia."
        actions={
          <Button onClick={() => navigate('/students/new')}>
            <Plus className="h-4 w-4" />
            Novo aluno
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
        onSearchChange={(value) => {
          setSearch(value)
          setPage(0)
        }}
        onSortChange={(value) => setSort(value)}
        sort={sort}
        searchPlaceholder="Buscar por nome, email ou CPF..."
        rowKey={(row) => row.id}
        toolbar={
          <Select value={status} onValueChange={(value) => { setStatus(value); setPage(0) }}>
            <SelectTrigger className="w-full sm:w-48">
              <SelectValue placeholder="Status" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Todos os status</SelectItem>
              <SelectItem value="ACTIVE">Ativo</SelectItem>
              <SelectItem value="INACTIVE">Inativo</SelectItem>
              <SelectItem value="SUSPENDED">Suspenso</SelectItem>
            </SelectContent>
          </Select>
        }
      />

      <ConfirmDialog
        open={!!studentToDelete}
        onOpenChange={(open) => !open && setStudentToDelete(null)}
        title="Remover aluno"
        description={`Tem certeza que deseja remover ${studentToDelete?.name}? Essa ação não pode ser desfeita.`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (studentToDelete) await deleteStudent.mutateAsync(studentToDelete.id)
        }}
      />
    </div>
  )
}
