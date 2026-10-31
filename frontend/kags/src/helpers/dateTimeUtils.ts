import {getNavigatorLanguage} from '@/mixins/commonMixin'

export function daysSince(dateString: string | number): number {
  const diffMs = Date.now() - new Date(dateString).getTime()
  return Math.floor(diffMs / (1000 * 60 * 60 * 24))
}

// Date portion with a trailing comma, e.g. "September 8, 2026,"
export function formatLastSeenDate(lastReported: string | number | null | undefined): string | false {
  if (!lastReported) return false
  const ts = new Date(lastReported)
  return ts.toLocaleDateString(getNavigatorLanguage(), {
    month: 'long',
    day: 'numeric',
    year: 'numeric'
  })
}

// Time portion, e.g. "12:09 PM"
export function formatLastSeenTime(lastReported: string | number | null | undefined): string | false {
  if (!lastReported) return false
  const ts = new Date(lastReported)
  return ts.toLocaleTimeString(getNavigatorLanguage(), {
    hour: 'numeric',
    minute: '2-digit',
    hour12: true
  })
}
