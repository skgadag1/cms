<template>
  <LazyHydrate when-visible>
    <section
      v-cloak
      data-test-id="evolution-table"
      class="evolution-table py-4"
      :class="backgroundClass"
    >
      <div class="container">
        <div v-if="title || titleAccent" class="text-uppercase mb-1">
          <component
            :is="htag"
            :class="htag"
            data-test-id="evolution-table-title"
          >
            <span v-if="title">{{ title }}</span>
            <span
              v-if="titleAccent"
              class="font-weight-bold text-primary ml-2"
            >{{ titleAccent }}</span>
          </component>
        </div>

        <component
          :is="layout.subtitleLink && layout.subtitleLink.type !== 'none' ? 'a' : 'div'"
          v-if="layout.subtitle"
          :href="resolveHref(layout.subtitleLink)"
          :target="layout.subtitleLink && layout.subtitleLink.target"
          :rel="layout.subtitleLink && layout.subtitleLink.target === '_blank' ? 'noopener nofollow' : null"
          class="evolution-table__subtitle text-primary text-uppercase font-weight-bold text-decoration-none small d-inline-block mb-3"
          data-test-id="evolution-table-subtitle"
          @click="onLinkClick($event, layout.subtitle, layout.subtitleLink)"
        >
          {{ layout.subtitle }}
          <b-icon
            v-if="layout.subtitleLink && layout.subtitleLink.type !== 'none'"
            icon="chevron-right"
            class="ml-1"
            aria-hidden="true"
          />
        </component>

        <div v-if="layout.headers.length" class="evolution-table__viewport">
          <div
            class="evolution-table__wrap"
            :class="['evolution-table__wrap--' + layout.tableColorScheme]"
            role="region"
            :aria-labelledby="captionId"
            tabindex="0"
          >
            <table class="evolution-table__table">
              <caption :id="captionId" class="evolution-table__caption">{{ tableCaption }}</caption>
              <thead>
                <tr class="evolution-table__header-row">
                  <th scope="col" class="evolution-table__header-cell evolution-table__header-cell--label" />
                  <th
                    v-for="header in layout.headers"
                    :key="header.id"
                    scope="col"
                    class="evolution-table__header-cell"
                  >
                    <div class="evolution-table__header-content d-flex flex-column">
                      <span
                        v-if="header.icon && header.icon !== 'none'"
                        class="evolution-table__header-icon"
                      >
                        <b-icon :icon="header.icon" aria-hidden="true" />
                      </span>
                      <ImageSet
                        v-if="header.image"
                        :src="header.image"
                        :alt="header.imageAlt || header.label"
                        :title="header.imageAlt || header.label"
                        crop="limit"
                        :sizes="[]"
                        :aspect-ratio="null"
                        class="evolution-table__header-image align-self-start"
                      />
                      <component
                        :is="header.link.type !== 'none' ? 'a' : 'span'"
                        v-if="header.label"
                        :href="resolveHref(header.link)"
                        :target="header.link.target"
                        :rel="header.link.target === '_blank' ? 'noopener nofollow' : null"
                        class="evolution-table__header-label text-uppercase font-weight-bold"
                        :class="{ 'evolution-table__link': header.link.type !== 'none' }"
                        @click="onLinkClick($event, header.label, header.link)"
                      >
                        {{ header.label }}
                      </component>
                      <p
                        v-if="header.description"
                        class="evolution-table__header-description small mb-0"
                      >
                        {{ header.description }}
                      </p>
                    </div>
                  </th>
                </tr>
              </thead>

              <tbody
                v-for="(section, sIdx) in layout.sections"
                :id="sectionDomId(section, sIdx)"
                :key="section.id || sIdx"
              >
                <tr
                  v-if="section.title && (section.rows || []).length"
                  class="evolution-table__section-row text-uppercase font-weight-bold"
                  :class="{ 'evolution-table__collapsed': isSectionCollapsed(sIdx) }"
                >
                  <th
                    scope="rowgroup"
                    class="evolution-table__section-cell"
                    :colspan="layout.headers.length + 1"
                  >
                    {{ section.title }}
                  </th>
                </tr>

                <tr
                  v-for="(row, rIdx) in (section.rows || [])"
                  :key="row.id"
                  class="evolution-table__row"
                  :class="{ 'evolution-table__collapsed': isRowCollapsed(sIdx, rIdx) }"
                >
                  <th
                    scope="row"
                    class="evolution-table__row-label text-uppercase font-weight-bold"
                    :class="row.labelColor !== 'default' ? 'evolution-table__color-' + row.labelColor : ''"
                  >
                    <component
                      :is="row.link.type !== 'none' ? 'a' : 'span'"
                      :href="resolveHref(row.link)"
                      :target="row.link.target"
                      :rel="row.link.target === '_blank' ? 'noopener nofollow' : null"
                      :class="{ 'evolution-table__link': row.link.type !== 'none' }"
                      @click="onLinkClick($event, row.label, row.link)"
                    >
                      {{ row.label }}<b-icon
                        v-if="row.link.type !== 'none'"
                        icon="chevron-right"
                        class="ml-1"
                        aria-hidden="true"
                      />
                    </component>
                  </th>
                  <td
                    v-for="cell in row.cells"
                    :key="cell.id"
                    class="evolution-table__cell"
                    :class="cellClasses(cell)"
                  >
                    <template v-if="cell.type === 'check'">
                      <b-icon
                        icon="success-check"
                        class="evolution-table__check"
                        aria-hidden="true"
                      />
                      <span v-if="labels.included" class="sr-only">{{ labels.included }}</span>
                    </template>
                    <template v-else-if="cell.type === 'dash'">
                      <span class="evolution-table__dash text-muted" aria-hidden="true" />
                      <span v-if="labels.notApplicable" class="sr-only">{{ labels.notApplicable }}</span>
                    </template>
                    <component
                      :is="cell.link.type !== 'none' ? 'a' : 'span'"
                      v-else-if="cell.type === 'text'"
                      :href="resolveHref(cell.link)"
                      :target="cell.link.target"
                      :rel="cell.link.target === '_blank' ? 'noopener nofollow' : null"
                      :class="{ 'evolution-table__link': cell.link.type !== 'none', 'font-weight-bold': cell.bold }"
                      @click="onLinkClick($event, cell.value, cell.link)"
                    >
                      <template v-if="abbreviationLabel(cell.value)">
                        <span aria-hidden="true">{{ cell.value }}</span>
                        <span class="sr-only">{{ abbreviationLabel(cell.value) }}</span>
                      </template>
                      <template v-else>{{ cell.value }}</template>
                    </component>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div
            v-if="hasHiddenRows"
            class="evolution-table__show-more"
            :class="showMoreExpanded ? 'evolution-table__show-more--expanded' : 'evolution-table__show-more--collapsed'"
          >
            <DynamicButton
              :variant="BUTTON_NAMES.HEXAGONAL"
              color="white-red"
              class="text-uppercase"
              data-test-id="evolution-table-show-more"
              :aria-expanded="showMoreExpanded ? 'true' : 'false'"
              :aria-controls="controlledSectionIds"
              @click.prevent="onShowMoreClick"
            >
              {{ showMoreLabel }}
            </DynamicButton>
          </div>
        </div>

        <ul v-if="visibleDisclaimers.length" class="list-unstyled mt-3">
          <li
            v-for="(disclaimer, idx) in visibleDisclaimers"
            :key="`disc-${idx}`"
            class="small"
          >
            <sup class="mr-1">{{ idx + 1 }}</sup>{{ disclaimer }}
          </li>
        </ul>
      </div>
    </section>
  </LazyHydrate>
