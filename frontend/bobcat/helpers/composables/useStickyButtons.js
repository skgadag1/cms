export default function useStickyButtons() {
  const initButtons = (isDealerInventoryPage, buttonActions, primaryButton) => {
    const buttons = isDealerInventoryPage
      ? [
        {selector: 'a[data-test-id="reserve-btn"]', defaultLabel: 'Reserve'},
        {selector: 'button[data-test-id="view-specifications-btn"]', defaultLabel: 'View Specifications'},
        {selector: 'button[data-test-id="info-sheet-download-btn"]', defaultLabel: 'Download Info Sheet'},
        {selector: 'button[data-test-id="share-btn"]', defaultLabel: 'Share'}
      ]
      : [
        {selector: 'a[data-test-id="model-pricing-build-and-quote-link"]', defaultLabel: 'Build and Price'}
      ]

    buttons.forEach((button) => {
      const btnElement = document.querySelector(button.selector)
      if (btnElement) {
        button.label = btnElement.innerText || button.defaultLabel
        button.isLoading = false
        buttonActions.value.push(button)
      }
    })

    if (isDealerInventoryPage) {
      const reserveBtn = buttonActions.value.find(button => button.selector === 'a[data-test-id="reserve-btn"]')
      const viewSpecBtn = buttonActions.value.find(button => button.selector === 'button[data-test-id="view-specifications-btn"]')

      if (reserveBtn) {
        primaryButton.value = reserveBtn
        buttonActions.value = buttonActions.value.filter(button => button !== reserveBtn)
      } else if (viewSpecBtn) {
        primaryButton.value = viewSpecBtn
        buttonActions.value = buttonActions.value.filter(button => button !== viewSpecBtn)
      }
    } else {
      const buildAndQuoteBtn = buttonActions.value.find(button => button.selector === 'a[data-test-id="model-pricing-build-and-quote-link"]')
      if (buildAndQuoteBtn) {
        primaryButton.value = buildAndQuoteBtn
        buttonActions.value = buttonActions.value.filter(button => button !== buildAndQuoteBtn)
      }

      const pricingLinksContainer = document.querySelector('ul[data-test-id="model-pricing-links"]')
      if (pricingLinksContainer) {
        const pricingLinks = pricingLinksContainer.querySelectorAll('a')
        pricingLinks.forEach((link) => {
          buttonActions.value.push({
            selector: link,
            label: link.innerText,
            isLoading: false
          })
        })
      }
    }
  }

  return {
    initButtons
  }
}
