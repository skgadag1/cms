export default function useDataLayer() {
  const setDataLayer = (event, properties) => {
    if (window?.dataLayer) { window.dataLayer.push({event, ...properties}) }
  }

  return {
    setDataLayer
  }
}
