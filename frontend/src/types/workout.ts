export interface WorkoutExercise {
  id: string
  exerciseId: string
  exerciseName: string
  sets: number
  reps: string
  load?: string | number
  restSeconds?: number
  durationSeconds?: number
  order: number
  notes?: string
}

export interface Workout {
  id: string
  studentId: string
  studentName: string
  name: string
  goal?: string
  active: boolean
  startDate?: string
  endDate?: string
  notes?: string
  exercises: WorkoutExercise[]
  createdAt: string
  updatedAt: string
}

export interface WorkoutExercisePayload {
  exerciseId: string
  sets: number
  reps: string
  load?: string
  restSeconds: number
  order: number
}

export interface WorkoutPayload {
  studentId: string
  name: string
  goal?: string
  active: boolean
  startDate?: string
  endDate?: string
  notes?: string
  exercises: WorkoutExercisePayload[]
}
