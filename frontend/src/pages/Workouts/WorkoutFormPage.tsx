import { useEffect } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useNavigate, useParams } from 'react-router-dom'
import { GripVertical, Loader2, Plus, Save, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useStudents } from '@/hooks/useStudents'
import { useExercises } from '@/hooks/useExercises'
import { useCreateWorkout, useUpdateWorkout, useWorkout } from '@/hooks/useWorkouts'
import type { WorkoutPayload } from '@/types/workout'

const workoutExerciseSchema = z.object({
  exerciseId: z.string().min(1, 'Selecione um exercício.'),
  sets: z.number().int().positive('Informe as séries.'),
  reps: z.string().min(1, 'Informe as repetições.'),
  load: z.string().optional(),
  restSeconds: z.number().int().nonnegative('Informe o descanso.'),
})

const workoutSchema = z.object({
  studentId: z.string().min(1, 'Selecione um aluno.'),
  name: z.string().min(2, 'Informe o nome do treino.'),
  goal: z.string().optional(),
  active: z.boolean(),
  exercises: z.array(workoutExerciseSchema).min(1, 'Adicione ao menos um exercício.'),
})

type WorkoutFormData = z.infer<typeof workoutSchema>

export function WorkoutFormPage() {
  const { id } = useParams()
  const isEditing = !!id
  const navigate = useNavigate()
  const { data: workout, isLoading } = useWorkout(id)
  const { data: students } = useStudents({ size: 100, sort: 'name,asc' })
  const { data: exercises } = useExercises({ size: 200, sort: 'name,asc' })
  const createWorkout = useCreateWorkout()
  const updateWorkout = useUpdateWorkout()

  const {
    register,
    control,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<WorkoutFormData>({
    resolver: zodResolver(workoutSchema),
    defaultValues: {
      studentId: '',
      name: '',
      goal: '',
      active: true,
      exercises: [],
    },
  })

  const { fields, append, remove } = useFieldArray({ control, name: 'exercises' })

  useEffect(() => {
    if (workout) {
      reset({
        studentId: workout.studentId,
        name: workout.name,
        goal: workout.goal ?? '',
        active: workout.active,
        exercises: workout.exercises.map((exercise) => ({
          exerciseId: exercise.exerciseId,
          sets: exercise.sets,
          reps: exercise.reps,
          load: exercise.load != null ? String(exercise.load) : '',
          restSeconds: exercise.restSeconds ?? 0,
        })),
      })
    }
  }, [workout, reset])

  async function onSubmit(data: WorkoutFormData) {
    const payload: WorkoutPayload = {
      studentId: data.studentId,
      name: data.name,
      goal: data.goal,
      active: data.active,
      exercises: data.exercises.map((exercise, index) => ({ ...exercise, order: index + 1 })),
    }
    if (isEditing && id) {
      await updateWorkout.mutateAsync({ id, payload })
    } else {
      await createWorkout.mutateAsync(payload)
    }
    navigate('/workouts')
  }

  if (isEditing && isLoading) {
    return <p className="text-sm text-muted-foreground">Carregando...</p>
  }

  return (
    <div className="space-y-6">
      <PageHeader title={isEditing ? 'Editar treino' : 'Novo treino'} />

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>Informações do treino</CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <div className="lg:col-span-2">
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
            <div className="flex items-end gap-2 pb-1">
              <Switch checked={watch('active')} onCheckedChange={(checked) => setValue('active', checked)} />
              <Label>Ativo</Label>
            </div>
            <div className="lg:col-span-2">
              <Label className="mb-1.5 block">Nome do treino</Label>
              <Input {...register('name')} placeholder="Ex: Peito e Tríceps" />
              {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
            </div>
            <div className="lg:col-span-2">
              <Label className="mb-1.5 block">Objetivo</Label>
              <Input {...register('goal')} placeholder="Ex: Hipertrofia" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex-row items-center justify-between space-y-0">
            <CardTitle>Exercícios</CardTitle>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => append({ exerciseId: '', sets: 3, reps: '12', load: '', restSeconds: 60 })}
            >
              <Plus className="h-4 w-4" />
              Adicionar exercício
            </Button>
          </CardHeader>
          <CardContent className="space-y-3">
            {errors.exercises?.root && <p className="text-xs text-destructive">{errors.exercises.root.message}</p>}
            {fields.length === 0 && (
              <p className="text-sm text-muted-foreground">Nenhum exercício adicionado ainda.</p>
            )}
            {fields.map((field, index) => (
              <div key={field.id} className="grid grid-cols-1 gap-3 rounded-lg border border-border p-3 sm:grid-cols-12 sm:items-end">
                <div className="hidden sm:col-span-1 sm:flex sm:items-center sm:justify-center text-muted-foreground">
                  <GripVertical className="h-4 w-4" />
                </div>
                <div className="sm:col-span-4">
                  <Label className="mb-1.5 block text-xs">Exercício</Label>
                  <Select
                    value={watch(`exercises.${index}.exerciseId`)}
                    onValueChange={(value) => setValue(`exercises.${index}.exerciseId`, value)}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Selecione" />
                    </SelectTrigger>
                    <SelectContent>
                      {exercises?.content.map((exercise) => (
                        <SelectItem key={exercise.id} value={exercise.id}>
                          {exercise.name}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="sm:col-span-1">
                  <Label className="mb-1.5 block text-xs">Séries</Label>
                  <Input type="number" {...register(`exercises.${index}.sets`, { valueAsNumber: true })} />
                </div>
                <div className="sm:col-span-2">
                  <Label className="mb-1.5 block text-xs">Reps</Label>
                  <Input {...register(`exercises.${index}.reps`)} placeholder="8-12" />
                </div>
                <div className="sm:col-span-2">
                  <Label className="mb-1.5 block text-xs">Carga</Label>
                  <Input {...register(`exercises.${index}.load`)} placeholder="20kg" />
                </div>
                <div className="sm:col-span-1">
                  <Label className="mb-1.5 block text-xs">Descanso (s)</Label>
                  <Input type="number" {...register(`exercises.${index}.restSeconds`, { valueAsNumber: true })} />
                </div>
                <div className="sm:col-span-1 flex justify-end">
                  <Button type="button" variant="ghost" size="icon" onClick={() => remove(index)}>
                    <Trash2 className="h-4 w-4 text-destructive" />
                  </Button>
                </div>
              </div>
            ))}
          </CardContent>
        </Card>

        <div className="flex justify-end gap-2">
          <Button type="button" variant="outline" onClick={() => navigate('/workouts')}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
            Salvar treino
          </Button>
        </div>
      </form>
    </div>
  )
}
