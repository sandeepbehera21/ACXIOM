import { useEffect, useState } from 'react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js'
import { Bar, Doughnut, Line } from 'react-chartjs-2'
import EmptyState from '../components/EmptyState'
import ErrorAlert from '../components/ErrorAlert'
import KpiTile from '../components/KpiTile'
import LoadingState from '../components/LoadingState'
import PageHeader from '../components/PageHeader'
import StatusBadge from '../components/StatusBadge'
import { extractApiError } from '../services/api'
import { getPipelineReport } from '../services/reportService'
import { formatCurrency, formatNumber } from '../utils/format'

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
)

export default function DashboardPage() {
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    async function load() {
      setLoading(true)
      setError('')
      try {
        const data = await getPipelineReport()
        if (active) setReport(data)
      } catch (err) {
        if (active) {
          setError(extractApiError(err).message)
          setReport(null)
        }
      } finally {
        if (active) setLoading(false)
      }
    }

    load()
    return () => {
      active = false
    }
  }, [])

  const value = (field) =>
    loading || !report || report[field] === undefined
      ? null
      : formatNumber(report[field])

  const money = (field) =>
    loading || !report || report[field] === undefined
      ? null
      : formatCurrency(report[field])

  const byStage = report?.byStage || []

  // Chart 1: Lead Status Chart Data (§17.12)
  const leadStatusData = {
    labels: ['New', 'Contacted', 'Qualified', 'Lost', 'Converted'],
    datasets: [
      {
        label: 'Leads',
        data: [
          report?.leadStatusCounts?.NEW || 0,
          report?.leadStatusCounts?.CONTACTED || 0,
          report?.leadStatusCounts?.QUALIFIED || 0,
          report?.leadStatusCounts?.LOST || 0,
          report?.leadStatusCounts?.CONVERTED || 0,
        ],
        backgroundColor: [
          '#3b82f6',
          '#8b5cf6',
          '#10b981',
          '#ef4444',
          '#06b6d4',
        ],
      },
    ],
  }

  // Chart 2: Opportunity Pipeline Chart Data (§17.12)
  const pipelineChartData = {
    labels: ['Qualification', 'Proposal', 'Negotiation', 'Won', 'Lost'],
    datasets: [
      {
        label: 'Opportunities',
        data: [
          report?.opportunityStageCounts?.QUALIFICATION || 0,
          report?.opportunityStageCounts?.PROPOSAL || 0,
          report?.opportunityStageCounts?.NEGOTIATION || 0,
          report?.opportunityStageCounts?.WON || 0,
          report?.opportunityStageCounts?.LOST || 0,
        ],
        backgroundColor: [
          '#f59e0b',
          '#6366f1',
          '#ec4899',
          '#10b981',
          '#ef4444',
        ],
      },
    ],
  }

  // Chart 3: Monthly Sales Chart Data (§17.12)
  const monthlySalesLabels = (report?.monthlySales || []).map((m) => m.month)
  const monthlySalesValues = (report?.monthlySales || []).map(
    (m) => Number(m.totalSales) || 0
  )

  const monthlySalesData = {
    labels: monthlySalesLabels.length ? monthlySalesLabels : ['Current'],
    datasets: [
      {
        label: 'Sales Revenue ($)',
        data: monthlySalesValues.length ? monthlySalesValues : [0],
        fill: false,
        borderColor: '#2563eb',
        tension: 0.2,
      },
    ],
  }

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle="Role-based overview, KPIs, and sales analytics."
      />

      {error && (
        <div className="mb-3">
          <ErrorAlert message={error} onDismiss={() => setError('')} />
        </div>
      )}

      {/* 8 Mandatory KPI Cards (§17.11) */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Total Customers" value={value('customerCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Total Leads" value={value('leadCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Open Leads" value={value('openLeadCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile
            label="Total Opportunities"
            value={value('totalOpportunities')}
          />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Open Opportunities" value={value('openCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Won Opportunities" value={value('wonCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile label="Lost Opportunities" value={value('lostCount')} />
        </div>
        <div className="col-12 col-sm-6 col-lg-3">
          <KpiTile
            label="Total Pipeline Value"
            value={money('totalOpenAmount')}
          />
        </div>
      </div>

      {/* 3 Mandatory Chart.js Charts (§17.12) */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-lg-4">
          <div className="axc-card h-100 p-3">
            <h5 className="axc-section-title mb-3">Lead Status</h5>
            <div style={{ maxHeight: '240px', position: 'relative' }}>
              {loading ? (
                <LoadingState label="Loading chart…" />
              ) : (
                <Doughnut
                  data={leadStatusData}
                  options={{
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } },
                  }}
                />
              )}
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-4">
          <div className="axc-card h-100 p-3">
            <h5 className="axc-section-title mb-3">Opportunity Pipeline</h5>
            <div style={{ maxHeight: '240px', position: 'relative' }}>
              {loading ? (
                <LoadingState label="Loading chart…" />
              ) : (
                <Bar
                  data={pipelineChartData}
                  options={{
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                  }}
                />
              )}
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-4">
          <div className="axc-card h-100 p-3">
            <h5 className="axc-section-title mb-3">Monthly Sales</h5>
            <div style={{ maxHeight: '240px', position: 'relative' }}>
              {loading ? (
                <LoadingState label="Loading chart…" />
              ) : (
                <Line
                  data={monthlySalesData}
                  options={{
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                  }}
                />
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Pipeline by Stage Table */}
      <div className="axc-card">
        <div className="axc-card__header">
          <h2 className="axc-section-title">Pipeline by stage</h2>
        </div>

        {loading ? (
          <LoadingState label="Loading pipeline…" />
        ) : byStage.length === 0 ? (
          <EmptyState
            title="No pipeline data yet"
            message="Stage totals appear once opportunities exist."
          />
        ) : (
          <div className="axc-table-wrap">
            <table className="axc-table">
              <caption className="visually-hidden">
                Opportunity totals grouped by pipeline stage
              </caption>
              <thead>
                <tr>
                  <th scope="col">Stage</th>
                  <th scope="col" className="axc-num">
                    Opportunities
                  </th>
                  <th scope="col" className="axc-num">
                    Total amount
                  </th>
                  <th scope="col" className="axc-num">
                    Weighted amount
                  </th>
                </tr>
              </thead>
              <tbody>
                {byStage.map((row) => (
                  <tr key={row.stage}>
                    <td>
                      <StatusBadge value={row.stage} />
                    </td>
                    <td className="axc-num">{formatNumber(row.count)}</td>
                    <td className="axc-num">{formatCurrency(row.totalAmount)}</td>
                    <td className="axc-num">
                      {formatCurrency(row.weightedAmount)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
