import { useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import RequireRole from './components/RequireRole'
import AppLayout from './layouts/AppLayout'
import ActivityListPage from './pages/ActivityListPage'
import AuditLogPage from './pages/AuditLogPage'
import CustomerFormPage from './pages/CustomerFormPage'
import CustomerListPage from './pages/CustomerListPage'
import DashboardPage from './pages/DashboardPage'
import FollowUpListPage from './pages/FollowUpListPage'
import LeadFormPage from './pages/LeadFormPage'
import LeadListPage from './pages/LeadListPage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import OpportunityFormPage from './pages/OpportunityFormPage'
import OpportunityListPage from './pages/OpportunityListPage'
import RegisterPage from './pages/RegisterPage'
import UserListPage from './pages/UserListPage'
import { ROLES } from './utils/constants'

export default function App() {
  useEffect(() => {
    document.title = 'AcxiomCRM'
  }, [])

  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />

          <Route path="customers" element={<CustomerListPage />} />
          <Route path="customers/new" element={<CustomerFormPage />} />
          <Route path="customers/:id/edit" element={<CustomerFormPage />} />

          <Route path="leads" element={<LeadListPage />} />
          <Route path="leads/new" element={<LeadFormPage />} />
          <Route path="leads/:id/edit" element={<LeadFormPage />} />

          <Route path="opportunities" element={<OpportunityListPage />} />
          <Route path="opportunities/new" element={<OpportunityFormPage />} />
          <Route
            path="opportunities/:id/edit"
            element={<OpportunityFormPage />}
          />

          <Route path="followups" element={<FollowUpListPage />} />
          <Route path="activities" element={<ActivityListPage />} />

          <Route
            path="users"
            element={
              <RequireRole role={[ROLES.ADMIN, ROLES.MANAGER]}>
                <UserListPage />
              </RequireRole>
            }
          />

          <Route
            path="audit"
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
