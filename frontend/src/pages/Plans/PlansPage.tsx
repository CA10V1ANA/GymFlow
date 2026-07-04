import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ListChecks, Loader2, Pencil, Plus, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { EmptyState } from '@/components/shared/EmptyState'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Textarea } from '@/components/ui/textarea'
import { Switch } from '@/components/ui/switch'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { useCreatePlan, useDeletePlan, usePlans, useUpdatePlan } from '@/hooks/usePlans'
import type { Plan, PlanPayload, PlanType } from '@/types/plan'
import { formatCurrency } from '@/utils/formatCurrency'

const planSchema = z.object({
  name: z.string().min(2, 'Informe o nome do plano.'),
  type: z.enum(['MONTHLY', 'QUARTERLY', 'SEMIANNUAL', 'ANNUAL', 'CUSTOM']),
  description: z.string().optional(),
  price: z.number().positive('Informe um valor válido.'),
  durationMonths: z.number().int().positive('Informe a duração em meses.'),
  discountPercentage: z.number().nonnegative('Informe um desconto válido.'),
  active: z.boolean(),
})

type PlanFormData = z.infer<typeof planSchema>

export function PlansPage() {
  const { data, isLoading } = usePlans({ size: 50, sort: 'name,asc' })
  const createPlan = useCreatePlan()
  const updatePlan = useUpdatePlan()
  const deletePlan = useDeletePlan()

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editingPlan, setEditingPlan] = useState<Plan | null>(null)
  const [planToDelete, setPlanToDelete] = useState<Plan | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<PlanFormData>({
    resolver: zodResolver(planSchema),
    defaultValues: { name: '', type: 'MONTHLY', description: '', price: 0, durationMonths: 1, discountPercentage: 0, active: true },
  })

  function openCreateDialog() {
    setEditingPlan(null)
    reset({ name: '', type: 'MONTHLY', description: '', price: 0, durationMonths: 1, discountPercentage: 0, active: true })
    setDialogOpen(true)
  }

  function openEditDialog(plan: Plan) {
    setEditingPlan(plan)
    reset({
      name: plan.name,
      type: plan.type,
      description: plan.description ?? '',
      price: plan.price,
      durationMonths: plan.durationMonths,
      discountPercentage: plan.discountPercentage ?? 0,
      active: plan.active,
    })
    setDialogOpen(true)
  }

  async function onSubmit(data: PlanFormData) {
    const payload: PlanPayload = { ...data }
    if (editingPlan) {
      await updatePlan.mutateAsync({ id: editingPlan.id, payload })
    } else {
      await createPlan.mutateAsync(payload)
    }
    setDialogOpen(false)
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Planos"
        description="Configure os planos disponíveis para matrícula."
        actions={
          <Button onClick={openCreateDialog}>
            <Plus className="h-4 w-4" />
            Novo plano
          </Button>
        }
      />

      {isLoading && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 3 }).map((_, index) => (
            <Skeleton key={index} className="h-48 w-full" />
          ))}
        </div>
      )}

      {!isLoading && (data?.content.length ?? 0) === 0 && (
        <EmptyState icon={ListChecks} title="Nenhum plano cadastrado" description="Crie o primeiro plano da academia." />
      )}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {data?.content.map((plan) => (
          <Card key={plan.id}>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle>{plan.name}</CardTitle>
                <Badge variant={plan.active ? 'success' : 'secondary'}>
                  {plan.active ? 'Ativo' : 'Inativo'}
                </Badge>
              </div>
            </CardHeader>
            <CardContent className="space-y-2">
              <p className="text-2xl font-semibold">{formatCurrency(plan.price)}</p>
              <p className="text-sm text-muted-foreground">{plan.durationMonths} meses • {plan.type}</p>
              {!!plan.discountPercentage && <p className="text-sm text-muted-foreground">Desconto: {plan.discountPercentage}%</p>}
              {plan.description && <p className="text-sm text-muted-foreground">{plan.description}</p>}
            </CardContent>
            <CardFooter className="justify-end gap-2">
              <Button variant="outline" size="sm" onClick={() => openEditDialog(plan)}>
                <Pencil className="h-4 w-4" />
              </Button>
              <Button variant="outline" size="sm" onClick={() => setPlanToDelete(plan)}>
                <Trash2 className="h-4 w-4 text-destructive" />
              </Button>
            </CardFooter>
          </Card>
        ))}
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{editingPlan ? 'Editar plano' : 'Novo plano'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Nome</Label>
              <Input {...register('name')} />
              {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">Tipo</Label>
              <Select value={watch('type')} onValueChange={(value) => setValue('type', value as PlanType)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="MONTHLY">Mensal</SelectItem>
                  <SelectItem value="QUARTERLY">Trimestral</SelectItem>
                  <SelectItem value="SEMIANNUAL">Semestral</SelectItem>
                  <SelectItem value="ANNUAL">Anual</SelectItem>
                  <SelectItem value="CUSTOM">Personalizado</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label className="mb-1.5 block">Descrição</Label>
              <Textarea {...register('description')} rows={3} />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="mb-1.5 block">Preço (R$)</Label>
                <Input type="number" step="0.01" {...register('price', { valueAsNumber: true })} />
                {errors.price && <p className="mt-1 text-xs text-destructive">{errors.price.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Duração (meses)</Label>
                <Input type="number" {...register('durationMonths', { valueAsNumber: true })} />
                {errors.durationMonths && (
                  <p className="mt-1 text-xs text-destructive">{errors.durationMonths.message}</p>
                )}
              </div>
              <div>
                <Label className="mb-1.5 block">Desconto (%)</Label>
                <Input type="number" step="0.01" {...register('discountPercentage', { valueAsNumber: true })} />
                {errors.discountPercentage && <p className="mt-1 text-xs text-destructive">{errors.discountPercentage.message}</p>}
              </div>
            </div>
            <div className="flex items-center gap-2">
              <Switch checked={watch('active')} onCheckedChange={(checked) => setValue('active', checked)} />
              <Label>Plano ativo</Label>
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
        open={!!planToDelete}
        onOpenChange={(open) => !open && setPlanToDelete(null)}
        title="Remover plano"
        description={`Tem certeza que deseja remover o plano "${planToDelete?.name}"?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (planToDelete) await deletePlan.mutateAsync(planToDelete.id)
        }}
      />
    </div>
  )
}
