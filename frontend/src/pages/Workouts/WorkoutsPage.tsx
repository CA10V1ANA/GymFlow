import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { useDeleteWorkout, useWorkouts } from '@/hooks/useWorkouts'
import type { Workout } from '@/types/workout'

export function WorkoutsPage() {
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const { data, isLoading } = useWorkouts({ page, size: 10 })
  const deleteWorkout = useDeleteWorkout()
  const [workoutToDelete, setWorkoutToDelete] = useState<Workout | null>(null)

  const columns: DataTableColumn<Workout>[] = [
    { key: 'studentName', header: 'Aluno', render: (row) => row.studentName },
    { key: 'name', header: 'Nome', render: (row) => row.name },
    { key: 'exercises', header: 'Exercícios', render: (row) => row.exercises.length },
    {
      key: 'active',
      header: 'Status',
      render: (row) => <Badge variant={row.active ? 'success' : 'secondary'}>{row.active ? 'Ativo' : 'Inativo'}</Badge>,
    },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) => (
        <div className="flex justify-end gap-1">
          <Button variant="ghost" size="icon" onClick={() => navigate(`/workouts/${row.id}/edit`)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setWorkoutToDelete(row)}>
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Treinos"
        description="Monte e gerencie os treinos dos alunos."
        actions={
          <Button onClick={() => navigate('/workouts/new')}>
            <Plus className="h-4 w-4" />
            Novo treino
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
        emptyMessage="Nenhum treino cadastrado."
      />

      <ConfirmDialog
        open={!!workoutToDelete}
        onOpenChange={(open) => !open && setWorkoutToDelete(null)}
        title="Remover treino"
        description={`Tem certeza que deseja remover o treino "${workoutToDelete?.name}"?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (workoutToDelete) await deleteWorkout.mutateAsync(workoutToDelete.id)
        }}
      />
    </div>
  )
}
