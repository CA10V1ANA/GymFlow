import { Dumbbell, Loader2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { EmptyState } from '@/components/shared/EmptyState'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useMyWorkouts } from '@/hooks/useWorkouts'

export function MyWorkoutsPage() {
  const { data, isLoading } = useMyWorkouts({ size: 20 })

  if (isLoading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    )
  }

  const workouts = data?.content ?? []

  return (
    <div className="space-y-6">
      <PageHeader title="Meus Treinos" description="Consulte os treinos montados pelo seu instrutor." />

      {workouts.length === 0 ? (
        <EmptyState
          icon={Dumbbell}
          title="Nenhum treino cadastrado"
          description="Seu instrutor ainda nao montou um treino para voce."
        />
      ) : (
        <div className="grid gap-4 sm:grid-cols-2">
          {workouts.map((workout) => (
            <Card key={workout.id}>
              <CardHeader>
                <CardTitle className="flex items-center justify-between gap-2 text-base">
                  <span>{workout.name}</span>
                  <Badge variant={workout.active ? 'success' : 'secondary'}>
                    {workout.active ? 'Ativo' : 'Inativo'}
                  </Badge>
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                {workout.goal && <p className="text-sm text-muted-foreground">Objetivo: {workout.goal}</p>}
                <ul className="space-y-1.5">
                  {workout.exercises.map((exercise) => (
                    <li key={exercise.id} className="flex items-center justify-between gap-3 text-sm">
                      <span>{exercise.exerciseName}</span>
                      <span className="shrink-0 text-muted-foreground">
                        {exercise.sets}x{exercise.reps}
                        {exercise.load ? ` - ${exercise.load}` : ''}
                      </span>
                    </li>
                  ))}
                </ul>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
