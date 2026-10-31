export const MIN_COMPARE_SLOT_COUNT = 2
export const MAX_COMPARE_SLOT_COUNT = 3

export const compareModelKey = model => String(model?.id || model?.['@id'] || '').trim()

const normalizeId = value => String(value ?? '').trim().toLowerCase()

/** Listing deep links use the catalog id, Magnolia default-model fields use the JCR uuid. */
const matchesRequestedId = (model, requestedId) =>
  [model?.id, model?.['@id'], model?.['@name']].some(candidate =>
    candidate != null && normalizeId(candidate) === requestedId
  )

export function buildCompareSlots(models, urlIds = [], defaultIds = []) {
  const requestedIds = urlIds.length ? urlIds : defaultIds
  const selected = []

  requestedIds.forEach((id) => {
    if (selected.length >= MAX_COMPARE_SLOT_COUNT) {
      return
    }
    const requestedId = normalizeId(id)
    if (!requestedId) {
      return
    }
    const model = (models || []).find(candidate => matchesRequestedId(candidate, requestedId))
    if (model && !selected.some(slot => compareModelKey(slot) === compareModelKey(model))) {
      selected.push(model)
    }
  })

  const slotCount = selected.length >= MAX_COMPARE_SLOT_COUNT
    ? MAX_COMPARE_SLOT_COUNT
    : MIN_COMPARE_SLOT_COUNT
  while (selected.length < slotCount) {
    selected.push(null)
  }
  return selected
}

export function addCompareSlot(slots) {
  return (slots || []).length < MAX_COMPARE_SLOT_COUNT ? [...slots, null] : slots
}

export function removeCompareSlot(slots, index) {
  const selected = (slots || []).filter((model, i) => i !== index && model)
  while (selected.length < MIN_COMPARE_SLOT_COUNT) {
    selected.push(null)
  }
  return selected.slice(0, MAX_COMPARE_SLOT_COUNT)
}
