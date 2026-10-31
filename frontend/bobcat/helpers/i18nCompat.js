export const hasListItems = source => Boolean(source?.['@nodes']?.length)

export const resolveLegacyListSource = (newSource, attrs, fieldName, lang, useLegacyStructure) => {
  if (hasListItems(newSource)) {
    return newSource
  }
  if (useLegacyStructure && lang) {
    const legacySource = attrs?.[`${fieldName}_${lang}`]
    if (hasListItems(legacySource)) {
      return legacySource
    }
  }
  return newSource
}
