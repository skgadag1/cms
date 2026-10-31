import info.magnolia.jcr.predicate.NodeTypePredicate

regions = ['br']
suffixToRemove = "Doosan Bobcat Brasil"
// provide the suffix to remove without dashes and spaces
// e.g. "Doosan Bobcat Brasil" will remove " - Doosan Bobcat Brasil" from the title
//it can be modified in removeSuffixFromTitle method
langs = ["en", "es", "fr", "de", "it", "nl", "pl", "ru", "no", "pt", "zh", "ko"]
counter = 0

def main () {
    regions.each {
        region -> updateRegion(region)
    }
}

def updateRegion(region) {
    println "-Update region = " + region
    session = ctx.getJCRSession("website")
    regionNode = session.getRootNode().getNode("bobcat/"+region)
    nodes =  NodeUtil.collectAllChildren(regionNode, new NodeTypePredicate("mgnl:page"))
    println "Nodes to check: " + nodes.size()
    nodes.each {
        node -> {
            langs.each {
                lang -> {
                        removeSuffixFromNodeTitle(node, suffixToRemove, "title_" + lang)
                }
            }
            removeSuffixFromNodeTitle(node, suffixToRemove, "title")
        }
    }
    session.save()
    println "Nodes updated: " + counter
}

def removeSuffixFromNodeTitle(node, suffix, titlePropertyName) {
    try{
        if (node.hasProperty(titlePropertyName)) {
            title = node.getProperty(titlePropertyName).getValue().getString()
            titleWithoutSuffix = removeSuffixFromTitle(title, suffix)
            node.setProperty(titlePropertyName, titleWithoutSuffix)
            counter++
        }
    } catch (ignored) {
        println "No title found for: " + node.getPath() + " and property " + titlePropertyName
    }
}

def removeSuffixFromTitle(title, suffix) {
    def indexOfSuffixStart = title.lastIndexOf(' ' + suffix)
    indexOfSuffixStart -= 2 // remove space and dash before suffix
    if (indexOfSuffixStart >= 0 && title.endsWith(suffix)) {
        def newTitle = title.substring(0, indexOfSuffixStart)
        return newTitle.trim()
    } else {
        return title
    }
}

main()