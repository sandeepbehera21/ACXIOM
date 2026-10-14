import { useCallback, useState } from 'react'
import { Outlet } from 'react-router-dom'
import { useAuth } from '../services/AuthContext'
import Sidebar from './Sidebar'
import TopBar from './TopBar'

/**
 * Application shell: fixed navy sidebar plus a top bar, with the routed page
 * rendered inside. Under 992px the sidebar becomes an off-canvas drawer that
 * closes on navigation (see the Sidebar's onNavigate).
 */
export default function AppLayout() {
  const { user } = useAuth()
  const [drawerOpen, setDrawerOpen] = useState(false)

  const closeDrawer = useCallback(() => setDrawerOpen(false), [])

  return (
    <div className="axc-shell">
      <Sidebar
        role={user?.role}
        user={user}
        open={drawerOpen}
        onNavigate={closeDrawer}
      />

      {drawerOpen && (
        <button
          type="button"
          className="axc-sidebar-backdrop"
          aria-label="Close navigation"
          onClick={closeDrawer}
        />
      )}

      <div className="axc-main">
        <TopBar user={user} onToggleSidebar={() => setDrawerOpen((v) => !v)} />
        <main className="axc-content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
