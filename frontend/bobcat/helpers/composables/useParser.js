/**
 * Parser utilities composable
 */
export default function useParser() {
  /**
   * Parses a boolean value from various input types
   * @param {boolean|string} value - The value to parse as boolean
   * @returns {boolean} - The parsed boolean value
   */
  const parseBoolean = (value) => {
    if (typeof value === 'boolean') { return value }
    if (typeof value === 'string') { return value.toLowerCase() === 'true' }
    return false
  }

  return {
    parseBoolean
  }
}
