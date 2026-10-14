/**
 * Option lists shared by filters and forms. Values are the exact enum
 * constants the API accepts.
 */

export const CUSTOMER_STATUSES = ['PROSPECT', 'ACTIVE', 'INACTIVE']

export const LEAD_STATUSES = [
  'NEW',
  'CONTACTED',
  'QUALIFIED',
  'UNQUALIFIED',
  'CONVERTED',
  'LOST',
]

export const LEAD_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH']

export const OPPORTUNITY_STAGES = [
  'QUALIFICATION',
  'PROPOSAL',
  'NEGOTIATION',
  'WON',
  'LOST',
]

export const FOLLOW_UP_STATUSES = ['PLANNED', 'COMPLETED', 'MISSED', 'CANCELLED']

export const FOLLOW_UP_TYPES = ['CALL', 'MEETING', 'EMAIL', 'OTHER']

export const ACTIVITY_STATUSES = ['PLANNED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED']

export const ACTIVITY_TYPES = ['CALL', 'MEETING', 'EMAIL', 'TASK']

export const PAGE_SIZE = 10

export const AUDIT_PAGE_SIZE = 20

export const ROLES = {
  ADMIN: 'ADMIN',
  MANAGER: 'MANAGER',
  SALES_EXECUTIVE: 'SALES_EXECUTIVE',
}
