import type {PagesConfig} from './build/vite-plugin-pages-mpa'

const pages: PagesConfig = {
  index: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Machine/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'index.html',
    title: 'Index - KAGS'
  },
  machinesearch: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/MachineSearch/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/homepage.html',
    filename: 'machinesearch.html',
    title: 'Machine Search'
  },
  header: {
    entry: 'src/components/Header/main.ts',
    template: 'public/index.html',
    filename: 'header.html',
    title: 'Header'
  },
  notificationbar: {
    entry: 'src/components/NotificationBar/main.ts',
    template: 'public/index.html',
    filename: 'notificationbar.html',
    title: 'Sticky Notification Bar'
  },
  footer: {
    entry: 'src/components/Footer/main.ts',
    template: 'public/index.html',
    filename: 'footer.html',
    title: 'Footer'
  },
  bats: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Bats/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/bats.html',
    title: 'BATS'
  },
  schematics: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Schematics/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/schematics',
    title: 'SCHEMATICS'
  },
  servicelibrary: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/ServiceLibrary/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'servicelibrary',
    title: 'SERVICE LIBRARY'
  },
  machinelibrary: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/MachineLibrary/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/manuals',
    title: 'My Machine Library'
  },
  machine: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Machine/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts',
      'src/assets/js/links.js'
    ],
    template: 'public/index.html',
    filename: 'machine/machine.html',
    title: 'My Machine page'
  },
  machinehistory: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/MachineHistory/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/diagnostichistory',
    title: 'Machine history page'
  },
  menu: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Menu/main.ts',
      'src/components/DownloadV2/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/menu.html',
    filename: 'menu.html',
    title: 'Menu'
  },
  troubleshooting: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/TroubleshootingPro/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/troubleshooting',
    title: 'Troubleshooting Pro'
  },
  faultcodes: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/FaultCodes/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/faultcodes',
    title: 'Fault Codes'
  },
  builtoptions: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/BuiltOptions/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/options',
    title: 'Built Options'
  },
  fieldmods: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/FieldModifications/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/fieldmodifications',
    title: 'Field Modifications',
    // Renamed to Product Recall; the CMS sitemap links it under these URLs
    devAliases: ['machine/productrecall', 'machine/productrecal']
  },
  warranty: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Warranty/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/warranty',
    title: 'Warranty'
  },
  locationhistory: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/LocationHistory/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/locationhistory',
    title: 'Location History'
  },
  video: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Video/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/homepage.html',
    filename: 'video.html',
    title: 'Video'
  },
  download: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/DownloadV2/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/homepage.html',
    filename: 'downloadv2',
    title: 'Download'
  },
  softwareregistration: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/SoftwareRegistration/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/softwareregistration.html',
    filename: 'softwareregistration',
    title: 'Software Registration'
  },
  homepage: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Video/main.ts',
      'src/components/MachineSearch/main.ts',
      'src/components/RichText/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/homepage.html',
    filename: 'homepage.html',
    title: 'Video'
  },
  contact: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Contact/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'contact.html',
    title: 'Contact Us',
    devAliases: ['contactus.html']
  },
  feedback: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'feedback.html',
    title: 'Feedback'
  },
  links: 'src/assets/js/links.js',
  breadcrumb: 'src/components/Breadcrumb/main.ts',
  serviceAnalyzer: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/Breadcrumb/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/static/service-analyzer.html',
    filename: 'serviceanalyzer',
    title: 'Service Analyzer'
  },
  maintenance: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/MaintenanceSchedule/main.ts',
      'src/components/Feedback/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/maintenance',
    title: 'Maintenance Schedule'
  },
  richtext: {
    entry: [
      'src/components/Header/main.ts',
      'src/components/RichText/main.ts',
      'src/components/Footer/main.ts'
    ],
    template: 'public/index.html',
    filename: 'machine/richtext.html',
    title: 'RichText'
  }
}

export default pages
