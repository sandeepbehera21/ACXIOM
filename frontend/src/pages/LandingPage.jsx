import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ActivitiesIcon,
  CustomersIcon,
  FollowUpsIcon,
  LeadsIcon,
  OpportunitiesIcon,
  UsersIcon,
} from '../components/Icons'
import { useAuth } from '../services/AuthContext'
import { extractApiError } from '../services/api'
import PublicNavbar from '../components/PublicNavbar'

export default function LandingPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [loggingInRole, setLoggingInRole] = useState('')
  const [quickError, setQuickError] = useState('')

  async function handleQuickLogin(email, password, roleName) {
    setQuickError('')
    setLoggingInRole(roleName)
    try {
      await login(email, password)
      navigate('/dashboard', { replace: true })
    } catch (err) {
      setQuickError(extractApiError(err).message || 'Quick login failed')
    } finally {
      setLoggingInRole('')
    }
  }

  return (
    <div className="axc-landing">
      {/* Sticky Glass Navbar */}
      <PublicNavbar activePage="landing" />

      {/* Hero Section */}
      <section className="axc-landing-hero">
        <div className="container">
          <div className="row align-items-center g-5">
            <div className="col-12 col-lg-7">
              <div className="axc-hero-badge">
                <span className="axc-pulse-dot" />
                <span>Enterprise Platform &middot; Java 17 &middot; Spring Boot 3 &middot; React 19</span>
              </div>

              <h1 className="axc-hero-title">
                Next-Generation CRM for <span className="axc-gradient-text">High-Velocity Sales</span> &amp; Customer Success
              </h1>

              <p className="axc-hero-subtitle">
                A unified, role-based CRM covering the full customer lifecycle:
                intelligent lead capture, stage-weighted opportunity forecasting,
                automated follow-up compliance, and tamper-evident audit logging.
              </p>

              {quickError && (
                <div className="alert alert-danger py-2 px-3 mb-4 rounded-3" style={{ maxWidth: '540px' }}>
                  {quickError}
                </div>
              )}

              <div className="d-flex flex-wrap gap-3 align-items-center">
                {user ? (
                  <Link
                    to="/dashboard"
                    className="btn btn-lg axc-btn-glow px-4 py-3 rounded-3"
                  >
                    Go to Your Workspace →
                  </Link>
                ) : (
                  <>
                    <Link
                      to="/login"
                      className="btn btn-lg axc-btn-glow px-4 py-3 rounded-3"
                    >
                      Sign in to CRM
                    </Link>
                    <a
                      href="#demo"
                      className="btn btn-lg btn-outline-light px-4 py-3 rounded-3"
                      style={{ borderColor: 'rgba(255,255,255,0.25)', background: 'rgba(255,255,255,0.03)' }}
                    >
                      ⚡ 1-Click Demo Logins
                    </a>
                  </>
                )}
              </div>
            </div>

            {/* Right Column: Live Mock CRM Dashboard Preview */}
            <div className="col-12 col-lg-5">
              <div className="axc-hero-preview-card">
                <div className="axc-preview-header">
                  <div className="axc-preview-dots">
                    <span className="axc-dot" style={{ backgroundColor: '#ef4444' }} />
                    <span className="axc-dot" style={{ backgroundColor: '#f59e0b' }} />
                    <span className="axc-dot" style={{ backgroundColor: '#10b981' }} />
                  </div>
                  <div className="small fw-semibold text-light text-opacity-75">
                    AcxiomCRM Live Pipeline
                  </div>
                  <div className="d-flex align-items-center gap-1">
                    <span className="axc-pulse-dot" />
                    <span className="small text-success" style={{ fontSize: '11px' }}>Port 8080 Active</span>
                  </div>
                </div>

                <div className="p-4">
                  {/* Top Metric Preview */}
                  <div className="d-flex justify-content-between align-items-end mb-4 pb-3 border-bottom border-light border-opacity-10">
                    <div>
                      <div className="text-muted small text-uppercase fw-semibold" style={{ fontSize: '11px', letterSpacing: '0.06em' }}>
                        Weighted Pipeline Forecast
                      </div>
                      <div className="fs-2 fw-bold text-white mt-1">
                        $284,500<span className="fs-6 text-muted fw-normal">.00</span>
                      </div>
                    </div>
                    <span className="badge bg-success bg-opacity-25 text-success border border-success border-opacity-25 px-2 py-1 small">
                      &uarr; 24.8% this month
                    </span>
                  </div>

                  {/* Pipeline Stage Bars */}
                  <div className="mb-4">
                    <div className="d-flex justify-content-between small text-muted mb-1">
                      <span>Proposal Stage (60% Prob)</span>
                      <span className="text-white fw-semibold">$142,000</span>
                    </div>
                    <div className="progress mb-3" style={{ height: '6px', background: 'rgba(255,255,255,0.1)' }}>
                      <div className="progress-bar bg-info" style={{ width: '65%' }} />
                    </div>

                    <div className="d-flex justify-content-between small text-muted mb-1">
                      <span>Negotiation Stage (80% Prob)</span>
                      <span className="text-white fw-semibold">$98,500</span>
                    </div>
                    <div className="progress mb-2" style={{ height: '6px', background: 'rgba(255,255,255,0.1)' }}>
                      <div className="progress-bar bg-primary" style={{ width: '80%' }} />
                    </div>
                  </div>

                  {/* Live Activity Feeds */}
                  <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.03)', border: '1px solid rgba(255,255,255,0.06)' }}>
                    <div className="small text-muted text-uppercase fw-bold mb-2" style={{ fontSize: '10px', letterSpacing: '0.05em' }}>
                      Recent Operations
                    </div>
                    <div className="d-flex justify-content-between align-items-center mb-2">
                      <div className="small text-white">Acme Global &middot; Follow-up scheduled</div>
                      <span className="badge bg-info-subtle text-info border border-info-subtle" style={{ fontSize: '10px' }}>Tomorrow</span>
                    </div>
                    <div className="d-flex justify-content-between align-items-center">
                      <div className="small text-white">Apex Industries &middot; Converted to Customer</div>
                      <span className="badge bg-success-subtle text-success border border-success-subtle" style={{ fontSize: '10px' }}>Won $45,000</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Metrics & Architecture Stat Ribbon */}
      <section className="axc-stat-ribbon">
        <div className="container">
          <div className="row g-4 text-center">
            <div className="col-6 col-md-3">
              <div className="axc-stat-item">
                <div className="axc-stat-number">39 / 39</div>
                <div className="axc-stat-label">Automated Tests Passing</div>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="axc-stat-item">
                <div className="axc-stat-number">100%</div>
                <div className="axc-stat-label">Server-Enforced RBAC</div>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="axc-stat-item">
                <div className="axc-stat-number">8 KPIs</div>
                <div className="axc-stat-label">&amp; 3 Live Chart Types</div>
              </div>
            </div>
            <div className="col-6 col-md-3">
              <div className="axc-stat-item">
                <div className="axc-stat-number">Java 17</div>
                <div className="axc-stat-label">Spring Boot 3 + React 19</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Pre-Seeded Demo Accounts Section */}
      <section id="demo" className="axc-landing-section axc-landing-section--light">
        <div className="container">
          <div className="text-center mb-5">
            <span className="badge bg-primary-subtle text-primary fw-semibold px-3 py-2 rounded-pill mb-2">
              ⚡ Instant Evaluation
            </span>
            <h2 className="axc-section-heading">Pre-Seeded Demo Accounts</h2>
            <p className="axc-section-subheading mx-auto">
              Test role-scoped authorization, team manager oversight, and executive ownership directly with 1 click.
            </p>
          </div>

          <div className="row g-4">
            {/* Sales Manager Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card axc-demo-card--manager">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="axc-role-avatar bg-primary-subtle text-primary">
                    <CustomersIcon />
                  </div>
                  <span className="badge bg-primary-subtle text-primary fw-semibold px-2 py-1">
                    Manager Access
                  </span>
                </div>

                <h4 className="fw-bold text-dark mb-1">Sales Manager</h4>
                <p className="text-muted small mb-3">
                  Team pipeline analytics, portfolio customer oversight, and manager-level user directory.
                </p>

                <div className="axc-cred-box">
                  <div className="mb-1 text-muted small">Email: <code>manager@acxiomcrm.local</code></div>
                  <div className="text-muted small">Password: <code>Manager@123</code></div>
                </div>

                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn btn-primary w-100 py-2 fw-semibold"
                    style={{ borderRadius: '10px' }}
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('manager@acxiomcrm.local', 'Manager@123', 'Manager')}
                  >
                    {loggingInRole === 'Manager' ? 'Signing in…' : '⚡ 1-Click Sign In as Manager'}
                  </button>
                </div>
              </div>
            </div>

            {/* System Admin Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card axc-demo-card--admin">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="axc-role-avatar bg-purple-subtle text-purple" style={{ backgroundColor: '#f3e8ff', color: '#7c3aed' }}>
                    <UsersIcon />
                  </div>
                  <span className="badge bg-danger-subtle text-danger fw-semibold px-2 py-1">
                    System Admin
                  </span>
                </div>

                <h4 className="fw-bold text-dark mb-1">Administrator</h4>
                <p className="text-muted small mb-3">
                  Full system control, user role management &amp; deactivations, and security audit log inspection.
                </p>

                <div className="axc-cred-box">
                  <div className="mb-1 text-muted small">Email: <code>admin@acxiomcrm.local</code></div>
                  <div className="text-muted small">Password: <code>Admin@123</code></div>
                </div>

                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn text-white w-100 py-2 fw-semibold"
                    style={{ background: '#7c3aed', borderRadius: '10px' }}
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('admin@acxiomcrm.local', 'Admin@123', 'Admin')}
                  >
                    {loggingInRole === 'Admin' ? 'Signing in…' : '⚡ 1-Click Sign In as Admin'}
                  </button>
                </div>
              </div>
            </div>

            {/* Sales Executive Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card axc-demo-card--sales">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <div className="axc-role-avatar bg-success-subtle text-success">
                    <OpportunitiesIcon />
                  </div>
                  <span className="badge bg-success-subtle text-success fw-semibold px-2 py-1">
                    Sales Executive
                  </span>
                </div>

                <h4 className="fw-bold text-dark mb-1">Sales Executive</h4>
                <p className="text-muted small mb-3">
                  Lead management, opportunity progression, scheduled follow-ups, and activity interaction logs.
                </p>

                <div className="axc-cred-box">
                  <div className="mb-1 text-muted small">Email: <code>sales@acxiomcrm.local</code></div>
                  <div className="text-muted small">Password: <code>Sales@123</code></div>
                </div>

                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn btn-success w-100 py-2 fw-semibold"
                    style={{ borderRadius: '10px' }}
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('sales@acxiomcrm.local', 'Sales@123', 'Sales')}
                  >
                    {loggingInRole === 'Sales' ? 'Signing in…' : '⚡ 1-Click Sign In as Sales Exec'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CRM Modules Grid */}
      <section id="modules" className="axc-landing-section axc-landing-section--light border-top">
        <div className="container">
          <div className="text-center mb-5">
            <span className="badge bg-secondary-subtle text-secondary fw-semibold px-3 py-2 rounded-pill mb-2">
              Modular Capabilities
            </span>
            <h2 className="axc-section-heading">Engineered for the Full Sales Lifecycle</h2>
            <p className="axc-section-subheading mx-auto">
              Every stage of customer engagement is governed by server-side validation and atomic consistency.
            </p>
          </div>

          <div className="row g-4">
            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <CustomersIcon />
                </div>
                <h3 className="axc-feature-title">Customer 360&deg; Management</h3>
                <p className="axc-feature-desc">
                  Maintain verified company and contact records with active status toggling, industry classification, and linked sales opportunities.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <LeadsIcon />
                </div>
                <h3 className="axc-feature-title">Intelligent Lead Conversion</h3>
                <p className="axc-feature-desc">
                  Capture inbound prospects, qualify interest, and execute a 1-click conversion into both a Customer account and active Opportunity.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <OpportunitiesIcon />
                </div>
                <h3 className="axc-feature-title">Stage-Weighted Pipeline</h3>
                <p className="axc-feature-desc">
                  Track deals across Qualification, Proposal, Negotiation, Won, and Lost with automatic weighted value calculation.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <FollowUpsIcon />
                </div>
                <h3 className="axc-feature-title">Strict Follow-Up Rules</h3>
                <p className="axc-feature-desc">
                  Built-in validation rejecting past dates (&sect;17.19), automated reminders, status workflow, and permanent audit logging.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <ActivitiesIcon />
                </div>
                <h3 className="axc-feature-title">Multi-Channel Activity Logging</h3>
                <p className="axc-feature-desc">
                  Log phone calls, video meetings, tasks, and email notes. Search and filter by activity type, date range, and owner.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <UsersIcon />
                </div>
                <h3 className="axc-feature-title">Role-Based Access Control</h3>
                <p className="axc-feature-desc">
                  Complete user administration, role assignment (Admin/Manager/Sales), account lockouts, and row-level ownership isolation.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Analytics & KPI Showcase */}
      <section id="analytics" className="axc-landing-section bg-dark text-white border-top border-bottom" style={{ background: '#090e17' }}>
        <div className="container">
          <div className="row align-items-center g-5">
            <div className="col-12 col-lg-6">
              <span className="badge bg-info-subtle text-info fw-semibold px-3 py-2 rounded-pill mb-3">
                Live Data Intelligence
              </span>
              <h2 className="axc-section-heading text-white">8 Real-Time KPI Cards &amp; 3 Chart.js Visualizations</h2>
              <p className="axc-section-subheading text-muted mb-4">
                Executive analytics rendered from real authorized database queries &mdash; zero placeholder demo counts.
              </p>

              <div className="row g-3 mb-4">
                <div className="col-6">
                  <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <div className="text-muted small">Total Customers &amp; Leads</div>
                    <div className="fw-bold text-white fs-5 mt-1">Real-time DB Counts</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <div className="text-muted small">Deal Velocity</div>
                    <div className="fw-bold text-white fs-5 mt-1">Won vs. Lost Breakdown</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <div className="text-muted small">Weighted Revenue</div>
                    <div className="fw-bold text-white fs-5 mt-1">Probability Forecasts</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.04)', border: '1px solid rgba(255,255,255,0.08)' }}>
                    <div className="text-muted small">Revenue Velocity</div>
                    <div className="fw-bold text-white fs-5 mt-1">6-Month Trend Line</div>
                  </div>
                </div>
              </div>

              <Link to="/dashboard" className="btn btn-lg axc-btn-glow px-4 py-3 rounded-3">
                Inspect Real-Time Analytics →
              </Link>
            </div>

            <div className="col-12 col-lg-6">
              <div className="p-4 rounded-4" style={{ background: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)' }}>
                <h6 className="fw-bold text-uppercase text-muted small mb-3">
                  Interactive Chart Breakdown
                </h6>
                <div className="p-3 rounded-3 mb-3" style={{ background: 'rgba(255,255,255,0.03)' }}>
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <span className="badge bg-primary">Chart 1</span>
                    <strong className="text-white">Lead Status (Doughnut)</strong>
                  </div>
                  <p className="text-muted small mb-0">Segments prospects across New, Contacted, Qualified, Lost, and Converted.</p>
                </div>
                <div className="p-3 rounded-3 mb-3" style={{ background: 'rgba(255,255,255,0.03)' }}>
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <span className="badge bg-warning text-dark">Chart 2</span>
                    <strong className="text-white">Opportunity Pipeline (Bar)</strong>
                  </div>
                  <p className="text-muted small mb-0">Visualizes opportunity counts across all 5 deal progression stages.</p>
                </div>
                <div className="p-3 rounded-3" style={{ background: 'rgba(255,255,255,0.03)' }}>
                  <div className="d-flex align-items-center gap-2 mb-1">
                    <span className="badge bg-info text-dark">Chart 3</span>
                    <strong className="text-white">Monthly Sales (Line)</strong>
                  </div>
                  <p className="text-muted small mb-0">Rolling 6-month historical won deal sales velocity.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Security Architecture */}
      <section id="security" className="axc-landing-section axc-landing-section--light">
        <div className="container">
          <div className="text-center mb-5">
            <span className="badge bg-danger-subtle text-danger fw-semibold px-3 py-2 rounded-pill mb-2">
              Zero-Trust Protection
            </span>
            <h2 className="axc-section-heading">Bank-Grade Security Architecture</h2>
            <p className="axc-section-subheading mx-auto">
              Protection is enforced on every controller endpoint and query &mdash; not merely in the frontend.
            </p>
          </div>

          <div className="row g-4">
            <div className="col-12 col-md-3">
              <div className="p-4 rounded-4 border bg-white h-100 shadow-sm">
                <h5 className="fw-bold text-dark mb-2">BCrypt Hashing</h5>
                <p className="text-muted small mb-0">
                  All passwords stored with adaptive, salted work factor. Plaintext credentials are never saved or exposed.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-4 rounded-4 border bg-white h-100 shadow-sm">
                <h5 className="fw-bold text-dark mb-2">Double-Submit CSRF</h5>
                <p className="text-muted small mb-0">
                  Strict anti-forgery verification on all mutating requests. Cookie tokens are validated per session.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-4 rounded-4 border bg-white h-100 shadow-sm">
                <h5 className="fw-bold text-dark mb-2">Ownership Isolation</h5>
                <p className="text-muted small mb-0">
                  Sales Executives are restricted to their own assigned records. Cross-ownership returns 404 to block probing.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-4 rounded-4 border bg-white h-100 shadow-sm">
                <h5 className="fw-bold text-dark mb-2">Immutable Audit Log</h5>
                <p className="text-muted small mb-0">
                  Logins, failures, lockouts, record modifications, and role updates are permanently recorded for compliance.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* High-Impact CTA Banner */}
      <section className="container">
        <div className="axc-landing-cta">
          <h2 className="fw-bold text-white mb-3" style={{ fontSize: '36px' }}>Ready to Supercharge Your Sales Pipeline?</h2>
          <p className="text-light text-opacity-75 mb-4 mx-auto" style={{ maxWidth: '540px', fontSize: '16px' }}>
            Experience verified customer engagement, weighted forecasting, and rock-solid compliance with AcxiomCRM.
          </p>
          <div className="d-flex justify-content-center gap-3">
            {user ? (
              <Link to="/dashboard" className="btn btn-lg axc-btn-glow px-4 py-3 rounded-3">
                Open CRM Workspace →
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-lg axc-btn-glow px-4 py-3 rounded-3">
                  Sign In Now
                </Link>
                <Link to="/register" className="btn btn-lg btn-outline-light px-4 py-3 rounded-3">
                  Create Free Account
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      {/* Modern Dark Footer */}
      <footer className="axc-landing-footer mt-auto">
        <div className="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
          <div className="d-flex align-items-center gap-2">
            <span className="axc-brand-mark" aria-hidden="true" />
            <span className="fw-bold text-white">AcxiomCRM</span>
            <span className="text-muted ms-2">&copy; 2026 Enterprise Edition. All rights reserved.</span>
          </div>

          <div className="d-flex gap-4 text-muted" style={{ fontSize: '13px' }}>
            <a href="#features" className="text-muted text-decoration-none">Features</a>
            <a href="#demo" className="text-muted text-decoration-none">Demo Logins</a>
            <a href="#analytics" className="text-muted text-decoration-none">Analytics</a>
            <a href="#security" className="text-muted text-decoration-none">Security</a>
            <Link to="/login" className="text-muted text-decoration-none">Sign In</Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
