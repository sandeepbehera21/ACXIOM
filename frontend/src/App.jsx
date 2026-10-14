import { useEffect } from 'react'
import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import RequireRole from './components/RequireRole'
import AppLayout from './layouts/AppLayout'
import ActivityListPage from './pages/ActivityListPage'
import AuditLogPage from './pages/AuditLogPage'
import CustomerFormPage from './pages/CustomerFormPage'
import CustomerListPage from './pages/CustomerListPage'
import FollowUpListPage from './pages/FollowUpListPage'
import HomePage from './pages/HomePage'
import LandingPage from './pages/LandingPage'
import LeadFormPage from './pages/LeadFormPage'
import LeadListPage from './pages/LeadListPage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import OpportunityFormPage from './pages/OpportunityFormPage'
import OpportunityListPage from './pages/OpportunityListPage'
import RegisterPage from './pages/RegisterPage'
import UserListPage from './pages/UserListPage'
import { useAuth } from './services/AuthContext'
import { ROLES } from './utils/constants'

function RootRoute() {
  const { user, loading } = useAuth()
  if (loading) {
    return (
      <div className="axc-fullpage-loading">
        <span className="axc-spinner" aria-hidden="true" />
        Loading AcxiomCRM…
      </div>
    )
  }
  if (!user) {
    return <LandingPage />
  }
  return <Navigate to="/dashboard" replace />
}

export default function App() {
  useEffect(() => {
    document.title = 'AcxiomCRM — Enterprise CRM Platform'
  }, [])

  return (
    <Routes>
      {/* Public routes */}
      <Route path="/" element={<RootRoute />} />
      <Route path="/landing" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Protected CRM workspace */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/dashboard" element={<HomePage />} />
          <Route path="/home" element={<HomePage />} />

          <Route path="/customers" element={<CustomerListPage />} />
          <Route path="/customers/new" element={<CustomerFormPage />} />
          <Route path="/customers/:id/edit" element={<CustomerFormPage />} />

          <Route path="/leads" element={<LeadListPage />} />
          <Route path="/leads/new" element={<LeadFormPage />} />
          <Route path="/leads/:id/edit" element={<LeadFormPage />} />

          <Route path="/opportunities" element={<OpportunityListPage />} />
          <Route path="/opportunities/new" element={<OpportunityFormPage />} />
          <Route
            path="/opportunities/:id/edit"
            element={<OpportunityFormPage />}
          />

          <Route path="/followups" element={<FollowUpListPage />} />
          <Route path="/activities" element={<ActivityListPage />} />

          <Route
            path="/users"
            element={
              <RequireRole role={[ROLES.ADMIN, ROLES.MANAGER]}>
                <UserListPage />
              </RequireRole>
            }
          />

          <Route
            path="/audit"
            element={
              <RequireRole role={ROLES.ADMIN}>
                <AuditLogPage />
              </RequireRole>
            }
          />

          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
