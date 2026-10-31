import javax.jcr.Node
import javax.jcr.NodeIterator
import javax.jcr.Session

// ========================================
// CONFIGURATION
// ========================================
DRY_RUN = true  // Set to false to apply changes
POPULATE_X_DEFAULT = true  // Set to true to populate xDefault property on regions

// ========================================
// STATISTICS
// ========================================
stats = [
    regionsProcessed: 0,
    languagesProcessed: 0,
    languagesUpdated: 0,
    languagesSkipped: 0,
    hreflangsAdded: 0,
    xDefaultsSet: 0,
    errors: 0
]

skippedLanguages = []

// ========================================
// CONSTANTS
// ========================================
X_DEFAULT_URL = "https://www.bobcat.com/regions"

// ========================================
// HREFLANG MAPPING
// ========================================
// Mapping structure: [regionCode: [languageCode: [list of hreflang values]]]
hreflangMapping = [
    'na': [
        'en': ['en-us', 'en-ca'],
        'es': ['es-us']
    ],
    'eu': [
        'en': ['en-gb', 'en-fr', 'en-it', 'en-es', 'en-de', 'en-nl', 'en-at', 'en-be', 
               'en-dk', 'en-is', 'en-gr', 'en-ie', 'en-il', 'en-pl', 'en-cy', 'en-pt', 
               'en-lv', 'en-ee', 'en-ch', 'en-ro', 'en-no', 'en-se', 'en-bg', 'en-cz', 
               'en-fi', 'en-hu', 'en-lt', 'en-si', 'en-hr', 'en-sk', 'en-rs'],
        'fr': ['fr-fr', 'fr-be', 'fr-ch'],
        'de': ['de-de', 'de-nl', 'de-at', 'de-be', 'de-ch'],
        'it': ['it-it'],
        'es': ['es-es'],
        'nl': ['nl-nl']
    ],
    'cis': [
        'ru': ['ru-am', 'ru-az', 'ru-ge', 'ru-kz', 'ru-kg', 'ru-tm', 'ru-ua', 'ru-uz']
    ],
    'mea': [
        'en': ['en-bh', 'en-eg', 'en-ma', 'en-iq', 'en-jo', 'en-ke', 'en-kw', 'en-om', 
               'en-qa', 'en-sa', 'en-za', 'en-tn', 'en-tr', 'en-ae'],
    ],
    'cn': [
        'en': ['en-cn'],
        'zh': ['zh-cn']
    ],
    'in': [
        'en': ['en-in']
    ],
    'kr': [
        'en': ['en-kr'],
        'ko': ['ko-kr']
    ],
    'la': [
        'en': ['en-ar', 'en-bo', 'en-br', 'en-gy', 'en-co', 'en-mx', 'en-py', 'en-pr', 'en-ve', 'en-jm'],
        'es': ['es-ar', 'es-bo', 'es-gt', 'es-cl', 'es-co', 'es-pe', 'es-cr', 'en-pa', 'es-do', 
               'es-ec', 'es-mx', 'es-py', 'es-pr', 'es-uy', 'es-sv', 'es-ve'],
        'pt': ['pt-br', 'pt-py']
    ],
    'oc': [
        'en': ['en-au', 'en-nz']
    ],
    'sea': [
        'en': ['en-hk', 'en-id', 'en-jp', 'en-my', 'en-ph', 'en-sg', 'en-lk', 'en-tw', 'en-th', 'en-vn']
    ]
]

// ========================================
// MAIN EXECUTION
// ========================================
def main() {
    println "========================================="
    println "Populate Hreflang Locales Script"
    println "========================================="
    println "Mode: ${DRY_RUN ? 'DRY RUN (no changes will be saved)' : 'LIVE (changes will be saved)'}"
    println "Populate xDefault: ${POPULATE_X_DEFAULT ? 'YES' : 'NO'}"
    println "=========================================\n"
    
    try {
        Session session = ctx.getJCRSession("regions")
        
        if (!session.nodeExists("/bobcat")) {
            println "ERROR: /bobcat node does not exist in regions workspace"
            return
        }
        
        Node bobcatNode = session.getNode("/bobcat")
        NodeIterator regionNodes = bobcatNode.getNodes()
        
        println "Processing regions under /bobcat...\n"
        
        while (regionNodes.hasNext()) {
            Node regionNode = regionNodes.nextNode()
            if (!regionNode.isNodeType("dxp:region")) {
                continue
            }
            
            processRegion(regionNode)
        }
        
        // Save changes if not in dry run mode
        if (!DRY_RUN) {
            println "\nSaving changes to repository..."
            session.save()
            println "Changes saved successfully!"
        } else {
            println "\nDRY RUN MODE - No changes were saved."
            println "Set DRY_RUN = false to apply changes."
        }
        
        printReport()
        
    } catch (Exception e) {
        println "\nERROR: ${e.getMessage()}"
        e.printStackTrace()
        stats.errors++
    }
    
    println "\nScript completed."
}

