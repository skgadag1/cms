<template>
  <LazyHydrate :on-interaction="['click', 'touchstart']">
    <section data-test-id="dinv-categories-landing-page" class="pb-4">
      <div class="container position-relative">
        <div v-if="title"
             class="d-flex align-items-baseline justify-content-between divider mx-3 mx-lg-0"
        >
          <component :is="htag"
                     data-test-id="categories-landing-page-title"
          >
            {{ title }}
          </component>
        </div>
        <b-row>
          <b-col cols="4" class="d-none d-md-block">
            <ul :style="`top: ${menuHeight}px`" class="list-unstyled bg-light border border-gray rounded-sm p-3 position-sticky">
              <li v-for="(item, index) in productCategories" :key="`category-${item.id}-${index}`" :class="{'mt-4':index != 0}">
                <a
                  class="font-weight-bold text-uppercase text-black cursor-pointer"
                  @click="() => { $ga4.events.dealer_inventory_event(item.name); selectCategory(item.id) }"
                >{{ item.name }}</a>
              </li>
            </ul>
          </b-col>
          <b-col md="8" class="px-3">
            <div v-for="(categoryItem, categoryIndex) in productCategories" :key="`category-${categoryItem.id}-${categoryIndex}`" :class="[`category-${categoryItem.id}`]">
              <h3>{{ categoryItem.name }}</h3>
              <hr class="divider">
              <div v-if="categoryItem.subcategories.length" class="row px-2">
                <div v-for="(item, index) in categoryItem.subcategories"
                     :key="`category-${item['@id']}-${index}`"
                     class="col-6 mb-3 px-2"
                >
                  <b-card
                    class="overflow-hidden h-100 transition-base border-0 card-horizontal p-3"
                    no-body
                    footer-tag="footer"
                    footer-class="z-index-1"
                  >
                    <a
                      class="font-weight-bold cursor-pointer"
                      :href="item.url"
                      :target="item.target"
                      @click="() => $ga4.events.dealer_inventory_event(categoryItem.name, item.name)"
                    >
                      <ImageSet
                        v-if="item.image"
                        class="w-100 h-100"
                        :src="item.image"
                        :alt="item.name"
                        :img-width="item.imageWidth"
                        :img-height="item.imageHeight"
                      />
                    </a>
                    <h4 class="subcategory-name text-center text-md-left mb-0 mb-sm-2">{{ item.name }}</h4>
                    <a
                      class="view-all-link font-weight-bold cursor-pointer d-lg-block d-none"
                      :href="item.url"
                      :target="item.target"
                      @click="() => $ga4.events.dealer_inventory_event(categoryItem.name, item.name)"
                    >{{ viewAllLabel }}</a>
                  </b-card>
                </div>
              </div>
              <div v-else class="row px-2">
                <div class="col-6 mb-3 px-2">
                  <b-card
                    class="overflow-hidden h-100 transition-base border-0 card-horizontal p-3"
                    no-body
                    footer-tag="footer"
                    footer-class="z-index-1"
                  >
                    <a
                      class="font-weight-bold cursor-pointer"
                      :href="categoryItem.url"
                      :target="categoryItem.target"
                      @click="() => $ga4.events.dealer_inventory_event(categoryItem.name, undefined, {event_action: 'subcategory click'})"
                    >
                      <ImageSet
                        v-if="categoryItem.image"
                        class="w-100 h-100"
                        :src="categoryItem.image"
                        :alt="categoryItem.name"
                        :img-width="categoryItem.imageWidth"
                        :img-height="categoryItem.imageHeight"
                      />
                    </a>
                    <h4 class="subcategory-name text-center text-md-left">{{ categoryItem.name }}</h4>
                    <a
                      class="font-weight-bold cursor-pointer d-lg-block d-none"
                      :href="categoryItem.url"
                      :target="categoryItem?.target"
                    >{{ viewAllLabel }}</a>
                  </b-card>
                </div>
              </div>
            </div>
          </b-col>
          <FixedBottomDropdown ref="mobileMenu" class="d-md-none" :button-label="categoriesLabel">
            <ul class="list-unstyled bg-white shadow p-3 mb-0 overflow-auto"
                tabindex="-1"
                role="listbox"
            >
              <li v-for="(item, index) in productCategories" :key="`category-${item.id}-${index}`" :class="{'border-top':index != 0}" class=" text-center p-2">
                <a
                  class="font-weight-bold text-black cursor-pointer"
                  @click="() => {$ga4.events.dealer_inventory_event(item.name); selectCategory(item.id, true) }"
                >{{ item.name }}</a>
              </li>
            </ul>
          </FixedBottomDropdown>
        </b-row>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {onBeforeMount, onBeforeUnmount, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import FixedBottomDropdown from './elements/FixedBottomDropdown.vue'
import {inEditorEdit} from '~/helpers/vue-editor'
import useMounted from '~/helpers/composables/useMounted'

export default {
  components: {FixedBottomDropdown},
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String
    },
    htag: {
      type: String,
      default: 'h2'
    },
    viewAllLabel: {
      type: String,
      default: 'View all inventory >'
    },
    categoriesLabel: {
      type: String,
      default: 'Categories'
    }
  },
  setup(props) {
    const {$services} = useContext()
    const selectedCategory = ref(null)
    const productCategories = ref([])
    const mobileMenu = ref(null)
    const menuHeight = ref(136)
    const isVisible = ref(false)

    useMounted(props.nodeId)

    const selectCategory = (categoryId, mobile = false) => {
      selectedCategory.value = categoryId
      if (mobileMenu.value) { mobileMenu.value.hide() }
      const category = window.document.querySelector(`.category-${categoryId}`)
      const y = category.getBoundingClientRect().top + window.scrollY - document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1
      window.scrollTo({top: y, behavior: mobile ? 'instant' : 'smooth'})
      if (mobile) {
        setTimeout(() => {
          // compensation for animated top menus
          const y = category.getBoundingClientRect().top + window.scrollY - document.querySelector('header[data-test-id="main-header"]')?.clientHeight - 1
          window.scrollTo({top: y, behavior: 'smooth'})
        }, 200)
      }
    }

    const setTopMenuHeight = () => {
      menuHeight.value = document.querySelector('header[data-test-id="main-header"]')?.clientHeight + 16
    }

    if (process.client) {
      onBeforeMount(() => {
        window.addEventListener('resize', setTopMenuHeight, {passive: true})
        window.addEventListener('scroll', setTopMenuHeight, {passive: true})
      })

      onBeforeUnmount(() => {
        window.removeEventListener('resize', setTopMenuHeight)
        window.removeEventListener('scroll', setTopMenuHeight)
      })
    }

    useFetch(async () => {
      try {
        const mainCategories = (await $services.bobcatapi.getCategories(null, 'na'))?.[0]
        const mainCategoriesFiltered = mainCategories['@nodes'].map(key => mainCategories[key])
        const allSubcategories = []
        productCategories.value = mainCategoriesFiltered.map((category) => {
          const subcategories = category['@nodes'].map(key => category[key]).filter(subcat => subcat['@nodeType'] === 'dxp:subcategory' && !!subcat.IRWProductName).map(cat => ({id: cat['@id'], name: cat.title}))
          allSubcategories.push(...subcategories.map(cat => cat.id))
          allSubcategories.push(category['@id'])
          return {
            IRWProductName: category.IRWProductName,
            id: category['@id'],
            name: category.title || '',
            subcategories,
            image: category.image?.src || null,
            imageWidth: category.image?.imageWidth || null,
            imageHeight: category.image?.imageHeight || null
          }
        }).filter(category => !!category.IRWProductName || category.subcategories.length)

        if (allSubcategories.length) {
          const subcategories = (await $services.bobcatapi.getCategories(allSubcategories))
          productCategories.value = productCategories.value.map((mainCategory) => {
            const category = subcategories.find(item => item['@id'] === mainCategory.id)
            return {
              ...mainCategory,
              url: category?.pageLink?.link?.pageUrl || null,
              target: category?.pageLink?.link?.target || '_self',
              subcategories: mainCategory.subcategories.map((subCat) => {
                const subcategory = subcategories.find(item => item['@id'] === subCat.id)
                if (subcategory && subcategory.image) {
                  return {
                    ...subCat,
                    image: subcategory.image.src,
                    imageWidth: subcategory.image.imageWidth || null,
                    imageHeight: subcategory.image.imageHeight || null,
                    url: subcategory?.pageLink?.link?.pageUrl || null,
                    target: subcategory?.pageLink?.link?.target || '_self'
                  }
                } else {
                  return subCat
                }
              })
            }
          })
        }

        if (mainCategoriesFiltered.length) {
          selectedCategory.value = productCategories.value[0].id
        }
      } catch (error) {
        // eslint-disable-next-line
        console.error('[Categories Landing Page] Cannot fetch products', error)
      }
    })

    return {
      menuHeight,
      mobileMenu,
      isVisible,
      productCategories,
      selectCategory,
      selectedCategory,
      inEditorEdit
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

.view-all-link {
  font-size: 1.125rem;
}

.subcategory-name {
  @include media-breakpoint-down(sm) {
    font-size: $h5-font-size;
  }

  @include media-breakpoint-down(xs) {
    font-size: $h6-font-size;
  }
}

</style>
