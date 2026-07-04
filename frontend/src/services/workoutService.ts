import { api } from '@/services/api'
import type { Page, PageParams } from '@/types/common'
import type { Workout, WorkoutPayload } from '@/types/workout'

interface WorkoutApiResponse {
  id: string
  student: { id: string; name: string }
  name: string
  goal?: string
  active: boolean
  startDate?: string
  endDate?: string
  notes?: string
  exercises: Array<{
    id: string
    exercise: { id: string; name: string }
    sortOrder?: number
    sets: number
    repetitions: string
    loadKg?: number
    durationSeconds?: number
    restSeconds?: number
    notes?: string
  }>
  createdAt: string
  updatedAt: string
}

function normalizeWorkout(data: WorkoutApiResponse): Workout {
  return {
    id: data.id,
    studentId: data.student.id,
    studentName: data.student.name,
    name: data.name,
    goal: data.goal,
    active: data.active,
    startDate: data.startDate,
    endDate: data.endDate,
    notes: data.notes,
    exercises: (data.exercises ?? []).map((exercise) => ({
      id: exercise.id,
      exerciseId: exercise.exercise.id,
      exerciseName: exercise.exercise.name,
      sets: exercise.sets,
      reps: exercise.repetitions,
      load: exercise.loadKg,
      restSeconds: exercise.restSeconds,
      durationSeconds: exercise.durationSeconds,
      order: exercise.sortOrder ?? 0,
      notes: exercise.notes,
    })),
    createdAt: data.createdAt,
    updatedAt: data.updatedAt,
  }
}

function toWorkoutRequest(payload: WorkoutPayload) {
  return {
    studentId: payload.studentId,
    name: payload.name,
    goal: payload.goal,
    active: payload.active,
    startDate: payload.startDate,
    endDate: payload.endDate,
    notes: payload.notes,
    exercises: payload.exercises.map((exercise, index) => ({
      exerciseId: exercise.exerciseId,
      sortOrder: exercise.order ?? index + 1,
      sets: exercise.sets,
      repetitions: exercise.reps,
      loadKg: exercise.load ? Number(exercise.load) || 0 : undefined,
      restSeconds: exercise.restSeconds,
    })),
  }
}

export const workoutService = {
  list: async (params?: PageParams): Promise<Page<Workout>> => {
    const { data } = await api.get<Page<WorkoutApiResponse>>('/workouts', { params })
    return { ...data, content: (data.content ?? []).map(normalizeWorkout) }
  },
  getById: async (id: string): Promise<Workout> => {
    const { data } = await api.get<WorkoutApiResponse>(`/workouts/${id}`)
    return normalizeWorkout(data)
  },
  create: async (payload: WorkoutPayload): Promise<Workout> => {
    const { data } = await api.post<WorkoutApiResponse>('/workouts', toWorkoutRequest(payload))
    return normalizeWorkout(data)
  },
  update: async (id: string, payload: WorkoutPayload): Promise<Workout> => {
    const { data } = await api.put<WorkoutApiResponse>(`/workouts/${id}`, toWorkoutRequest(payload))
    return normalizeWorkout(data)
  },
  remove: async (id: string): Promise<void> => {
    await api.delete(`/workouts/${id}`)
  },
}
