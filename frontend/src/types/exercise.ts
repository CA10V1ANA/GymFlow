export type ExerciseCategory = 'STRENGTH' | 'CARDIO' | 'MOBILITY' | 'FUNCTIONAL'

export type MuscleGroup =
  | 'CHEST'
  | 'BACK'
  | 'LEGS'
  | 'SHOULDERS'
  | 'ARMS'
  | 'CORE'
  | 'CARDIO'
  | 'FULL_BODY'

export type ExerciseLevel = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

export interface Exercise {
  id: string
  name: string
  description?: string
  category: ExerciseCategory
  muscleGroup: MuscleGroup
  level: ExerciseLevel
  videoUrl?: string
  imageUrl?: string
  equipment?: string
  createdAt: string
}

export interface ExercisePayload {
  name: string
  description?: string
  category: ExerciseCategory
  muscleGroup: MuscleGroup
  level: ExerciseLevel
  videoUrl?: string
  imageUrl?: string
  equipment?: string
}
