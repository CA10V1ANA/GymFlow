import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { ArrowDownCircle, ArrowUpCircle, Loader2, Pencil, Plus, Trash2 } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { DataTable, type DataTableColumn } from '@/components/shared/DataTable'
import { ConfirmDialog } from '@/components/shared/ConfirmDialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import {
  useCreateProduct,
  useCreateProductCategory,
  useCreateStockMovement,
  useCreateSupplier,
  useDeleteProduct,
  useDeleteProductCategory,
  useDeleteSupplier,
  useProductCategories,
  useProducts,
  useStockMovements,
  useSuppliers,
  useUpdateProduct,
} from '@/hooks/useProducts'
import type { Product, ProductPayload, StockMovementReason, StockMovementType } from '@/types/product'
import { formatCurrency } from '@/utils/formatCurrency'
import { formatDateTime } from '@/utils/formatDate'

const productSchema = z.object({
  name: z.string().min(2, 'Informe o nome.'),
  description: z.string().optional(),
  sku: z.string().min(2, 'Informe o SKU.'),
  price: z.number().nonnegative('Informe um preço válido.'),
  costPrice: z.number().nonnegative().optional(),
  stockQuantity: z.number().int().nonnegative(),
  minStockQuantity: z.number().int().nonnegative().optional(),
  categoryId: z.string().min(1, 'Selecione uma categoria.'),
  supplierId: z.string().optional(),
  active: z.boolean(),
})
type ProductFormData = z.infer<typeof productSchema>

const simpleNameSchema = z.object({ name: z.string().min(2, 'Informe o nome.') })
type SimpleNameFormData = z.infer<typeof simpleNameSchema>

const stockMovementSchema = z.object({
  productId: z.string().min(1, 'Selecione um produto.'),
  type: z.enum(['ENTRY', 'EXIT']),
  quantity: z.number().int().positive('Informe a quantidade.'),
  unitPrice: z.number().nonnegative('Informe o valor unitário.'),
  reason: z.enum(['PURCHASE', 'SALE', 'ADJUSTMENT', 'LOSS']),
})
type StockMovementFormData = z.infer<typeof stockMovementSchema>

export function ProductsPage() {
  return (
    <div className="space-y-6">
      <PageHeader title="Produtos" description="Gerencie produtos, categorias, fornecedores e estoque." />
      <Tabs defaultValue="products">
        <TabsList>
          <TabsTrigger value="products">Produtos</TabsTrigger>
          <TabsTrigger value="categories">Categorias</TabsTrigger>
          <TabsTrigger value="suppliers">Fornecedores</TabsTrigger>
          <TabsTrigger value="stock">Movimentação de estoque</TabsTrigger>
        </TabsList>
        <TabsContent value="products">
          <ProductsTab />
        </TabsContent>
        <TabsContent value="categories">
          <SimpleCrudTab
            title="Categorias"
            useList={useProductCategories}
            useCreate={useCreateProductCategory}
            useDelete={useDeleteProductCategory}
          />
        </TabsContent>
        <TabsContent value="suppliers">
          <SimpleCrudTab
            title="Fornecedores"
            useList={useSuppliers}
            useCreate={useCreateSupplier}
            useDelete={useDeleteSupplier}
          />
        </TabsContent>
        <TabsContent value="stock">
          <StockMovementsTab />
        </TabsContent>
      </Tabs>
    </div>
  )
}

