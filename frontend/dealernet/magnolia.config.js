import Index from '~/components/templates/index.vue'
import MainHeader from '~/components/MainHeader'
import Footer from '~/components/Footer'
import HeroSpot from '~/components/HeroSpot'
import Announcements from '~/components/Announcements'
import ListDocuments from '~/components/ListDocuments'

export default {
  componentMappings: {
    // Pages
    'dealernet:pages/index': Index,
    // Components
    'dealernet:components/header': MainHeader,
    'dealernet:components/herospot': HeroSpot,
    'dealernet:components/announcements': Announcements,
    'dealernet:components/list-documents': ListDocuments,
    'dealernet:components/footer': Footer
  }
}
