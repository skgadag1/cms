import info.magnolia.context.MgnlContext
import javax.jcr.*
import javax.jcr.query.*

Set<String> availableComponents = [
        'bobcat:components/header',
        'bobcat:components/footer',
        'bobcat:components/gallery',
        'bobcat:components/gallery-v2',
        'bobcat:components/herospot',
        'bobcat:components/evolution-herospot-v2',
        'bobcat:components/flexiblelisting',
        'bobcat:components/cta',
        'bobcat:components/disclaimer',
        'bobcat:components/multi-disclaimers',
        'bobcat:components/promotional-content',
        'bobcat:components/modelPricing',
        'bobcat:components/notification-bar',
        'bobcat:components/content-carousel',
        'bobcat:components/sectionText',
        'bobcat:components/evolutionSectionText',
        'bobcat:components/sectionTextRegionalized',
        'bobcat:components/captioned-image',
        'bobcat:components/categories-landing-page',
        'bobcat:components/categorized-captioned-image',
        'bobcat:components/accordion',
        'bobcat:components/HTMLSlot',
        'bobcat:components/productOverview',
        'bobcat:components/attachmentInfo',
        'bobcat:components/features',
        'bobcat:components/productSpecifications',
        'bobcat:components/attachmentSpecifications',
        'bobcat:components/quote',
        'bobcat:components/header-section',
        'bobcat:components/text-section',
        'bobcat:components/socialShare',
        'bobcat:components/video',
        'bobcat:components/mid-buying-tools',
        'bobcat:components/partsFinder',
        'bobcat:components/download-table',
        'bobcat:components/form',
        'bobcat:components/formCaptcha',
        'bobcat:components/product-listing',
        'bobcat:components/page-listing',
        'bobcat:components/text-listing',
        'bobcat:components/series-navigation',
        'bobcat:components/iframe',
        'bobcat:components/buildQuoteSelector',
        'bobcat:components/region-selector',
        'bobcat:components/attachment-related-products',
        'bobcat:components/faq',
        'bobcat:components/breadcrumb',
        'bobcat:components/accordion-group',
        'bobcat:components/back-to-top-button',
        'bobcat:components/tableManual',
        'bobcat:components/model-lookup',
        'bobcat:components/products-navigation',
        'bobcat:components/related-links',
        'bobcat:components/previously-viewed',
        'bobcat:components/full-width-promo-model',
        'bobcat:components/separator',
        'bobcat:components/dealer-inventory-search',
        'bobcat:components/dealer-inventory-category-search',
        'bobcat:components/dealer-inventory-listing',
        'bobcat:components/packages-and-options',
        'bobcat:components/dealer-model-pricing',
        'bobcat:components/dealer-model-pricing-v2',
        'bobcat:components/media-spotlight',
        'bobcat:components/feature-hero-spot',
        'bobcat:components/mid-page-hero-gallery',
        'bobcat:components/diagnostic-model-information',
        'bobcat:components/coveo-search',
        'bobcat:components/sticky-cta',
        'bobcat:components/side-nav-gallery',
        'bobcat:components/dealer-banner',
        'bobcat:components/dealer-locator-search-header',
        'bobcat:components/evolution-value-proposition'
]
workspaces = ["website"]

def getUsedComponents(session) {
    def usedComponents = new HashSet<>()
    def queryManager = session.workspace.queryManager
    def query = queryManager.createQuery("SELECT * FROM [nt:base] WHERE [mgnl:template] IS NOT NULL", Query.JCR_SQL2)
    def result = query.execute()

    result.nodes.each { node ->
        if (node.hasProperty("mgnl:template")) {
            usedComponents.add(node.getProperty("mgnl:template").getString())
        }
    }
    return usedComponents
}

def identifyUnusedComponents(availableComponents) {
    workspaces.each { workspaceName ->
        try {
            def session = MgnlContext.getJCRSession(workspaceName)

            def usedComponents = getUsedComponents(session)

            availableComponents.removeAll(usedComponents)

            println "Checked workspace: ${workspaceName}"
            println ""

            if (availableComponents.isEmpty()) {
                println "No more components to scan"
                return
            }

        } catch (Exception e) {
            println "Error processing workspace: ${workspaceName} - ${e.message}"
        }
    }

    if (!availableComponents.isEmpty()) {
        println "Remaining unused components after scanning: ${availableComponents}"
    }
}

identifyUnusedComponents(availableComponents)
