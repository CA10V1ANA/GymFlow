import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Loader2, Pencil, Plus, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useCreateExercise, useDeleteExercise, useExercises, useUpdateExercise } from '@/hooks/useExercises'
import type { Exercise, ExerciseCategory, ExerciseLevel, ExercisePayload, MuscleGroup } from '@/types/exercise'

const muscleGroupLabels: Record<MuscleGroup, string> = {
  CHEST: 'Peito',
  BACK: 'Costas',
  LEGS: 'Pernas',
  SHOULDERS: 'Ombros',
  ARMS: 'Braços',
  CORE: 'Core',
  CARDIO: 'Cardio',
  FULL_BODY: 'Corpo todo',
}

const levelLabels: Record<ExerciseLevel, string> = {
  BEGINNER: 'Iniciante',
  INTERMEDIATE: 'Intermediário',
  ADVANCED: 'Avançado',
}

const exerciseSchema = z.object({
  name: z.string().min(2, 'Informe o nome do exercício.'),
  description: z.string().optional(),
  category: z.enum(['STRENGTH', 'CARDIO', 'MOBILITY', 'FUNCTIONAL']),
  muscleGroup: z.enum(['CHEST', 'BACK', 'LEGS', 'SHOULDERS', 'ARMS', 'CORE', 'CARDIO', 'FULL_BODY']),
  level: z.enum(['BEGINNER', 'INTERMEDIATE', 'ADVANCED']),
  videoUrl: z.string().url('URL inválida.').optional().or(z.literal('')),
  equipment: z.string().optional(),
})

type ExerciseFormData = z.infer<typeof exerciseSchema>

export function ExercisesPage() {
  const [page, setPage] = useState(0)
  const [search, setSearch] = useState('')
  const [muscleGroupFilter, setMuscleGroupFilter] = useState('ALL')
  const [levelFilter, setLevelFilter] = useState('ALL')

  const { data, isLoading } = useExercises({
    page,
    size: 10,
    search: search || undefined,
    muscleGroup: muscleGroupFilter === 'ALL' ? undefined : muscleGroupFilter,
    level: levelFilter === 'ALL' ? undefined : levelFilter,
  })

  const createExercise = useCreateExercise()
  const updateExercise = useUpdateExercise()
  const deleteExercise = useDeleteExercise()

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingExercise, setEditingExercise] = useState<Exercise | null>(null)
  const [exerciseToDelete, setExerciseToDelete] = useState<Exercise | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<ExerciseFormData>({
    resolver: zodResolver(exerciseSchema),
    defaultValues: { name: '', description: '', category: 'STRENGTH', muscleGroup: 'CHEST', level: 'BEGINNER', videoUrl: '', equipment: '' },
  })

  function openCreateDialog() {
    setEditingExercise(null)
    reset({ name: '', description: '', category: 'STRENGTH', muscleGroup: 'CHEST', level: 'BEGINNER', videoUrl: '', equipment: '' })
    setDialogOpen(true)
  }

  function openEditDialog(exercise: Exercise) {
    setEditingExercise(exercise)
    reset({
      name: exercise.name,
      description: exercise.description ?? '',
      category: exercise.category,
      muscleGroup: exercise.muscleGroup,
      level: exercise.level,
      videoUrl: exercise.videoUrl ?? '',
      equipment: exercise.equipment ?? '',
    })
    setDialogOpen(true)
  }

  async function onSubmit(data: ExerciseFormData) {
    const payload: ExercisePayload = { ...data, videoUrl: data.videoUrl || undefined }
    if (editingExercise) {
      await updateExercise.mutateAsync({ id: editingExercise.id, payload })
    } else {
      await createExercise.mutateAsync(payload)
    }
    setDialogOpen(false)
  }

  const columns: DataTableColumn<Exercise>[] = [
    { key: 'name', header: 'Nome', render: (row) => row.name },
    { key: 'category', header: 'Categoria', render: (row) => categoryLabels[row.category] },
    { key: 'muscleGroup', header: 'Grupo muscular', render: (row) => <Badge variant="outline">{muscleGroupLabels[row.muscleGroup]}</Badge> },
    { key: 'level', header: 'Nível', render: (row) => <Badge variant="secondary">{levelLabels[row.level]}</Badge> },
    {
      key: 'actions',
      header: '',
      className: 'text-right',
      render: (row) => (
        <div className="flex justify-end gap-1">
          <Button variant="ghost" size="icon" onClick={() => openEditDialog(row)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setExerciseToDelete(row)}>
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Exercícios"
        description="Banco de exercícios disponíveis para montagem de treinos."
        actions={
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4" />
            Novo exercício
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
        searchPlaceholder="Buscar exercício..."
        rowKey={(row) => row.id}
        toolbar={
          <div className="flex gap-2">
            <Select value={muscleGroupFilter} onValueChange={(value) => { setMuscleGroupFilter(value); setPage(0) }}>
              <SelectTrigger className="w-44">
                <SelectValue placeholder="Grupo muscular" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">Todos os grupos</SelectItem>
                {Object.entries(muscleGroupLabels).map(([key, label]) => (
                  <SelectItem key={key} value={key}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Select value={levelFilter} onValueChange={(value) => { setLevelFilter(value); setPage(0) }}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="Nível" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">Todos os níveis</SelectItem>
                {Object.entries(levelLabels).map(([key, label]) => (
                  <SelectItem key={key} value={key}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        }
      />

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingExercise ? 'Editar exercício' : 'Novo exercício'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Nome</Label>
              <Input {...register('name')} />
              {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">Categoria</Label>
              <Select value={watch('category')} onValueChange={(value) => setValue('category', value as ExerciseCategory)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.entries(categoryLabels).map(([key, label]) => (
                    <SelectItem key={key} value={key}>
                      {label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.category && <p className="mt-1 text-xs text-destructive">{errors.category.message}</p>}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="mb-1.5 block">Grupo muscular</Label>
                <Select value={watch('muscleGroup')} onValueChange={(value) => setValue('muscleGroup', value as MuscleGroup)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(muscleGroupLabels).map(([key, label]) => (
                      <SelectItem key={key} value={key}>
                        {label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label className="mb-1.5 block">Nível</Label>
                <Select value={watch('level')} onValueChange={(value) => setValue('level', value as ExerciseLevel)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {Object.entries(levelLabels).map(([key, label]) => (
                      <SelectItem key={key} value={key}>
                        {label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
            <div>
              <Label className="mb-1.5 block">Vídeo (URL)</Label>
              <Input {...register('videoUrl')} placeholder="https://..." />
              {errors.videoUrl && <p className="mt-1 text-xs text-destructive">{errors.videoUrl.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">Equipamento</Label>
              <Input {...register('equipment')} placeholder="Ex: Halteres, banco..." />
            </div>
            <div>
              <Label className="mb-1.5 block">Descrição</Label>
              <Textarea {...register('description')} rows={3} />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setDialogOpen(false)}>
                Cancelar
              </Button>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting && <Loader2 className="h-4 w-4 animate-spin" />}
                Salvar
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={!!exerciseToDelete}
        onOpenChange={(open) => !open && setExerciseToDelete(null)}
        title="Remover exercício"
        description={`Tem certeza que deseja remover "${exerciseToDelete?.name}"?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (exerciseToDelete) await deleteExercise.mutateAsync(exerciseToDelete.id)
        }}
      />
    </div>
  )
}
const categoryLabels: Record<ExerciseCategory, string> = {
  STRENGTH: 'Força',
  CARDIO: 'Cardio',
  MOBILITY: 'Mobilidade',
  FUNCTIONAL: 'Funcional',
}
