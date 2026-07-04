import { api } from '@/services/api'
import type { DashboardSummary } from '@/types/dashboard'

interface DashboardSummaryApiResponse {
  totalStudents: number
  newStudentsThisMonth: number
  activeStudents: number
  inactiveStudents: number
  overdueTransactions: number
  monthlyRevenue: number
  annualRevenue: number
  dailyAttendance: number
  weeklyAttendance: number
  studentsByPlan: Record<string, number>
  topProducts: Array<{
    productName: string
    totalSold: number
  }>
  revenueLastTwelveMonths: Array<{
    month: string
    revenue: number
  }>
}

export const dashboardService = {
  getSummary: async (): Promise<DashboardSummary> => {
    const { data } = await api.get<DashboardSummaryApiResponse>('/dashboard/summary')

    return {
      totalStudents: data.totalStudents,
      newStudentsThisMonth: data.newStudentsThisMonth,
      activeStudents: data.activeStudents,
      inactiveStudents: data.inactiveStudents,
      overdueFees: data.overdueTransactions,
      monthlyRevenue: data.monthlyRevenue,
      annualRevenue: data.annualRevenue,
      dailyAttendance: data.dailyAttendance,
      weeklyAttendance: data.weeklyAttendance,
      studentsByPlan: Object.entries(data.studentsByPlan ?? {}).map(([planName, count]) => ({
        planName,
        count,
      })),
      topProducts: (data.topProducts ?? []).map((product) => ({
        productName: product.productName,
        quantitySold: product.totalSold,
      })),
      monthlyRevenueSeries: data.revenueLastTwelveMonths ?? [],
    }
  },
}
