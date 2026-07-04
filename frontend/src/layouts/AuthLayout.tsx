import { Outlet } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Dumbbell } from 'lucide-react'

export function AuthLayout() {
  return (
    <div className="grid min-h-svh lg:grid-cols-2">
      <div className="relative flex flex-col justify-center px-6 py-12 sm:px-12 lg:px-20">
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(244,123,63,0.12),transparent_28%)]" />
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="relative mx-auto w-full max-w-sm"
        >
          <div className="mb-8 flex items-center gap-3">
            <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-primary via-primary to-warning text-primary-foreground shadow-[0_20px_40px_-22px_rgba(244,123,63,0.95)]">
              <Dumbbell className="h-5 w-5" />
            </div>
            <div>
              <p className="text-lg font-semibold">GymFlow Pro</p>
              <p className="text-[0.68rem] uppercase tracking-[0.24em] text-muted-foreground">Athletic Ops</p>
            </div>
          </div>
          <Outlet />
        </motion.div>
      </div>

      <div className="relative hidden overflow-hidden bg-[linear-gradient(140deg,#271711_0%,#5a2816_38%,#d8672c_100%)] lg:flex lg:flex-col lg:justify-between lg:p-12">
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.14),transparent_44%)]" />
        <div className="absolute bottom-[-120px] left-[-60px] h-72 w-72 rounded-full bg-emerald-300/10 blur-3xl" />

        <div className="relative z-10 max-w-lg text-primary-foreground">
          <p className="text-[0.7rem] font-semibold uppercase tracking-[0.28em] text-primary-foreground/70">
            Studio view
          </p>
          <h2 className="mt-5 text-4xl font-semibold leading-tight">
            Menos cara de painel pronto. Mais cara de produto que alguem quer usar todo dia.
          </h2>
          <p className="mt-4 max-w-md text-primary-foreground/80">
            Alunos, treinos, caixa e estoque organizados em uma interface com mais presenca visual e menos ruido de template.
          </p>
        </div>

        <div className="relative z-10 grid gap-3 text-primary-foreground">
          <div className="rounded-3xl border border-white/10 bg-white/[0.08] p-4 backdrop-blur-sm">
            <p className="text-[0.68rem] uppercase tracking-[0.2em] text-primary-foreground/70">Visao unica</p>
            <p className="mt-2 text-xl font-semibold">Operacao, equipe e recorrencia no mesmo fluxo.</p>
          </div>
        </div>
      </div>
    </div>
  )
}
