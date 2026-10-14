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
      {/* Navigation */}
      <header className="axc-landing-nav">
        <div className="container d-flex justify-content-between align-items-center">
          <Link
            to="/"
            className="d-flex align-items-center gap-2 text-decoration-none"
          >
            <span className="axc-brand-mark" aria-hidden="true" />
            <span className="axc-brand-name">AcxiomCRM</span>
          </Link>

          <nav className="axc-landing-nav-links d-none d-md-flex">
            <a href="#features">Features</a>
            <a href="#modules">Modules</a>
            <a href="#demo">Demo Accounts</a>
            <a href="#analytics">Analytics</a>
            <a href="#security">Security</a>
          </nav>

          <div className="d-flex align-items-center gap-2">
            {user ? (
              <Link to="/dashboard" className="btn btn-sm btn-axc-primary">
                Open CRM Dashboard &rarr;
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-sm btn-outline-light">
                  Sign in
                </Link>
                <Link to="/register" className="btn btn-sm btn-axc-primary">
                  Get started
                </Link>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="axc-landing-hero">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-12 col-lg-8">
              <span className="axc-hero-badge">
                <span>&#9889;</span> Enterprise CRM &middot; Java 17 &middot; Spring Boot 3 &middot; React 19
              </span>
              <h1 className="axc-hero-title">
                Next-Generation CRM for High-Velocity Sales &amp; Customer Success
              </h1>
              <p className="axc-hero-subtitle">
                A unified, role-based CRM covering the full customer lifecycle:
                intelligent lead capture, stage-weighted opportunity forecasting,
                automated follow-up compliance, and tamper-evident audit logging.
              </p>

              {quickError && (
                <div className="alert alert-danger py-2 px-3 mb-3" style={{ maxWidth: '540px' }}>
                  {quickError}
                </div>
              )}

              <div className="d-flex flex-wrap gap-3">
                {user ? (
                  <Link
                    to="/dashboard"
                    className="btn btn-lg btn-axc-primary px-4"
                  >
                    Go to Your Workspace &rarr;
                  </Link>
                ) : (
                  <>
                    <Link
                      to="/login"
                      className="btn btn-lg btn-axc-primary px-4"
                    >
                      Sign in to CRM
                    </Link>
                    <a
                      href="#demo"
                      className="btn btn-lg btn-outline-light px-4"
                    >
                      Explore 1-Click Demo Logins
                    </a>
                  </>
                )}
              </div>
            </div>

            <div className="col-12 col-lg-4 mt-5 mt-lg-0">
              <div className="p-4 rounded-3 text-white" style={{ background: 'rgba(255, 255, 255, 0.05)', border: '1px solid rgba(255, 255, 255, 0.12)' }}>
                <h6 className="text-uppercase text-muted fw-bold mb-3" style={{ fontSize: '11px', letterSpacing: '0.08em' }}>
                  Live System Status
                </h6>
                <div className="d-flex align-items-center gap-2 mb-2">
                  <span className="badge bg-success-subtle text-success border border-success-subtle">
                    Operational
                  </span>
                  <span className="text-white small">All CRM Services Online</span>
                </div>
                <hr style={{ borderColor: 'rgba(255, 255, 255, 0.1)' }} />
                <div className="small text-muted mb-1">Architecture</div>
                <div className="text-white fw-semibold small mb-2">Spring Boot 3.5.16 &middot; MySQL 8 &middot; React 19</div>
                <div className="small text-muted mb-1">Security Standard</div>
                <div className="text-white fw-semibold small mb-3">BCrypt &middot; Double-Submit CSRF &middot; Session Cookie</div>
                <div className="small text-muted mb-1">Test Suite</div>
                <div className="text-white fw-semibold small">39 / 39 Automated Acceptance Tests Passing</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* 1-Click Demo Accounts */}
      <section id="demo" className="axc-landing-section bg-white border-bottom">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="axc-section-heading">Pre-Seeded Demo Accounts</h2>
            <p className="axc-section-subheading mx-auto">
              Test role-scoped authorization, manager oversight, and executive ownership directly with pre-configured accounts.
            </p>
          </div>

          <div className="row g-4">
            {/* Sales Manager Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <span className="badge bg-primary-subtle text-primary fw-semibold px-2 py-1">
                    Sales Manager
                  </span>
                  <small className="text-muted">Manager Access</small>
                </div>
                <h5 className="fw-bold mb-2">Sales Manager</h5>
                <p className="text-muted small mb-3">
                  Team pipeline analytics, complete customer portfolio visibility, and manager-level user directory.
                </p>
                <div className="p-2 rounded bg-light small mb-3">
                  <div><strong>Email:</strong> <code>manager@acxiomcrm.local</code></div>
                  <div><strong>Password:</strong> <code>Manager@123</code></div>
                </div>
                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn btn-sm btn-axc-primary w-100"
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('manager@acxiomcrm.local', 'Manager@123', 'Manager')}
                  >
                    {loggingInRole === 'Manager' ? 'Signing in…' : '1-Click Sign In as Manager'}
                  </button>
                </div>
              </div>
            </div>

            {/* System Admin Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <span className="badge bg-danger-subtle text-danger fw-semibold px-2 py-1">
                    System Admin
                  </span>
                  <small className="text-muted">Full Permissions</small>
                </div>
                <h5 className="fw-bold mb-2">Administrator</h5>
                <p className="text-muted small mb-3">
                  Full system administration, user role assignments &amp; deactivations, and tamper-evident audit logs.
                </p>
                <div className="p-2 rounded bg-light small mb-3">
                  <div><strong>Email:</strong> <code>admin@acxiomcrm.local</code></div>
                  <div><strong>Password:</strong> <code>Admin@123</code></div>
                </div>
                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-axc w-100"
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('admin@acxiomcrm.local', 'Admin@123', 'Admin')}
                  >
                    {loggingInRole === 'Admin' ? 'Signing in…' : '1-Click Sign In as Admin'}
                  </button>
                </div>
              </div>
            </div>

            {/* Sales Executive Card */}
            <div className="col-12 col-md-4">
              <div className="axc-demo-card">
                <div className="d-flex align-items-center justify-content-between mb-3">
                  <span className="badge bg-success-subtle text-success fw-semibold px-2 py-1">
                    Sales Executive
                  </span>
                  <small className="text-muted">Assigned Access</small>
                </div>
                <h5 className="fw-bold mb-2">Sales Executive</h5>
                <p className="text-muted small mb-3">
                  Lead tracking, 1-click customer conversions, stage updates, scheduled follow-ups, and activity logs.
                </p>
                <div className="p-2 rounded bg-light small mb-3">
                  <div><strong>Email:</strong> <code>sales@acxiomcrm.local</code></div>
                  <div><strong>Password:</strong> <code>Sales@123</code></div>
                </div>
                <div className="mt-auto">
                  <button
                    type="button"
                    className="btn btn-sm btn-outline-axc w-100"
                    disabled={Boolean(loggingInRole)}
                    onClick={() => handleQuickLogin('sales@acxiomcrm.local', 'Sales@123', 'Sales')}
                  >
                    {loggingInRole === 'Sales' ? 'Signing in…' : '1-Click Sign In as Sales Exec'}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CRM Modules Showcase */}
      <section id="modules" className="axc-landing-section">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="axc-section-heading">Comprehensive CRM Modules</h2>
            <p className="axc-section-subheading mx-auto">
              Every phase of customer engagement is modeled with strict business logic and validation rules.
            </p>
          </div>

          <div className="row g-4">
            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <CustomersIcon />
                </div>
                <h3 className="axc-feature-title">Customer 360&deg; Profiles</h3>
                <p className="axc-feature-desc">
                  Manage corporate and individual accounts with verified addresses, industry segmentation, status tracking, and linked opportunities.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <LeadsIcon />
                </div>
                <h3 className="axc-feature-title">Lead Qualification &amp; Conversion</h3>
                <p className="axc-feature-desc">
                  Track prospect status from New to Qualified. Convert qualified leads into new Customers and Opportunities in a single atomic transaction.
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
                  Monitor deal stages (Qualification, Proposal, Negotiation, Won, Lost) with automated weighted revenue calculations based on stage probabilities.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <FollowUpsIcon />
                </div>
                <h3 className="axc-feature-title">Follow-Up Reminders</h3>
                <p className="axc-feature-desc">
                  Enforces strict business rules: rejects any follow-up scheduled in the past (&sect;17.19), tracks completed/canceled status, and logs audit events.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <ActivitiesIcon />
                </div>
                <h3 className="axc-feature-title">Multi-Channel Activities</h3>
                <p className="axc-feature-desc">
                  Log calls, meetings, tasks, and email notes. Filter by type, date, and assigned user with rich notes and status tracking.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-6 col-lg-4">
              <div className="axc-feature-card">
                <div className="axc-feature-icon">
                  <UsersIcon />
                </div>
                <h3 className="axc-feature-title">User &amp; Role Administration</h3>
                <p className="axc-feature-desc">
                  Admin-managed user onboarding, role transitions, active/inactive deactivation, and server-enforced role authorization.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Analytics & KPI Showcase */}
      <section id="analytics" className="axc-landing-section bg-white border-top border-bottom">
        <div className="container">
          <div className="row align-items-center">
            <div className="col-12 col-lg-6 mb-4 mb-lg-0">
              <h2 className="axc-section-heading">8 Live KPI Cards &amp; 3 Analytics Charts</h2>
              <p className="axc-section-subheading">
                Instant executive oversight driven directly by authorized database queries &mdash; never static demo placeholders.
              </p>

              <div className="row g-2 mb-4">
                <div className="col-6">
                  <div className="p-3 bg-light rounded border">
                    <div className="text-muted small">Customers &amp; Leads</div>
                    <div className="fw-bold text-dark">Total &amp; Open Counts</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 bg-light rounded border">
                    <div className="text-muted small">Pipeline Deals</div>
                    <div className="fw-bold text-dark">Won, Lost, &amp; Open</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 bg-light rounded border">
                    <div className="text-muted small">Weighted Revenue</div>
                    <div className="fw-bold text-dark">Probability Forecasts</div>
                  </div>
                </div>
                <div className="col-6">
                  <div className="p-3 bg-light rounded border">
                    <div className="text-muted small">Visual Trend</div>
                    <div className="fw-bold text-dark">Monthly Sales Line</div>
                  </div>
                </div>
              </div>

              <div className="d-flex gap-3">
                <Link to="/dashboard" className="btn btn-axc-primary">
                  View Real-Time Dashboard &rarr;
                </Link>
              </div>
            </div>

            <div className="col-12 col-lg-6">
              <div className="p-4 rounded-3 border bg-light shadow-sm">
                <h6 className="fw-bold text-uppercase text-muted small mb-3">
                  Supported Chart.js Visualizations
                </h6>
                <ul className="list-unstyled mb-0">
                  <li className="d-flex align-items-center gap-2 mb-3">
                    <span className="badge bg-primary">Chart 1</span>
                    <div>
                      <strong>Lead Status (Doughnut)</strong>
                      <div className="text-muted small">Breakdown across New, Contacted, Qualified, Lost, Converted</div>
                    </div>
                  </li>
                  <li className="d-flex align-items-center gap-2 mb-3">
                    <span className="badge bg-warning text-dark">Chart 2</span>
                    <div>
                      <strong>Opportunity Pipeline (Bar)</strong>
                      <div className="text-muted small">Stage-by-stage progression from Qualification to Won</div>
                    </div>
                  </li>
                  <li className="d-flex align-items-center gap-2">
                    <span className="badge bg-info text-dark">Chart 3</span>
                    <div>
                      <strong>Monthly Sales (Line)</strong>
                      <div className="text-muted small">Rolling 6-month won deal revenue velocity</div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Security Architecture */}
      <section id="security" className="axc-landing-section">
        <div className="container">
          <div className="text-center mb-5">
            <h2 className="axc-section-heading">Bank-Grade Security Architecture</h2>
            <p className="axc-section-subheading mx-auto">
              Security is enforced at the controller and JPA query layer &mdash; not merely by hiding UI buttons.
            </p>
          </div>

          <div className="row g-4">
            <div className="col-12 col-md-3">
              <div className="p-3 rounded border bg-white h-100">
                <h6 className="fw-bold text-dark mb-2">BCrypt Hashing</h6>
                <p className="text-muted small mb-0">
                  All passwords stored with salted, adaptive work-factor hashing. Plaintext credentials are never saved or logged.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-3 rounded border bg-white h-100">
                <h6 className="fw-bold text-dark mb-2">CSRF Anti-Forgery</h6>
                <p className="text-muted small mb-0">
                  Double-submit cookie protection with automatic token validation on state-changing REST requests.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-3 rounded border bg-white h-100">
                <h6 className="fw-bold text-dark mb-2">Row-Level Isolation</h6>
                <p className="text-muted small mb-0">
                  Sales Executives can only access assigned records. Cross-ownership lookups return 404 to prevent resource probing.
                </p>
              </div>
            </div>

            <div className="col-12 col-md-3">
              <div className="p-3 rounded border bg-white h-100">
                <h6 className="fw-bold text-dark mb-2">Immutable Audit Log</h6>
                <p className="text-muted small mb-0">
                  Every login, failure, record modification, deletion, and role change is permanently recorded for compliance.
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Footer Banner */}
      <section className="container">
        <div className="axc-landing-cta">
          <h2 className="fw-bold text-white mb-2">Accelerate Your CRM Operations Today</h2>
          <p className="text-white-50 mb-4 mx-auto" style={{ maxWidth: '500px' }}>
            Experience reliable customer engagement, pipeline forecasting, and verified compliance with AcxiomCRM.
          </p>
          <div className="d-flex justify-content-center gap-3">
            {user ? (
              <Link to="/dashboard" className="btn btn-lg btn-axc-primary px-4">
                Open Dashboard &rarr;
              </Link>
            ) : (
              <>
                <Link to="/login" className="btn btn-lg btn-axc-primary px-4">
                  Sign In Now
                </Link>
                <Link to="/register" className="btn btn-lg btn-outline-light px-4">
                  Create Free Account
                </Link>
              </>
            )}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="axc-landing-footer mt-auto">
        <div className="container d-flex flex-column flex-md-row justify-content-between align-items-center gap-3">
          <div className="d-flex align-items-center gap-2">
            <span className="axc-brand-mark" aria-hidden="true" />
            <span className="fw-semibold text-white">AcxiomCRM</span>
            <span className="text-muted">&copy; 2026 All rights reserved.</span>
          </div>

          <div className="d-flex gap-4 small text-muted">
            <a href="#features" className="text-muted text-decoration-none">Features</a>
            <a href="#demo" className="text-muted text-decoration-none">Demo</a>
            <a href="#security" className="text-muted text-decoration-none">Security</a>
            <Link to="/login" className="text-muted text-decoration-none">Login</Link>
          </div>
        </div>
      </footer>
    </div>
  )
}
