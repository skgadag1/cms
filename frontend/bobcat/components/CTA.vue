<template>
  <LazyHydrate :on-interaction="['click', 'touchstart']">
    <component :is="isArticleWidth ? 'section' : 'div'" :class="isArticleWidth ? 'container' : undefined">
      <div :class="isArticleWidth ? 'row justify-content-center' : undefined">
        <div :class="isArticleWidth ? 'col-12 col-lg-7' : undefined">
          <component
            :is="isArticleWidth ? 'div' : 'section'"
            class="py-5 w-100 d-flex flex-column justify-content-center align-items-center cta-section"
            :class="[variantClasses.background, variantClasses.text, { container: isContainerWidth }]"
            data-test-id="cta"
          >
            <div class="container">
              <div class="w-100 row" :class="alignmentRowClass">
                <div class="col-12">
                  <div
                    class="w-100 d-flex flex-column"
                    :class="alignmentContentClass"
                  >
                    <component
                      :is="titleWrapperTag"
                      v-if="hasTitle"
                      :class="[titleWrapperTag !== 'div' ? titleWrapperTag : '', 'cta-title w-100']"
                      v-html="title"
                    />
                    <div
                      v-if="hasParagraph"
                      class="cta-paragraph w-100 constrained"
                      v-html="paragraph"
                    />
                    <div v-if="showButton" class="d-flex mt-3" :class="alignmentButtonClass">
                      <DynamicButton
                        :variant="buttonVariant"
                        :color="buttonColor"
                        :href="buttonUrl"
                        :target="buttonTarget"
                        :rel="buttonRel"
                        class="text-uppercase"
                        data-test-id="cta-button"
                        @click="ga4click"
                      >
                        {{ buttonLabel }}
                      </DynamicButton>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </component>
        </div>
      </div>
    </component>
  </LazyHydrate>
</template>

<script>
import {computed, useContext} from '@nuxtjs/composition-api'
import useMounted from '~/helpers/composables/useMounted'
import DynamicButton from '~/components/DynamicButton.vue'
import {
  VARIANTS_CLASSES,
  ALIGNMENT_CLASSES,
  CENTER_ALIGNMENT
} from '~/constants/variants/ctaVariants'

const BUTTON_STYLE_VARIANTS = ['hexagonal', 'classic', 'linear']
const VALID_HTAGS = ['h1', 'h2', 'h3', 'h4', 'h5', 'h6']

export default {
  components: {DynamicButton},
  props: {
    title: {type: String, default: ''},
    paragraph: {type: String, default: ''},
    link: {type: Object, default: null},
    color: {type: String, default: 'red'},
    htag: {type: String, default: ''},
    nodeId: {type: String, default: null},
    textAlignment: {type: String, default: CENTER_ALIGNMENT},
    width: {type: String, default: 'full'}
  },
  setup(props) {
    const {$regions, $utils, $ga4} = useContext()

    useMounted(props.nodeId)

    const isContainerWidth = computed(() => props.width === 'container')
    const isArticleWidth = computed(() => props.width === 'article')

    const isButtonStyleChooser = computed(() =>
      props.link?.field && BUTTON_STYLE_VARIANTS.includes(props.link.field)
    )

    const linkField = computed(() => {
      if (props.link?.field === 'none') {
        return null
      }
      if (isButtonStyleChooser.value && props.link?.link) {
        return $utils.getLinkField(props.link.link, $regions.current.language.name)
      }
      return $utils.getLinkField(props.link, $regions.current.language.name)
    })

    const showButton = computed(() =>
      props.link?.field === 'none' ? false : !!linkField.value?.label
    )

    const buttonVariant = computed(() =>
      isButtonStyleChooser.value ? props.link.field : 'classic'
    )

    const buttonColor = computed(() =>
      isButtonStyleChooser.value && props.link.color ? props.link.color : 'white'
    )

    const variantClasses = computed(() =>
      VARIANTS_CLASSES[props.color] || VARIANTS_CLASSES.red
    )

    const alignmentRowClass = computed(() =>
      ALIGNMENT_CLASSES[props.textAlignment] || ''
    )

    const alignmentContentClass = computed(() => {
      if (props.textAlignment === 'center') {
        return 'align-items-center text-center'
      }
      return 'align-items-start text-left'
    })

    const alignmentButtonClass = computed(() => {
      if (props.textAlignment === 'center') {
        return 'justify-content-center'
      }
      return 'justify-content-start'
    })

    const titleWrapperTag = computed(() =>
      props.htag && VALID_HTAGS.includes(props.htag) ? props.htag : 'div'
    )

    const hasTitle = computed(() => props.title != null && String(props.title).trim() !== '')

    const hasParagraph = computed(() => props.paragraph != null && String(props.paragraph).trim() !== '')

    const buttonUrl = computed(() => linkField.value && linkField.value.url)

    const buttonTarget = computed(() => {
      const t = linkField.value && linkField.value.target
      return t === '_blank_follow' ? '_blank' : (t || '_self')
    })

    const buttonRel = computed(() => {
      const t = linkField.value && linkField.value.target
      return t === '_blank' ? 'noopener nofollow' : null
    })

    const buttonLabel = computed(() => linkField.value && linkField.value.label)

    const ga4click = () => {
      $ga4.events.cta_banner_click(props?.title, props?.paragraph, linkField.value?.label)
    }

    return {
      isContainerWidth,
      isArticleWidth,
      linkField,
      showButton,
      buttonVariant,
      buttonColor,
      buttonUrl,
      buttonTarget,
      buttonRel,
      buttonLabel,
      titleWrapperTag,
      variantClasses,
      alignmentRowClass,
      alignmentContentClass,
      alignmentButtonClass,
      hasTitle,
      hasParagraph,
      ga4click
    }
  }
}
</script>

<style lang="scss" scoped>
.cta-section {
  &.evolution-gradient {
    background-image: url('~/assets/evolution-gradient.png');
    background-size: 500px;
    background-repeat: no-repeat;
    background-position: top right;
  }

  .constrained {
    max-width: 36rem;
  }

  ::v-deep {
    .cta-title, .cta-paragraph {
        h1, h2, h3, h4, h5, h6, p {
        margin-bottom: 0;
      }
    }
  }
}
</style>
