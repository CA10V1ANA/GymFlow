import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import { Download } from 'lucide-react'
import { toast } from 'sonner'
import { PageHeader } from '@/components/shared/PageHeader'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import { useDashboardSummary } from '@/hooks/useDashboard'
import { formatCurrency } from '@/utils/formatCurrency'

const CHART_COLORS = ['#4f6df5', '#22c07a', '#f5a524', '#a855f7', '#ef4444', '#06b6d4']

export function ReportsPage() {
  const { data, isLoading } = useDashboardSummary()

  return (
    <div className="space-y-6">
      <PageHeader
        title="Relatórios"
        description="Análises consolidadas sobre alunos, financeiro e produtos."
        actions={
          <Button variant="outline" onClick={() => toast('Exportação em breve.')}>
            <Download className="h-4 w-4" />
            Exportar
          </Button>
        }
      />

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>Alunos por plano</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            {isLoading ? (
              <Skeleton className="h-full w-full" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={data?.studentsByPlan ?? []} dataKey="count" nameKey="planName" outerRadius={90}>
                    {(data?.studentsByPlan ?? []).map((_, index) => (
                      <Cell key={index} fill={CHART_COLORS[index % CHART_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ borderRadius: 8, borderColor: 'hsl(var(--border))' }} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Produtos mais vendidos</CardTitle>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <Skeleton className="h-56 w-full" />
            ) : (
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Produto</TableHead>
                    <TableHead className="text-right">Qtd. vendida</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {(data?.topProducts ?? []).map((product) => (
                    <TableRow key={product.productName}>
                      <TableCell>{product.productName}</TableCell>
                      <TableCell className="text-right">{product.quantitySold}</TableCell>
                    </TableRow>
                  ))}
                  {(data?.topProducts ?? []).length === 0 && (
                    <TableRow>
                      <TableCell colSpan={2} className="text-center text-muted-foreground">
                        Nenhum dado disponível.
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            )}
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Resumo financeiro</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-4">
            <SummaryItem label="Receita mensal" value={formatCurrency(data?.monthlyRevenue)} isLoading={isLoading} />
            <SummaryItem label="Receita anual" value={formatCurrency(data?.annualRevenue)} isLoading={isLoading} />
            <SummaryItem label="Mensalidades vencidas" value={String(data?.overdueFees ?? 0)} isLoading={isLoading} />
            <SummaryItem label="Total de alunos" value={String(data?.totalStudents ?? 0)} isLoading={isLoading} />
          </div>
        </CardContent>
      </Card>
    </div>
  )
}

function SummaryItem({ label, value, isLoading }: { label: string; value: string; isLoading?: boolean }) {
  return (
    <div>
      <p className="text-xs text-muted-foreground">{label}</p>
      {isLoading ? <Skeleton className="mt-1 h-6 w-20" /> : <p className="text-lg font-semibold">{value}</p>}
    </div>
  )
}
