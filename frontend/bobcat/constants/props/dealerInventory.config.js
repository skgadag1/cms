export default {
  htag: {
    type: String
  },
  title: {
    type: String
  },
  description: {
    type: String
  },
  zipCodeLabel: {
    type: String,
    required: true
  },
  distanceLabel: {
    type: String,
    required: true
  },
  productTypeLabel: {
    type: String,
    required: true
  },
  productModelLabel: {
    type: String,
    required: true
  },
  zipCodePlaceholder: {
    type: String
  },
  productTypePlaceholder: {
    type: String
  },
  productModelPlaceholder: {
    type: String
  },
  searchLabel: {
    type: String,
    required: true
  },
  resetLabel: {
    type: String,
    required: true
  },
  min: {
    type: String,
    default: '25'
  },
  max: {
    type: String,
    default: '100'
  },
  interval: {
    type: String,
    default: '25'
  },
  apiKey: {
    type: String,
    required: true
  },
  validationMessage: {
    type: String
  },
  noResultsMsg: {
    type: String
  },
  autoDetect: {
    type: String
  }
}
