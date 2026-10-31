import {ref, useContext} from '@nuxtjs/composition-api'
import {PRINT_REGIONS} from '~/constants'

export default function usePrintSpecs(model, currentUom, modelType) {
  const {$utils, $services} = useContext()
  const isPrinting = ref(false)

  const printSpec = (cropCloudinaryGallery = false) => {
    isPrinting.value = true

    // hiding elements needs a little delay
    setTimeout(() => {
      const areas = [...document.querySelectorAll('div[data-test-id="area"]')]
      const cldG = document.querySelector('div[data-test="gallery-wrap"]')
      const expandBtn = document.querySelector('span[data-test-id="gallery-btn-expand"]')

      if (areas?.length) {
        const header = areas.filter(x => x.dataset.testName === 'area-header')[0]
        const footer = areas.filter(x => x.dataset.testName === 'area-footer')[0]

        header?.classList.add('d-print-none')
        footer?.classList.add('d-print-none')

        if (cropCloudinaryGallery) {
          cldG.style.maxWidth = '80%'
          expandBtn.style.display = 'none'
        }

        window.print()

        if (cropCloudinaryGallery) {
          cldG.style.maxWidth = 'unset'
          expandBtn.style.display = 'block'
        }

        header?.classList.remove('d-print-none')
        footer?.classList.remove('d-print-none')
        isPrinting.value = false
      }
    }, 200)
  }

  const printSpecsTableOnly = () => {
    isPrinting.value = true
    setTimeout(() => {
      const specsSection = document.querySelector('[data-test-id="product-specifications"], [data-test-id="attachment-specifications"]')

      if (specsSection) {
        const scrollX = window.scrollX
        const scrollY = window.scrollY

        const hiddenElements = []

        document.querySelectorAll('body *').forEach((element) => {
          if (!specsSection.contains(element) &&
              !element.contains(specsSection) &&
              element !== specsSection) {
            const originalDisplay = element.style.display
            hiddenElements.push({element, originalDisplay})
            element.style.display = 'none'
          }
        })

        const originallyCollapsed = []
        const originallyExpanded = []
        const collapseElements = specsSection.querySelectorAll('.collapse')

        collapseElements.forEach((element) => {
          if (element.classList.contains('show')) {
            originallyExpanded.push(element)
          } else {
            originallyCollapsed.push(element)
            element.classList.add('show')
            element.style.display = 'block'
          }
        })

        const printStyles = document.createElement('style')
        printStyles.innerHTML = `
          @media print {
            @page {
              margin: 15mm;
              size: letter;
            }
            body, html {
              margin: 0 !important;
              padding: 0 !important;
              height: 100%;
            }
            [data-test-id="product-specifications"],
            [data-test-id="attachment-specifications"] {
              margin: 0 !important;
              padding: 20px !important;
            }
          }
        `
        document.head.appendChild(printStyles)

        setTimeout(() => {
          window.print()

          const cleanup = () => {
            hiddenElements.forEach(({element, originalDisplay}) => {
              element.style.display = originalDisplay
            })

            originallyCollapsed.forEach((element) => {
              element.classList.remove('show')
              element.style.display = ''
            })

            originallyExpanded.forEach((element) => {
              if (!element.classList.contains('show')) {
                element.classList.add('show')
              }
            })

            if (document.head.contains(printStyles)) {
              document.head.removeChild(printStyles)
            }

            window.scrollTo(scrollX, scrollY)

            isPrinting.value = false
          }

          setTimeout(cleanup, 100)

          const afterPrintHandler = () => {
            cleanup()
            window.removeEventListener('afterprint', afterPrintHandler)
          }
          window.addEventListener('afterprint', afterPrintHandler)
        }, 100)
      } else {
        isPrinting.value = false
      }
    }, 200)
  }

  const bu = $utils.products.getBusinessUnit(model)

  const downloadPDF = async () => {
    const blob = await $services.products.downloadPdfSpecification(model?.id, bu, currentUom.value.value, modelType)
    $utils.downloadFile(blob, `${model?.id}.pdf`)
  }

  const downloadOrPrintPDF = async () => {
    try {
      if (PRINT_REGIONS.includes(bu)) {
        await downloadPDF()
      } else { printSpec() }
    } catch (error) {
      printSpec()
    }
  }

  const downloadOrPrintPDFSpecsOnly = async () => {
    try {
      if (PRINT_REGIONS.includes(bu)) {
        await downloadPDF()
      } else { printSpecsTableOnly() }
    } catch (error) {
      printSpecsTableOnly()
    }
  }

  const showPrintOnMobile = PRINT_REGIONS.includes(bu)

  return {
    isPrinting,
    downloadOrPrintPDF,
    downloadOrPrintPDFSpecsOnly,
    showPrintOnMobile,
    printSpec,
    printSpecsTableOnly
  }
}