function ProductsTab() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useProducts({ page, size: 10, sort: 'name,asc' })
  const { data: categories } = useProductCategories({ size: 100 })
  const { data: suppliers } = useSuppliers({ size: 100 })
  const createProduct = useCreateProduct()
  const updateProduct = useUpdateProduct()
  const deleteProduct = useDeleteProduct()

  const [dialogOpen, setDialogOpen] = useState(false)
  const [editing, setEditing] = useState<Product | null>(null)
  const [toDelete, setToDelete] = useState<Product | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<ProductFormData>({
    resolver: zodResolver(productSchema),
    defaultValues: {
      name: '',
      description: '',
      sku: '',
      price: 0,
      costPrice: 0,
      stockQuantity: 0,
      minStockQuantity: 0,
      categoryId: '',
      supplierId: '',
      active: true,
    },
  })

  function openCreate() {
    setEditing(null)
    reset({
      name: '',
      description: '',
      sku: '',
      price: 0,
      costPrice: 0,
      stockQuantity: 0,
      minStockQuantity: 0,
      categoryId: '',
      supplierId: '',
      active: true,
    })
    setDialogOpen(true)
  }

  function openEdit(product: Product) {
    setEditing(product)
    reset({
      name: product.name,
      description: product.description ?? '',
      sku: product.sku,
      price: product.price,
      costPrice: product.costPrice ?? 0,
      stockQuantity: product.stockQuantity,
      minStockQuantity: product.minStockQuantity ?? 0,
      categoryId: product.categoryId,
      supplierId: product.supplierId ?? '',
      active: product.active,
    })
    setDialogOpen(true)
  }

  async function onSubmit(data: ProductFormData) {
    const payload: ProductPayload = { ...data, supplierId: data.supplierId || undefined }
    if (editing) {
      await updateProduct.mutateAsync({ id: editing.id, payload })
    } else {
      await createProduct.mutateAsync(payload)
    }
    setDialogOpen(false)
  }

  const columns: DataTableColumn<Product>[] = [
    { key: 'name', header: 'Nome', render: (row) => row.name },
    { key: 'categoryName', header: 'Categoria', render: (row) => row.categoryName ?? '-' },
    { key: 'price', header: 'Preço', render: (row) => formatCurrency(row.price) },
    {
      key: 'stockQuantity',
      header: 'Estoque',
      render: (row) => (
        <Badge variant={row.minStockQuantity !== undefined && row.stockQuantity <= row.minStockQuantity ? 'destructive' : 'outline'}>
          {row.stockQuantity}
        </Badge>
      ),
    },
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
          <Button variant="ghost" size="icon" onClick={() => openEdit(row)}>
            <Pencil className="h-4 w-4" />
          </Button>
          <Button variant="ghost" size="icon" onClick={() => setToDelete(row)}>
            <Trash2 className="h-4 w-4 text-destructive" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="space-y-4 pt-4">
      <div className="flex justify-end">
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" />
          Novo produto
        </Button>
      </div>

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
            <DialogTitle>{editing ? 'Editar produto' : 'Novo produto'}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Nome</Label>
              <Input {...register('name')} />
              {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">SKU</Label>
              <Input {...register('sku')} />
              {errors.sku && <p className="mt-1 text-xs text-destructive">{errors.sku.message}</p>}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="mb-1.5 block">Preço de venda</Label>
                <Input type="number" step="0.01" {...register('price', { valueAsNumber: true })} />
              </div>
              <div>
                <Label className="mb-1.5 block">Preço de custo</Label>
                <Input type="number" step="0.01" {...register('costPrice', { valueAsNumber: true })} />
              </div>
              <div>
                <Label className="mb-1.5 block">Estoque atual</Label>
                <Input type="number" {...register('stockQuantity', { valueAsNumber: true })} />
              </div>
              <div>
                <Label className="mb-1.5 block">Estoque mínimo</Label>
                <Input type="number" {...register('minStockQuantity', { valueAsNumber: true })} />
              </div>
            </div>
            <div>
              <Label className="mb-1.5 block">Categoria</Label>
              <Select value={watch('categoryId')} onValueChange={(value) => setValue('categoryId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {(categories?.content ?? []).map((category) => (
                    <SelectItem key={category.id} value={category.id}>
                      {category.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.categoryId && <p className="mt-1 text-xs text-destructive">{errors.categoryId.message}</p>}
            </div>
            <div>
              <Label className="mb-1.5 block">Fornecedor</Label>
              <Select value={watch('supplierId')} onValueChange={(value) => setValue('supplierId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {(suppliers?.content ?? []).map((supplier) => (
                    <SelectItem key={supplier.id} value={supplier.id}>
                      {supplier.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="flex items-center gap-2">
              <Switch checked={watch('active')} onCheckedChange={(checked) => setValue('active', checked)} />
              <Label>Produto ativo</Label>
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
        open={!!toDelete}
        onOpenChange={(open) => !open && setToDelete(null)}
        title="Remover produto"
        description={`Tem certeza que deseja remover "${toDelete?.name}"?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (toDelete) await deleteProduct.mutateAsync(toDelete.id)
        }}
      />
    </div>
  )
}

interface SimpleEntity {
  id: string
  name: string
}

function SimpleCrudTab({
  title,
  useList,
  useCreate,
  useDelete,
}: {
  title: string
  useList: (params: { size: number }) => { data?: { content: SimpleEntity[] }; isLoading: boolean }
  useCreate: () => { mutateAsync: (payload: SimpleNameFormData) => Promise<unknown> }
  useDelete: () => { mutateAsync: (id: string) => Promise<unknown> }
}) {
  const { data, isLoading } = useList({ size: 100 })
  const create = useCreate()
  const del = useDelete()
  const [dialogOpen, setDialogOpen] = useState(false)
  const [toDelete, setToDelete] = useState<SimpleEntity | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SimpleNameFormData>({ resolver: zodResolver(simpleNameSchema), defaultValues: { name: '' } })

  async function onSubmit(formData: SimpleNameFormData) {
    await create.mutateAsync(formData)
    reset()
    setDialogOpen(false)
  }

  return (
    <div className="space-y-4 pt-4">
      <div className="flex justify-end">
        <Button onClick={() => setDialogOpen(true)}>
          <Plus className="h-4 w-4" />
          Novo
        </Button>
      </div>

      <div className="rounded-lg border border-border">
        {isLoading && <p className="p-4 text-sm text-muted-foreground">Carregando...</p>}
        {!isLoading && (data?.content.length ?? 0) === 0 && (
          <p className="p-4 text-sm text-muted-foreground">Nenhum registro cadastrado.</p>
        )}
        {data?.content.map((item) => (
          <div key={item.id} className="flex items-center justify-between border-b border-border p-3 last:border-0">
            <span className="text-sm font-medium">{item.name}</span>
            <Button variant="ghost" size="icon" onClick={() => setToDelete(item)}>
              <Trash2 className="h-4 w-4 text-destructive" />
            </Button>
          </div>
        ))}
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Novo(a) {title.toLowerCase()}</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Nome</Label>
              <Input {...register('name')} />
              {errors.name && <p className="mt-1 text-xs text-destructive">{errors.name.message}</p>}
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
        open={!!toDelete}
        onOpenChange={(open) => !open && setToDelete(null)}
        title={`Remover ${title.toLowerCase()}`}
        description={`Tem certeza que deseja remover "${toDelete?.name}"?`}
        confirmLabel="Remover"
        variant="destructive"
        onConfirm={async () => {
          if (toDelete) await del.mutateAsync(toDelete.id)
        }}
      />
    </div>
  )
}

function StockMovementsTab() {
  const [page, setPage] = useState(0)
  const { data, isLoading } = useStockMovements({ page, size: 10, sort: 'createdAt,desc' })
  const { data: products } = useProducts({ size: 100, sort: 'name,asc' })
  const createMovement = useCreateStockMovement()
  const [dialogOpen, setDialogOpen] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<StockMovementFormData>({
    resolver: zodResolver(stockMovementSchema),
    defaultValues: { productId: '', type: 'ENTRY', quantity: 1, unitPrice: 0, reason: 'PURCHASE' },
  })

  async function onSubmit(data: StockMovementFormData) {
    await createMovement.mutateAsync(data)
    reset({ productId: '', type: 'ENTRY', quantity: 1, unitPrice: 0, reason: 'PURCHASE' })
    setDialogOpen(false)
  }

  return (
    <div className="space-y-4 pt-4">
      <div className="flex justify-end">
        <Button onClick={() => setDialogOpen(true)}>
          <Plus className="h-4 w-4" />
          Nova movimentação
        </Button>
      </div>

      <div className="rounded-lg border border-border">
        {isLoading && <p className="p-4 text-sm text-muted-foreground">Carregando...</p>}
        {!isLoading && (data?.content.length ?? 0) === 0 && (
          <p className="p-4 text-sm text-muted-foreground">Nenhuma movimentação registrada.</p>
        )}
        {data?.content.map((movement) => (
          <div key={movement.id} className="flex items-center justify-between border-b border-border p-3 last:border-0">
            <div className="flex items-center gap-3">
              {movement.type === 'ENTRY' ? (
                <ArrowUpCircle className="h-4 w-4 text-success" />
              ) : (
                <ArrowDownCircle className="h-4 w-4 text-destructive" />
              )}
              <div>
                <p className="text-sm font-medium">{movement.productName}</p>
                <p className="text-xs text-muted-foreground">{formatDateTime(movement.createdAt)} {movement.reason ? `- ${movement.reason}` : ''}</p>
              </div>
            </div>
            <Badge variant={movement.type === 'ENTRY' ? 'success' : 'destructive'}>
              {movement.type === 'ENTRY' ? '+' : '-'}
              {movement.quantity}
            </Badge>
          </div>
        ))}
      </div>

      <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Nova movimentação de estoque</DialogTitle>
          </DialogHeader>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Label className="mb-1.5 block">Produto</Label>
              <Select value={watch('productId')} onValueChange={(value) => setValue('productId', value)}>
                <SelectTrigger>
                  <SelectValue placeholder="Selecione" />
                </SelectTrigger>
                <SelectContent>
                  {(products?.content ?? []).map((product) => (
                    <SelectItem key={product.id} value={product.id}>
                      {product.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {errors.productId && <p className="mt-1 text-xs text-destructive">{errors.productId.message}</p>}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="mb-1.5 block">Tipo</Label>
                <Select value={watch('type')} onValueChange={(value) => setValue('type', value as StockMovementType)}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="ENTRY">Entrada</SelectItem>
                    <SelectItem value="EXIT">Saída</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label className="mb-1.5 block">Quantidade</Label>
                <Input type="number" {...register('quantity', { valueAsNumber: true })} />
                {errors.quantity && <p className="mt-1 text-xs text-destructive">{errors.quantity.message}</p>}
              </div>
              <div>
                <Label className="mb-1.5 block">Valor unitário</Label>
                <Input type="number" step="0.01" {...register('unitPrice', { valueAsNumber: true })} />
                {errors.unitPrice && <p className="mt-1 text-xs text-destructive">{errors.unitPrice.message}</p>}
              </div>
            </div>
            <div>
              <Label className="mb-1.5 block">Motivo</Label>
              <Select value={watch('reason')} onValueChange={(value) => setValue('reason', value as StockMovementReason)}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PURCHASE">Compra</SelectItem>
                  <SelectItem value="SALE">Venda</SelectItem>
                  <SelectItem value="ADJUSTMENT">Ajuste</SelectItem>
                  <SelectItem value="LOSS">Perda</SelectItem>
                </SelectContent>
              </Select>
              {errors.reason && <p className="mt-1 text-xs text-destructive">{errors.reason.message}</p>}
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
    </div>
  )
}
