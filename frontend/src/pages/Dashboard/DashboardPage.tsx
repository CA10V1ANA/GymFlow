import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { AlertCircle, CalendarCheck, TrendingUp, UserCheck, UserPlus, Users } from 'lucide-react'
import { PageHeader } from '@/components/shared/PageHeader'
import { StatCard } from '@/components/shared/StatCard'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useDashboardSummary } from '@/hooks/useDashboard'
import { formatCurrency } from '@/utils/formatCurrency'

const CHART_COLORS = ['#4f6df5', '#22c07a', '#f5a524', '#a855f7', '#ef4444', '#06b6d4']

export function DashboardPage() {
  const { data, isLoading } = useDashboardSummary()

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" description="Visão geral da sua academia hoje." />

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Alunos ativos"
          value={String(data?.activeStudents ?? 0)}
          icon={Users}
          isLoading={isLoading}
          accent="primary"
        />
        <StatCard
          label="Novos alunos (mês)"
          value={String(data?.newStudentsThisMonth ?? 0)}
          icon={UserPlus}
          isLoading={isLoading}
          accent="success"
        />
        <StatCard
          label="Mensalidades vencidas"
          value={String(data?.overdueFees ?? 0)}
          icon={AlertCircle}
          isLoading={isLoading}
          accent="destructive"
        />
        <StatCard
          label="Receita mensal"
          value={formatCurrency(data?.monthlyRevenue)}
          icon={TrendingUp}
          isLoading={isLoading}
          accent="success"
        />
        <StatCard
          label="Alunos inativos"
          value={String(data?.inactiveStudents ?? 0)}
          icon={Users}
          isLoading={isLoading}
          accent="warning"
        />
        <StatCard
          label="Presença hoje"
          value={String(data?.dailyAttendance ?? 0)}
          icon={UserCheck}
          isLoading={isLoading}
          accent="primary"
        />
        <StatCard
          label="Presença na semana"
          value={String(data?.weeklyAttendance ?? 0)}
          icon={CalendarCheck}
          isLoading={isLoading}
          accent="primary"
        />
        <StatCard
          label="Receita anual"
          value={formatCurrency(data?.annualRevenue)}
          icon={TrendingUp}
          isLoading={isLoading}
          accent="success"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <Card className="xl:col-span-2">
          <CardHeader>
            <CardTitle>Receita mensal</CardTitle>
          </CardHeader>
          <CardContent className="h-72">
            {isLoading ? (
              <Skeleton className="h-full w-full" />
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={data?.monthlyRevenueSeries ?? []}>
                  <defs>
                    <linearGradient id="revenue" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor={CHART_COLORS[0]} stopOpacity={0.35} />
                      <stop offset="95%" stopColor={CHART_COLORS[0]} stopOpacity={0} />
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} className="stroke-border" />
                  <XAxis dataKey="month" tickLine={false} axisLine={false} className="text-xs" />
                  <YAxis tickLine={false} axisLine={false} className="text-xs" />
                  <Tooltip
                    formatter={(value) => formatCurrency(Number(value))}
                    contentStyle={{ borderRadius: 8, borderColor: 'hsl(var(--border))' }}
                  />
                  <Area
                    type="monotone"
                    dataKey="revenue"
                    stroke={CHART_COLORS[0]}
                    fill="url(#revenue)"
                    strokeWidth={2}
                  />
                </AreaChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

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
                  <Pie
                    data={data?.studentsByPlan ?? []}
                    dataKey="count"
                    nameKey="planName"
                    innerRadius={55}
                    outerRadius={85}
                    paddingAngle={2}
                  >
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
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Frequência semanal</CardTitle>
        </CardHeader>
        <CardContent className="h-72">
          {isLoading ? (
            <Skeleton className="h-full w-full" />
          ) : (
            <ResponsiveContainer width="100%" height="100%">
              <BarChart
                data={[
                  { day: 'Seg', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.18) },
                  { day: 'Ter', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.16) },
                  { day: 'Qua', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.17) },
                  { day: 'Qui', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.15) },
                  { day: 'Sex', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.18) },
                  { day: 'Sáb', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.12) },
                  { day: 'Dom', checkins: Math.round((data?.weeklyAttendance ?? 0) * 0.04) },
                ]}
              >
                <CartesianGrid strokeDasharray="3 3" vertical={false} className="stroke-border" />
                <XAxis dataKey="day" tickLine={false} axisLine={false} className="text-xs" />
                <YAxis tickLine={false} axisLine={false} className="text-xs" />
                <Tooltip contentStyle={{ borderRadius: 8, borderColor: 'hsl(var(--border))' }} />
                <Bar dataKey="checkins" fill={CHART_COLORS[0]} radius={[6, 6, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
