import {WHITE_VARIANT} from '../variants/dinvCarouselVariants'

export default {
  configuration: {
    type: [Object, String] // String because if JCR reference will break component won't
  },
  thankYouPage: {
    type: Object
  },
  title: {
    type: String
  },
  htag: {
    type: String,
    default: 'h2'
  },
  subtitle: {
    type: String
  },
  submitLabel: {
    type: String
  },
  grayBackground: {
    type: String
  },
  ctaLabel: {
    type: String
  },
  description: {
    type: String
  },
  noResultsDescription: {
    type: String
  },
  cancelLabel: {
    type: String
  },
  cta: {
    type: String
  },
  elements: {
    type: Object
  },
  stickyProgressBar: {
    type: String
  },
  progressLabel: {
    type: String
  },
  email: {
    type: Object
  },
  label: {
    type: String
  },
  name: {
    type: String,
    required: true
  },
  options: {
    type: Array
  },
  onChange: {
    type: Function
  },
  required: {
    type: Boolean,
    default: false
  },
  disabledDropdown: {
    type: Boolean,
    default: false
  },
  relatedDropdowns: {
    type: Boolean,
    default: false
  },
  validationMessage: {
    type: String
  },
  icon: {
    type: String
  },
  id: {
    type: String
  },
  placeholder: {
    type: String
  },
  minlength: {
    type: String,
    default: '0'
  },
  maxlength: {
    type: String
  },
  disableMinWidth: {
    type: Boolean,
    default: false
  },
  validation: {
    type: String
  },
  clearLabel: {
    type: String,
    default: 'Clear all'
  },
  selectedLabel: {
    type: String,
    default: 'Selected options'
  },
  rows: {
    type: String,
    default: '3'
  },
  bold: {
    type: Boolean,
    default: false
  },
  handleFormChange: {
    type: Function
  },
  successStateTitle: {
    type: String
  },
  successStateSubtitle: {
    type: String
  },
  resetForm: {
    type: Function
  },
  submitAnother: {
    type: String
  },
  labels: {
    type: Object
  },
  field: {
    type: Object,
    required: true
  },
  fields: {
    type: Array,
    default: []
  },
  fieldsAmount: {
    type: Number
  },
  filledFieldsAmount: {
    type: Number
  },
  isFormValid: {
    type: Boolean
  },
  isFetching: {
    type: Boolean
  },
  formSent: {
    type: Boolean
  },
  handleSubmit: {
    type: Function
  },
  initRelatedFields: {
    type: Function
  },
  removeRelatedFields: {
    type: Function
  },
  initDynamicForms: {
    type: Function
  },
  pickedCountry: {
    type: String
  },
  errorMessage: {
    type: String
  },
  noResultsMsg: {
    type: String,
    default: 'No results found'
  },
  countriesLabel: {
    type: String
  },
  exploreInventoryLabel: {
    type: String
  },
  changeLocationLabel: {
    type: String
  },
  statesLabel: {
    type: String
  },
  statesPlaceholder: {
    type: String
  },
  countriesPlaceholder: {
    type: String
  },
  type: {
    type: String
  },
  stateName: {
    type: String
  },
  apiKey: {
    type: String
  },
  autoDetect: {
    type: String
  },
  stateBold: {
    type: Boolean,
    default: false
  },
  reset: {
    type: Boolean,
    default: false
  },
  countryBold: {
    type: Boolean,
    default: false
  },
  getWholeValue: {
    type: Boolean,
    default: false
  },
  defaultId: {
    type: String,
    default: '@name'
  },
  facet: {
    type: [Object, String],
    default: null
  },
  limit: {
    type: String,
    default: '200'
  },
  requiredSign: {
    type: String,
    default: '*'
  },
  showCTA: {
    type: String,
    default: 'false'
  },
  hideFilters: {
    type: String,
    default: 'false'
  },
  value: {
    type: String,
    default: ''
  },
  formType: {
    type: String,
    default: ''
  },
  formName: {
    type: String,
    default: ''
  },
  formSelectionsForGA4: {
    type: String,
    default: ''
  },
  modelValidationMessage: {
    type: String,
    default: ''
  },
  productTypeValidationMessage: {
    type: String,
    default: 'Product type is required'
  },
  initValues: {
    type: Array
  },
  filterValues: {
    type: Function
  },
  searchEnabled: {
    type: String
  },
  searchPlaceholder: {
    type: String
  },
  noResultsLabel: {
    type: String
  },
  noFacetSearchData: {
    type: Boolean
  },
  isMultiSelect: {
    type: String
  },
  formTitle: {
    type: String
  },
  locationLabel: {
    type: String
  },
  locationPlaceholder: {
    type: String
  },
  autoDetectLabel: {
    type: String
  },
  modelFacetLabel: {
    type: String
  },
  dealerFacetLabel: {
    type: String
  },
  reservationFacetLabel: {
    type: String
  },
  reservationFacetValueLabel: {
    type: String
  },
  distanceLabel: {
    type: String
  },
  searchLabel: {
    type: String
  },
  min: {
    type: String
  },
  max: {
    type: String
  },
  interval: {
    type: String
  },
  productCategory: {
    type: String
  },
  model: {
    type: Object
  },
  variant: {
    type: String,
    default: WHITE_VARIANT
  },
  text: {
    type: String
  }
}
