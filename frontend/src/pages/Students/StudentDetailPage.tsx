import { useParams, useNavigate } from 'react-router-dom'
import { Pencil, Phone, Mail, MapPin, Cake, ShieldAlert } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { useStudent } from '@/hooks/useStudents'
import { useEnrollments } from '@/hooks/useEnrollments'
import { formatDate } from '@/utils/formatDate'
import type { StudentStatus } from '@/types/student'

const statusVariant: Record<StudentStatus, 'success' | 'secondary' | 'destructive'> = {
  ACTIVE: 'success',
  INACTIVE: 'secondary',
  SUSPENDED: 'destructive',
}

export function StudentDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { data: student, isLoading } = useStudent(id)
  const { data: enrollments } = useEnrollments({ studentId: id, size: 20 })

  if (isLoading || !student) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-8 w-64" />
        <Skeleton className="h-48 w-full" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Detalhes do aluno"
        actions={
          <Button onClick={() => navigate(`/students/${id}/edit`)}>
            <Pencil className="h-4 w-4" />
            Editar
          </Button>
        }
      />

      <Card>
        <CardContent className="flex flex-col gap-6 p-6 sm:flex-row sm:items-center">
          <Avatar className="h-20 w-20">
            <AvatarImage src={student.photoUrl ?? undefined} />
            <AvatarFallback className="text-xl">{student.name.slice(0, 2).toUpperCase()}</AvatarFallback>
          </Avatar>
          <div className="flex-1 space-y-2">
            <div className="flex flex-wrap items-center gap-2">
              <h2 className="text-xl font-semibold">{student.name}</h2>
              <Badge variant={statusVariant[student.status]}>{student.status}</Badge>
            </div>
            <div className="grid grid-cols-1 gap-2 text-sm text-muted-foreground sm:grid-cols-2">
              <span className="flex items-center gap-2"><Mail className="h-4 w-4" /> {student.email}</span>
              <span className="flex items-center gap-2"><Phone className="h-4 w-4" /> {student.phone}</span>
              <span className="flex items-center gap-2"><Cake className="h-4 w-4" /> {formatDate(student.birthDate)}</span>
              <span className="flex items-center gap-2">
                <MapPin className="h-4 w-4" /> {student.address}, {student.city ?? ''}-{student.state ?? ''}
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-base">
              <ShieldAlert className="h-4 w-4" /> Contato de emergência
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-1 text-sm">
            <p><span className="text-muted-foreground">Nome:</span> {student.emergencyContact?.name}</p>
            <p><span className="text-muted-foreground">Telefone:</span> {student.emergencyContact?.phone}</p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Observações</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">{student.notes || 'Nenhuma observação registrada.'}</p>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-base">Histórico de matrículas</CardTitle>
        </CardHeader>
        <CardContent className="space-y-2">
          {(enrollments?.content ?? []).length === 0 && (
            <p className="text-sm text-muted-foreground">Nenhuma matrícula encontrada.</p>
          )}
          {(enrollments?.content ?? []).map((enrollment) => (
            <div
              key={enrollment.id}
              className="flex items-center justify-between rounded-lg border border-border p-3 text-sm"
            >
              <div>
                <p className="font-medium">{enrollment.planName}</p>
                <p className="text-muted-foreground">
                  {formatDate(enrollment.startDate)} - {formatDate(enrollment.endDate)}
                </p>
              </div>
              <Badge variant="outline">{enrollment.status}</Badge>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  )
}
