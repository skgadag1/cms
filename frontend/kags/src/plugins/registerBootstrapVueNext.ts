import {
  createBootstrap,
  BContainer, BRow, BCol,
  BLink, BButton, BImg, BBadge, BSpinner, BAlert, BProgress,
  BListGroup, BListGroupItem,
  BNav, BNavItem, BNavText, BNavbar, BNavbarBrand, BNavbarToggle,
  BCollapse,
  BDropdown, BDropdownItem, BDropdownText, BDropdownForm, BDropdownDivider,
  BModal,
  BFormGroup, BFormInput, BFormTextarea,
  BFormCheckbox, BFormCheckboxGroup, BFormRadio, BFormRadioGroup,
  BFormSelect, BFormSelectOption, BFormInvalidFeedback,
  BInputGroup, BInputGroupText,
  BPagination,
  BTable,
  BTabs, BTab,
  BTooltip,
  vBToggle, vBTooltip, vBModal
} from 'bootstrap-vue-next'
import 'bootstrap-vue-next/dist/bootstrap-vue-next.css'

// Registered globally because templates use these tags without per-SFC imports. Only the
// components used in src/ are registered to keep the shared chunk small.
const bvnComponents = {
  BContainer, BRow, BCol,
  BLink, BButton, BImg, BBadge, BSpinner, BAlert, BProgress,
  BListGroup, BListGroupItem,
  BNav, BNavItem, BNavText, BNavbar, BNavbarBrand, BNavbarToggle,
  BCollapse,
  BDropdown, BDropdownItem, BDropdownText, BDropdownForm, BDropdownDivider,
  BModal,
  BFormGroup, BFormInput, BFormTextarea,
  BFormCheckbox, BFormCheckboxGroup, BFormRadio, BFormRadioGroup,
  BFormSelect, BFormSelectOption, BFormInvalidFeedback,
  BInputGroup, BInputGroupText,
  BPagination,
  BTable,
  BTabs, BTab,
  BTooltip
}

export function registerBootstrapVueNext(app) {
  app.use(createBootstrap({
    components: {
      BModal: {noCloseOnBackdrop: true},
      BTooltip: {teleportTo: 'body'},
      BPopover: {teleportTo: 'body'}
    }
  }))

  Object.entries(bvnComponents).forEach(([name, component]) => app.component(name, component))

  app.directive('b-toggle', vBToggle)
  app.directive('b-tooltip', vBTooltip)
  app.directive('b-modal', vBModal)
}
