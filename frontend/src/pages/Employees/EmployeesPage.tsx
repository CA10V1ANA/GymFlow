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
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useCreateEmployee, useDeleteEmployee, useEmployees, useUpdateEmployee } from '@/hooks/useEmployees'
import type { Employee, EmployeePayload } from '@/types/employee'
import type { Role } from '@/types/common'
import { formatDate } from '@/utils/formatDate'

const roleLabels: Record<Role, string> = {
  ADMIN: 'Administrador',
  RECEPTIONIST: 'Recepcionista',
  INSTRUCTOR: 'Instrutor',
  STUDENT: 'Aluno',
}

const employeeSchema = z.object({
  name: z.string().min(3, 'Informe o nome.'),
  email: z.string().email('Email inválido.'),
  password: z.string().optional(),
  phone: z.string().min(8, 'Telefone inválido.'),
  cpf: z.string().min(11, 'CPF inválido.'),
  role: z.enum(['ADMIN', 'RECEPTIONIST', 'INSTRUCTOR', 'STUDENT']),
  position: z.string().min(2, 'Informe o cargo.'),
  hiredAt: z.string().min(1, 'Informe a data de contratação.'),
  salary: z.number().nonnegative().optional(),
})

type EmployeeFormData = z.infer<typeof employeeSchema>

export function EmployeesPage() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useEmployees({ page, size: 10, sort: 'name,asc' })
  const createEmployee = useCreateEmployee()
  const updateEmployee = useUpdateEmployee()
  const deleteEmployee = useDeleteEmployee()

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null)
  const [employeeToDelete, setEmployeeToDelete] = useState<Employee | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<EmployeeFormData>({
    resolver: zodResolver(employeeSchema),
    defaultValues: {
      name: '',
      email: '',
      password: '',
      phone: '',
      cpf: '',
      role: 'RECEPTIONIST',
      position: '',
      hiredAt: new Date().toISOString().slice(0, 10),
      salary: 0,
    },
  })

  function openCreateDialog() {
    setEditingEmployee(null)
    reset({
      name: '',
      email: '',
      password: '',
      phone: '',
      cpf: '',
      role: 'RECEPTIONIST',
      position: '',
      hiredAt: new Date().toISOString().slice(0, 10),
      salary: 0,
    })
    setDialogOpen(true)
  }

  function openEditDialog(employee: Employee) {
    setEditingEmployee(employee)
    reset({
      name: employee.name,
      email: employee.email,
      password: '',
      phone: employee.phone,
      cpf: employee.cpf,
      role: employee.role,
      position: employee.position,
      hiredAt: employee.hiredAt?.slice(0, 10),
      salary: employee.salary ?? 0,
    })
    setDialogOpen(true)
  }

  async function onSubmit(data: EmployeeFormData) {
    const payload: EmployeePayload = { ...data }
    if (editingEmployee) {
      await updateEmployee.mutateAsync({ id: editingEmployee.id, payload })
    } else {
      await createEmployee.mutateAsync(payload)
    }
    setDialogOpen(false)
  }

  const columns: DataTableColumn<Employee>[] = [
    { key: 'name', header: 'Nome', render: (row) => row.name },
    { key: 'email', header: 'Email', render: (row) => row.email },
    { key: 'role', header: 'Perfil', render: (row) => <Badge variant="outline">{roleLabels[row.role]}</Badge> },
    { key: 'hiredAt', header: 'Contratação', render: (row) => formatDate(row.hiredAt) },
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
          <Button variant="ghost" size="icon" onClick={() => openEditDialog(row)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setEmployeeToDelete(row)}>
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        title="Funcionários"
        description="Gerencie a equipe da academia."
        actions={
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4" />
            Novo funcionário
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
      />

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingEmployee ? 'Editar funcionário' : 'Novo funcionário'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="col-span-2">
                <Label className="mb-1.5 block">Nome</Label>
                <Input {...register('name')} />
                {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Email</Label>
                <Input {...register('email')} />
                {errors.email && <p className="mt-1 text-xs text-destructive">{errors.email.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Telefone</Label>
                <Input {...register('phone')} />
                {errors.phone && <p className="mt-1 text-xs text-destructive">{errors.phone.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Senha {editingEmployee ? '(opcional)' : ''}</Label>
                <Input type="password" {...register('password')} />
              </div>
              <div>
                <Label className="mb-1.5 block">CPF</Label>
                <Input {...register('cpf')} />
                {errors.cpf && <p className="mt-1 text-xs text-destructive">{errors.cpf.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Cargo</Label>
                <Input {...register('position')} />
                {errors.position && <p className="mt-1 text-xs text-destructive">{errors.position.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Data de contratação</Label>
                <Input type="date" {...register('hiredAt')} />
              </div>
              <div>
                <Label className="mb-1.5 block">Salário</Label>
                <Input type="number" step="0.01" {...register('salary', { valueAsNumber: true })} />
              </div>
              <div className="col-span-2">
                <Label className="mb-1.5 block">Perfil (cargo)</Label>
                <Select value={watch('role')} onValueChange={(value) => setValue('role', value as Role)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ADMIN">Administrador</SelectItem>
                    <SelectItem value="RECEPTIONIST">Recepcionista</SelectItem>
                    <SelectItem value="INSTRUCTOR">Instrutor</SelectItem>
                  </SelectContent>
                </Select>
              </div>
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
        open={!!employeeToDelete}
        onOpenChange={(open) => !open && setEmployeeToDelete(null)}
        title="Remover funcionário"
        description={`Tem certeza que deseja remover ${employeeToDelete?.name}?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (employeeToDelete) await deleteEmployee.mutateAsync(employeeToDelete.id)
        }}
      />
    </div>
  )
}
