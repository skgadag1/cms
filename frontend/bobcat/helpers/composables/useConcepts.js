import {inEditorEdit} from '~/helpers/vue-editor'

export default function useConcepts() {
  const generateConceptHint = (_key, _alternative = null) => {
    return inEditorEdit && _key ? `Concept path: bobcat/common/labels/${_key} or bobcat/_region_/common/labels/${_key}` : _alternative
  }

  return {
    generateConceptHint
  }
}
