import { NavLink } from 'react-router-dom'
import {
  ActivitiesIcon,
  AuditIcon,
  CustomersIcon,
  DashboardIcon,
  FollowUpsIcon,
  LeadsIcon,
  OpportunitiesIcon,
  UsersIcon,
} from '../components/Icons'
import { ROLES } from '../utils/constants'

/**
 * Sidebar navigation, grouped and filtered by role.
 * Visibility here is convenience only — the API enforces authorisation.
 */
const NAV_GROUPS = [
  {
    label: 'Overview',
    items: [
      {
        to: '/dashboard',
        label: 'Dashboard',
        end: true,
        icon: <DashboardIcon />,
      },
    ],
  },
  {
    label: 'Sales',
    items: [
      {
        to: '/customers',
        label: 'Customers',
        icon: <CustomersIcon />,
      },
      {
        to: '/leads',
        label: 'Leads',
        icon: <LeadsIcon />,
      },
      {
        to: '/opportunities',
        label: 'Opportunities',
        icon: <OpportunitiesIcon />,
      },
      {
        to: '/followups',
        label: 'Follow-Ups',
        icon: <FollowUpsIcon />,
      },
      {
        to: '/activities',
        label: 'Activities',
        icon: <ActivitiesIcon />,
      },
    ],
  },
  {
    label: 'Administration',
    items: [
      {
        to: '/users',
        label: 'Users & Roles',
        icon: <UsersIcon />,
        roles: [ROLES.ADMIN, ROLES.MANAGER],
      },
      {
        to: '/audit',
        label: 'Audit Log',
        icon: <AuditIcon />,
        roles: [ROLES.ADMIN],
      },
    ],
  },
]

export default function Sidebar({ role, user, open, onNavigate }) {
  const groups = NAV_GROUPS.map((group) => ({
    ...group,
    items: group.items.filter(
      (item) => !item.roles || item.roles.includes(role),
    ),
  })).filter((group) => group.items.length > 0)

  return (
    <aside className={`axc-sidebar${open ? ' is-open' : ''}`}>
      <div className="axc-sidebar__brand">
        <span className="axc-brand-mark" aria-hidden="true" />
        <span className="axc-brand-name">AcxiomCRM</span>
      </div>

      <nav className="axc-sidebar__nav" aria-label="Main navigation">
        {groups.map((group) => (
          <div key={group.label}>
            <div className="axc-nav-group-label">{group.label}</div>
            {group.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                onClick={onNavigate}
                className={({ isActive }) =>
                  `axc-nav-link${isActive ? ' is-active' : ''}`
                }
              >
                {item.icon}
                <span>{item.label}</span>
              </NavLink>
            ))}
          </div>
        ))}
      </nav>

      <div className="axc-sidebar__footer">
        {user ? (
          <>
            <div>Signed in as</div>
            <div style={{ color: '#ffffff', fontWeight: 500 }}>{user.name}</div>
          </>
        ) : (
          <div>AcxiomCRM</div>
        )}
      </div>
    </aside>
  )
}
