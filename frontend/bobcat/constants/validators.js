const caValidator = /^[ABCEGHJ-NPRSTVXY]\d[ABCEGHJ-NPRSTV-Z][ -]?\d[ABCEGHJ-NPRSTV-Z]\d$/i
const usValidator = /(^\d{5}$)|(^\d{9}$)|(^\d{5}-\d{4}$)/

export {
  caValidator,
  usValidator
}
