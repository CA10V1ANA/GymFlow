import { Navigate, Route, Routes } from 'react-router-dom'
import { AuthLayout } from '@/layouts/AuthLayout'
import { DashboardLayout } from '@/layouts/DashboardLayout'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { LoginPage } from '@/pages/Login/LoginPage'
import { DashboardPage } from '@/pages/Dashboard/DashboardPage'
import { StudentsListPage } from '@/pages/Students/StudentsListPage'
import { StudentFormPage } from '@/pages/Students/StudentFormPage'
import { StudentDetailPage } from '@/pages/Students/StudentDetailPage'
import { PlansPage } from '@/pages/Plans/PlansPage'
import { EnrollmentsPage } from '@/pages/Enrollments/EnrollmentsPage'
import { NewEnrollmentPage } from '@/pages/Enrollments/NewEnrollmentPage'
import { EmployeesPage } from '@/pages/Employees/EmployeesPage'
import { WorkoutsPage } from '@/pages/Workouts/WorkoutsPage'
import { WorkoutFormPage } from '@/pages/Workouts/WorkoutFormPage'
import { ExercisesPage } from '@/pages/Exercises/ExercisesPage'
import { AttendancePage } from '@/pages/Attendance/AttendancePage'
import { FinancialPage } from '@/pages/Financial/FinancialPage'
import { ProductsPage } from '@/pages/Products/ProductsPage'
import { ReportsPage } from '@/pages/Reports/ReportsPage'
import { AuditPage } from '@/pages/Audit/AuditPage'
import { NotFoundPage } from '@/pages/NotFound/NotFoundPage'

export function AppRoutes() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      <Route
        element={
          <ProtectedRoute>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <DashboardPage />
            </ProtectedRoute>
          }
        />

        <Route path="/students" element={<StudentsListPage />} />
        <Route path="/students/new" element={<StudentFormPage />} />
        <Route path="/students/:id" element={<StudentDetailPage />} />
        <Route path="/students/:id/edit" element={<StudentFormPage />} />

        <Route
          path="/plans"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <PlansPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/enrollments"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <EnrollmentsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/enrollments/new"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <NewEnrollmentPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/employees"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <EmployeesPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/workouts"
          element={
            <ProtectedRoute roles={['ADMIN', 'INSTRUCTOR']}>
              <WorkoutsPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/workouts/new"
          element={
            <ProtectedRoute roles={['ADMIN', 'INSTRUCTOR']}>
              <WorkoutFormPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/workouts/:id/edit"
          element={
            <ProtectedRoute roles={['ADMIN', 'INSTRUCTOR']}>
              <WorkoutFormPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/exercises"
          element={
            <ProtectedRoute roles={['ADMIN', 'INSTRUCTOR']}>
              <ExercisesPage />
            </ProtectedRoute>
          }
        />

        <Route path="/attendance" element={<AttendancePage />} />

        <Route
          path="/financial"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <FinancialPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/products"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <ProductsPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/reports"
          element={
            <ProtectedRoute roles={['ADMIN', 'RECEPTIONIST']}>
              <ReportsPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/audit"
          element={
            <ProtectedRoute roles={['ADMIN']}>
              <AuditPage />
            </ProtectedRoute>
          }
        />

        <Route index element={<Navigate to="/dashboard" replace />} />
      </Route>

      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}
