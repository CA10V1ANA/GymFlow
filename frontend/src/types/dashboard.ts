export interface StudentsByPlan {
  planName: string
  count: number
}

export interface TopProduct {
  productName: string
  quantitySold: number
}

export interface MonthlyRevenuePoint {
  month: string
  revenue: number
}

export interface DashboardSummary {
  totalStudents: number
  newStudentsThisMonth: number
  activeStudents: number
  inactiveStudents: number
  overdueFees: number
  monthlyRevenue: number
  annualRevenue: number
  dailyAttendance: number
  weeklyAttendance: number
  studentsByPlan: StudentsByPlan[]
  topProducts: TopProduct[]
  monthlyRevenueSeries: MonthlyRevenuePoint[]
}
