import type {ComputedRef} from 'vue'

export type LabelValues = Record<string, string | Record<string, string> | undefined>

export type SerialError = {
  error_code?: string | number
  error_message?: string
}

export type UseLabelsReturn = {
  labels: ComputedRef<LabelValues>
  translate: (conceptKey: string, conceptPath?: string | null) => string
  translatedConfig: (conceptKey: string) => string
  getSerialErrorMessage: (error: SerialError, sn: string) => string
  updateUrlParam: (keyword: string, value: string | number | null | undefined) => void
  translateLocale: (facet: string | null | undefined) => string
}