// ========================================
// PROCESS REGION
// ========================================
def processRegion(Node regionNode) {
    def regionName = regionNode.getName()
    println "Processing region: ${regionName}"
    stats.regionsProcessed++
    
    try {
        if (POPULATE_X_DEFAULT) {
            setXDefault(regionNode)
        }
        
        NodeIterator languageNodes = regionNode.getNodes()
        
        while (languageNodes.hasNext()) {
            Node languageNode = languageNodes.nextNode()
            
            if (!languageNode.isNodeType("dxp:language")) {
                continue
            }
            
            processLanguage(regionNode, languageNode)
        }
        
    } catch (Exception e) {
        println "  ERROR processing region ${regionName}: ${e.getMessage()}"
        stats.errors++
    }
}

// ========================================
// SET X-DEFAULT
// ========================================
def setXDefault(Node regionNode) {
    def regionName = regionNode.getName()
    def regionPath = regionNode.getPath()
    
    try {
        def currentValue = regionNode.hasProperty("xDefault") ? regionNode.getProperty("xDefault").getString() : null
        
        if (currentValue == X_DEFAULT_URL) {
            println "  xDefault already set to correct value"
            return
        }
        
        if (!DRY_RUN) {
            regionNode.setProperty("xDefault", X_DEFAULT_URL)
            stats.xDefaultsSet++
            println "  Set xDefault to: ${X_DEFAULT_URL}"
        } else {
            println "  [DRY RUN] Would set xDefault to: ${X_DEFAULT_URL}"
            if (currentValue != null) {
                println "    (current value: ${currentValue})"
            }
            stats.xDefaultsSet++
        }
        
    } catch (Exception e) {
        println "  ERROR setting xDefault on ${regionPath}: ${e.getMessage()}"
        stats.errors++
    }
}

// ========================================
// PROCESS LANGUAGE
// ========================================
def processLanguage(Node regionNode, Node languageNode) {
    def regionName = regionNode.getName()
    def languageName = languageNode.getName()
    def languagePath = languageNode.getPath()
    
    stats.languagesProcessed++
    
    println "  Processing language: ${languagePath}"
    
    try {
        def hreflangValues = getHreflangValues(regionName, languageName)
        
        if (hreflangValues == null) {
            def reason = "No mapping found for region='${regionName}' language='${languageName}'"
            println "    ${reason}"
            stats.languagesSkipped++
            skippedLanguages.add([path: languagePath, reason: reason])
            return
        }
        
        if (hreflangValues.isEmpty()) {
            def reason = "No hreflang values defined for this combination (empty list in mapping)"
            println "    ${reason}"
            stats.languagesSkipped++
            skippedLanguages.add([path: languagePath, reason: reason])
            return
        }
        
        def hasExisting = languageNode.hasNode("hreflangLocale")
        if (hasExisting) {
            println "    Found existing hreflangLocale node"
        }
        
        if (!DRY_RUN) {
            if (hasExisting) {
                Node hreflangLocaleNode = languageNode.getNode("hreflangLocale")
                println "    Removing existing hreflangLocale node"
                hreflangLocaleNode.remove()
            }
            
            Node hreflangLocaleNode = languageNode.addNode("hreflangLocale", "mgnl:contentNode")
            
            hreflangValues.eachWithIndex { hreflangValue, index ->
                Node hreflangChildNode = hreflangLocaleNode.addNode("hreflangLocale${index}", "mgnl:contentNode")
                hreflangChildNode.setProperty("field", hreflangValue)
                stats.hreflangsAdded++
                println "    Added hreflang: ${hreflangValue}"
            }
        } else {
            println "    [DRY RUN] Would ${hasExisting ? 'replace' : 'create'} hreflangLocale with ${hreflangValues.size()} value(s):"
            hreflangValues.each { hreflangValue ->
                println "      - ${hreflangValue}"
                stats.hreflangsAdded++
            }
        }
        
        stats.languagesUpdated++
        if (!DRY_RUN) {
            println "    Successfully updated with ${hreflangValues.size()} hreflang value(s)"
        }
    } catch (Exception e) {
        println "    ERROR processing language ${languagePath}: ${e.getMessage()}"
        stats.errors++
    }
}

// ========================================
// GET HREFLANG VALUES
// ========================================
def getHreflangValues(String regionCode, String languageCode) {
    def regionMapping = hreflangMapping[regionCode]
    if (regionMapping == null) {
        return null
    }
    
    def hreflangValues = regionMapping[languageCode]
    return hreflangValues
}

// ========================================
// PRINT REPORT
// ========================================
def printReport() {
    println "\n========================================="
    println "EXECUTION SUMMARY"
    println "========================================="
    println "Regions processed:     ${stats.regionsProcessed}"
    println "Languages processed:   ${stats.languagesProcessed}"
    println "Languages updated:     ${stats.languagesUpdated}"
    println "Languages skipped:     ${stats.languagesSkipped}"
    println "Hreflang values added: ${stats.hreflangsAdded}"
    if (POPULATE_X_DEFAULT) {
        println "xDefault properties set: ${stats.xDefaultsSet}"
    }
    println "Errors encountered:    ${stats.errors}"
    println "=========================================\n"
    
    if (!skippedLanguages.isEmpty()) {
        println "SKIPPED LANGUAGES (${skippedLanguages.size()}):"
        println "========================================="
        skippedLanguages.each { item ->
            println "Path:   ${item.path}"
            println "Reason: ${item.reason}"
            println "-----------------------------------------"
        }
        println "========================================="
    }
}

// ========================================
// RUN
// ========================================
main()

