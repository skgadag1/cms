export default {
  noResultsMessage: {
    type: String,
    default: 'No results'
  },
  label: {
    type: String
  },
  name: {
    type: String,
    required: true
  },
  onChange: {
    type: Function,
    required: true
  },
  validationMessage: {
    type: String,
    default: 'This field is required'
  },
  required: {
    type: Boolean
  },
  useMapping: {
    type: Boolean,
    default: false
  },
  categoriesLabel: {
    type: String,
    default: 'Product Categories'
  },
  subcategoriesLabel: {
    type: String,
    default: 'Product Subcategories'
  },
  productGroupsLabel: {
    type: String,
    default: 'Product Groups'
  },
  productsLabel: {
    type: String,
    default: 'Products'
  },
  placeholder: {
    type: String,
    default: 'What product are you interested in?'
  },
  clearAllLabel: {
    type: String,
    default: 'Clear all'
  }
}
