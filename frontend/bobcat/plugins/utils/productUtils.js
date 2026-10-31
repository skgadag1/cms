import {BUSINESS_REGIONS, SPECS_VALUES_DICTIONARY} from '~/constants'

export const fixKey = str => str?.replace(/[\s,%()&".]+/g, '').toLowerCase()

export const reValue = (v, labels = {}) => {
  if (Object.keys(SPECS_VALUES_DICTIONARY).includes(v)) { v = SPECS_VALUES_DICTIONARY[v] }
  return labels[v] || labels[v?.toLowerCase()] || v
}

const generateTableValue = (value, labels = {}) => {
  const headers = value.header.map((h) => { return {key: fixKey(h), label: h} })
  return {
    fields: headers,
    items: value.rows.map((x) => {
      const row = {}
      x.forEach((itm, j) => { row[headers[j].key] = reValue(itm.value, labels) })
      return row
    })
  }
}

export default {
  getProductRegion(model = {}) {
    const modelPath = model ? model['@path']?.replace(process?.env?.VUE_APP_SITE_BASENAME, '') : model
    const [regionName] = modelPath?.split('/')?.filter(x => x) || [process.env.NUXT_DEFAULT_REGION]
    return regionName
  },
  getBusinessUnit(model = {}) {
    return Object.keys(BUSINESS_REGIONS).filter(x => BUSINESS_REGIONS[x].includes(this.getProductRegion(model)))[0]
  },
  structurizeSpecsForCompareTable(specs, filterSections = [], filterPropertiesType = [], labels = {}, filterPropertiesId = [], lang = process.env.NUXT_DEFAULT_LANGUAGE, shouldSort = false) {
    if (!Array.isArray(specs)) {
      specs = Object.values(specs)
    }
    const _structurizedSections = {}
    specs.forEach((m) => {
      const _sections = filterSections?.length && m.data?.sections ? m.data.sections.filter(s => !filterSections.includes(s?.title?.toLowerCase())) : m.data?.sections || []
      _sections.forEach((s) => {
        try {
          const filteredValues = s.properties
            .filter(p => !filterPropertiesType.includes(p.type))
            .filter(p => !filterPropertiesId.includes(p.id))
          const _formattedValues = this.getFormattedValues(filteredValues, labels)
          const _sectionKey = 'section-key-' + fixKey(s.title)
          if (!_structurizedSections[_sectionKey]) { _structurizedSections[_sectionKey] = {title: s.title, properties: {}} }
          const _tempStructurizedSections = _structurizedSections[_sectionKey]

          _formattedValues.forEach((p) => {
            const _propKey = fixKey(p.label || p.id)
            if (!_tempStructurizedSections.properties[_propKey]) { _tempStructurizedSections.properties[_propKey] = {label: p.label} }
            _tempStructurizedSections.properties[_propKey][m.model.id] = p
          })
        // eslint-disable-next-line no-console
        } catch { console.debug(`[Specifications] Invalid section: ${s.title}`) }
      })
    })

    return {
      models: shouldSort
        ? specs
          .map(m => m.model)
          ?.sort((a, b) =>
            a?.title
              ?.toLowerCase()
              ?.localeCompare(b?.title?.toLowerCase(), lang)
          )
        : specs.map(m => m.model),
      sections: _structurizedSections
    }
  },
  groupSpecsForCompareTable(specs, label = null) {
    if (!label) { return specs }

    const _fixedLabel = fixKey(label)
    const groupedSection = {}
    groupedSection[_fixedLabel] = {title: label, properties: {}}

    if (specs?.sections) {
      Object.keys(specs.sections).forEach((s) => {
        const newProps = specs.sections[s]?.properties
        groupedSection[_fixedLabel].properties = {...groupedSection[_fixedLabel]?.properties || {}, ...newProps}
      })
    }

    return {
      ...specs,
      sections: groupedSection
    }
  },
  getFormattedValues(_props, labels) {
    const _tmp = []
    _props.forEach((p) => {
      switch (p.type) {
      case 'unparsed':
        break
      case 'nested_list':
        _tmp.push(...this.getFormattedValues(p.value))
        break
      case 'number':
        p.value = reValue(p.value || 'N/A', labels)
        _tmp.push(p)
        break
      case 'option':
        p.value = reValue(p.value && p.value !== 'undefined' ? p.value : 'N/A', labels)
        _tmp.push(p)
        break
      case 'list':
        if (p.value.some(x => x.item)) {
          p.value = p.value.forEach((pv) => { _tmp.push({label: pv.item, value: reValue(pv.value || 'yes', labels)}) })
        } else {
          p.value = p.value.map((pv) => { return {label: p.label, value: reValue(pv.value || 'yes', labels), unit: pv.unit || p.unit} })
          _tmp.push(p)
        }
        break
      case 'references':
        _tmp.push(...p.value.map((pv) => { return {label: pv.label, value: reValue(pv.value, labels)} }))
        break
      case 'text':
        if (p.value?.length) { p.value = reValue(p.value, labels); _tmp.push(p) }
        break
      case 'table':
        p.value = generateTableValue(p.value, labels)
        _tmp.push(p)
        break
      default:
        p.value = reValue(p.value, labels)
        _tmp.push(p)
      }
    })
    return _tmp
  },
  getFirstModelImage(model) {
    if (!model) { return null }
    // Prefer the dedicated single image (e.g. Attachment Subcategory image) over gallery assets
    if (model.image?.src) {
      return {
        img: model.image
      }
    }
    // otherwise fall back to the first image within the assets gallery
    if (model.images) {
      return model.images['@nodes'].map(name => model.images?.[name]?.asset).filter(x => x && x?.field === 'img')[0]
    }

    return null
  }
}