</template>

<script>
import {computed, inject, onServerPrefetch, ref, useContext, useFetch} from '@nuxtjs/composition-api'
import {BUTTON_NAMES} from '~/constants/buttons'
import useMounted from '~/helpers/composables/useMounted'

const BACKGROUND_CLASSES = {
  'light-gray': 'bg-gray-lighter',
  white: 'bg-white',
  dark: 'bg-gray-darker text-white',
  black: 'bg-black text-white',
  hyperspace: 'bg-black evolution-gradient text-white'
}

const DEFAULT_ROW_LIMIT = 5
const ABBREVIATION_KEYS = {
  OPT: 'optional',
  STD: 'standard'
}

function safeParse(raw) {
  if (!raw) { return null }
  if (typeof raw === 'object') { return raw }
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

export default {
  props: {
    nodeId: {
      type: String
    },
    title: {
      type: String,
      default: ''
    },
    titleAccent: {
      type: String,
      default: ''
    },
    htag: {
      type: String,
      default: 'h2'
    },
    tableConfig: {
      type: [String, Object],
      default: ''
    }
  },
  setup(props) {
    const {$utils, $regions, $ga4, $services} = useContext()
    const model = inject('model')
    const labels = ref({})
    useMounted(props.nodeId)

    const {fetch: fetchLabels} = useFetch(async () => {
      if (Object.keys(labels.value).length || !$services?.bobcatapi?.getConcepts) { return }
      labels.value = await $services.bobcatapi.getConcepts(['/common/labels/functional']) || {}
    })

    onServerPrefetch(async () => {
      await fetchLabels()
    })

    const heading = computed(() => [props.title, props.titleAccent].filter(Boolean).join(' '))

    const showMoreExpanded = ref(false)

    const layout = computed(() => {
      const fallback = {
        background: 'white',
        tableColorScheme: 'black',
        subtitle: '',
        subtitleLink: {type: 'none', target: '_self'},
        headers: [],
        sections: [],
        showMore: false,
        showMoreRowLimit: 5,
        disclaimers: []
      }
      const parsed = safeParse(props.tableConfig)
      if (!parsed) { return fallback }
      return {
        ...fallback,
        ...parsed,
        subtitleLink: parsed.subtitleLink || fallback.subtitleLink,
        headers: Array.isArray(parsed.headers) ? parsed.headers : [],
        sections: Array.isArray(parsed.sections) ? parsed.sections : [],
        disclaimers: Array.isArray(parsed.disclaimers) ? parsed.disclaimers : []
      }
    })

    const backgroundClass = computed(() => BACKGROUND_CLASSES[layout.value.background] || BACKGROUND_CLASSES.white)

    const totalRows = computed(() => layout.value.sections.reduce((sum, s) => sum + (s.rows?.length || 0), 0))

    const rowLimit = computed(() => {
      const limit = Math.floor(Number(layout.value.showMoreRowLimit))
      return Number.isFinite(limit) && limit > 0 ? limit : DEFAULT_ROW_LIMIT
    })

    const hasHiddenRows = computed(() => layout.value.showMore && totalRows.value > rowLimit.value)

    function sectionDomId(section, sIdx) {
      const raw = String(section?.id || sIdx)
      const slug = raw.replace(/[^A-Za-z0-9_-]/g, '')
      return `evolution-table-${props.nodeId || 'table'}-${slug || sIdx}`
    }

    const captionId = computed(() => `evolution-table-caption-${props.nodeId || 'table'}`)

    const tableCaption = computed(() => {
      return [
        [props.title, props.titleAccent].filter(Boolean).join(' '),
        layout.value.sections.map(section => section.title).filter(Boolean).join(', '),
        layout.value.headers.map(header => header.label).filter(Boolean).join(', ')
      ].filter(Boolean).join('. ')
    })

    const showMoreLabel = computed(() => (
      showMoreExpanded.value ? labels.value.showLess : labels.value.showMore
    ))

    // Rows past the limit stay mounted. Show More only toggles this set.
    const collapsedRowKeys = computed(() => {
      const keys = new Set()
      if (!hasHiddenRows.value || showMoreExpanded.value) { return keys }
      let index = 0
      const limit = rowLimit.value
      layout.value.sections.forEach((section, sIdx) => {
        (section.rows || []).forEach((row, rIdx) => {
          if (index >= limit) { keys.add(`${sIdx}:${row?.id || rIdx}`) }
          index += 1
        })
      })
      return keys
    })

    const controlledSectionIds = computed(() => {
      if (!hasHiddenRows.value) { return undefined }
      let index = 0
      const limit = rowLimit.value
      const ids = []
      layout.value.sections.forEach((section, sIdx) => {
        let controlsSection = false
        ;(section.rows || []).forEach(() => {
          if (index >= limit) { controlsSection = true }
          index += 1
        })
        if (controlsSection) { ids.push(sectionDomId(section, sIdx)) }
      })
      return ids.join(' ') || undefined
    })

    const visibleDisclaimers = computed(() => (layout.value.disclaimers || []).filter(d => typeof d === 'string' && d.trim()))

    const resolveHref = (link) => {
      if (!link || link.type === 'none') { return undefined }
      if (link.type === 'external') {
        const url = (link.externalUrl || '').trim()
        if (!url) { return undefined }
        return /^([a-z][a-z0-9+.-]*:|[/#])/i.test(url) ? url : `https://${url}`
      }
      if (link.type === 'page') {
        const pageLink = (link.pageLink || '').trim()
        if (!pageLink) { return undefined }
        const region = $regions?.current?.name
        const language = $regions?.current?.language?.name
        const prefix = region && language ? `/${region}/${language}` : ''
        const path = pageLink.startsWith('/') ? pageLink : `/${pageLink}`
        if (!prefix || path === prefix || path.startsWith(`${prefix}/`)) { return path }
        return `${prefix}${path}`
      }
      return undefined
    }

    function isRowCollapsed(sIdx, rIdx) {
      const row = layout.value.sections[sIdx]?.rows?.[rIdx]
      return collapsedRowKeys.value.has(`${sIdx}:${row?.id || rIdx}`)
    }

    function isSectionCollapsed(sIdx) {
      const rows = layout.value.sections[sIdx]?.rows || []
      return rows.length > 0 && rows.every((row, rIdx) => collapsedRowKeys.value.has(`${sIdx}:${row?.id || rIdx}`))
    }

    function abbreviationLabel(value) {
      const key = ABBREVIATION_KEYS[String(value || '').trim().toUpperCase()]
      return key ? labels.value[key] || '' : ''
    }

    const cellClasses = (cell) => {
      const classes = []
      if (cell?.type && cell.type !== 'empty') { classes.push(`evolution-table__cell--${cell.type}`) }
      if (cell?.color && cell.color !== 'default') { classes.push(`evolution-table__color-${cell.color}`) }
      if (cell?.wrap === false) { classes.push('text-nowrap') }
      return classes
    }

    const onLinkClick = (event, label, link) => {
      if (!link || link.type === 'none') { return }
      $ga4?.events?.table_click(event, model, heading.value, 'link click', label)
    }

    const onShowMoreClick = (event) => {
      $ga4?.events?.table_click(event, model, heading.value, 'link click', showMoreLabel.value)
      showMoreExpanded.value = !showMoreExpanded.value
    }

    return {
      layout,
      backgroundClass,
      visibleDisclaimers,
      hasHiddenRows,
      showMoreExpanded,
      labels,
      captionId,
      tableCaption,
      showMoreLabel,
      controlledSectionIds,
      resolveHref,
      cellClasses,
      sectionDomId,
      isRowCollapsed,
      isSectionCollapsed,
      abbreviationLabel,
      onLinkClick,
      onShowMoreClick,
      BUTTON_NAMES,
      $utils,
      $regions
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~bootstrap/scss/functions';
@import '~/assets/sass/variables';
@import '~bootstrap/scss/mixins';

$fade-height: 4.5rem;

.evolution-gradient {
  background-image: url('~/assets/evolution-gradient.png');
  background-size: 100%;

  @include media-breakpoint-down(md) {
    background-size: cover;
    background-position: center;
  }
}

.evolution-table {
  &__subtitle {
    font-size: 1.125rem;

    &:hover {
      text-decoration: underline;
      color: $primary;
    }
  }

  &__viewport {
    position: relative;
  }

  &__wrap {
    overflow-x: auto;
    background: $white;
    border: 1px solid $gray-300;
    border-radius: $border-radius;

    &--black .evolution-table__header-row {
      background: $gray-900;
      color: $white;
    }

    &--white .evolution-table__header-row {
      background: $white;
      color: $gray-900;
      border-bottom: 2px solid $gray-300;
    }
  }

  &__table {
    width: 100%;
    border-collapse: collapse;
    color: $gray-900;
  }

  &__caption {
    @include sr-only;
  }

  &__collapsed {
    display: none;
  }

  &__header-cell {
    padding: 1rem;
    vertical-align: top;
    text-align: left;
    border-right: 1px solid rgba($white, 0.08);

    &--label {
      width: 14rem;
      background: $gray-100;
    }
  }

  &__wrap--white &__header-cell {
    border-right-color: $gray-200;
  }

  &__header-content {
    gap: 0.5rem;
  }

  &__header-icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    align-self: center;
    width: 3rem;
    height: 3rem;
    margin-bottom: 0.5rem;
    border-radius: 50%;
    background: $white;
    color: $primary;
    font-size: 1.375rem;
    box-shadow: 0 1px 4px rgba($black, 0.15);
  }

  &__header-image {
    max-width: 100%;
    max-height: 4rem;

    :deep(img) {
      max-height: 4rem;
      object-fit: contain;
    }
  }

  &__header-label {
    font-size: $font-size-base;
  }

  &__header-description {
    line-height: 1.35;
    opacity: 0.95;
  }

  &__section-row {
    background: $gray-400;
    color: $white;
    letter-spacing: 0.05em;
  }

  &__show-more {
    display: flex;
    justify-content: center;

    .dynamic-button {
      width: auto;
    }

    &--collapsed {
      position: absolute;
      right: 1px;
      bottom: 1px;
      left: 1px;
      z-index: 1;
      align-items: flex-end;
      height: $fade-height;
      padding-bottom: 0.5rem;
      border-radius: 0 0 $border-radius $border-radius;
      background: linear-gradient(180deg, rgba($white, 0) 0%, $white 86.89%);
      pointer-events: none;

      > * {
        pointer-events: auto;
      }
    }

    &--expanded {
      margin-top: 1.5rem;
    }
  }

  &__section-cell {
    padding: 0.5rem 1rem;
    text-align: left;
  }

  &__row {
    border-bottom: 1px solid $gray-200;

    &:nth-child(odd) {
      background: rgba($gray-100, 0.5);
    }
  }

  &__row-label {
    padding: 0.75rem 1rem;
    background: $gray-100;
    border-right: 1px solid $gray-300;
    color: $gray-900;
    text-align: left;
  }

  &__cell {
    padding: 0.75rem 1rem;
    border-right: 1px solid $gray-200;
    vertical-align: middle;

    &--check,
    &--dash {
      text-align: center;
    }
  }

  &__check {
    color: $green;
    font-size: 1.125rem;
  }

  &__dash::before {
    content: "-";
  }

  &__link {
    color: $primary;
    font-weight: 700;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  &__color-orange,
  &__color-red {
    color: $primary;
  }

  &__color-white {
    color: $white;
  }

  &__color-black {
    color: $black;
  }
}
</style>
