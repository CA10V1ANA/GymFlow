import { createCrudService } from '@/services/crudService'
import type { Exercise, ExercisePayload } from '@/types/exercise'

export const exerciseService = createCrudService<Exercise, ExercisePayload>('/exercises')
