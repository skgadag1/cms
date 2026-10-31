//------------------------------IMPORTANT---------------------------------
// This is a backup of script created in Magnolia CMS admin panel.
// To properly use it, below content need to be copied and pasted to new
// (in case of missing currently created script) script in Magnolia Groovy
// scrips section: https://magnolia-cms.dev.dice-tools.com/.magnolia/admincentral#app:groovy:browser;/:treeview:
//------------------------------------------------------------------------

import info.magnolia.jcr.predicate.NodeNamePredicate;
import info.magnolia.jcr.predicate.NodeTypePredicate;

LISTING_ONLY_MODE = true
VALUE_TO_SET = 'automatic'
ALLOWED_PREFIXES = ["/bobcat/na/mktest", "/bobcat/na/test"] // Example how to narrow searching

session = ctx.getJCRSession('website')
skipped = []
componentNodes = NodeUtil.collectAllChildren(session.getRootNode(), new NodeTypePredicate("mgnl:component"))

componentNodes.each { componentNode ->
    if (!(componentNode.hasProperty("jcr:primaryType") && componentNode.hasProperty("mgnl:template") &&
            componentNode.getProperty('mgnl:template').getValue().getString() == 'bobcat:components/modelPricing'
    )) {
        return
    }

    modelPricingNode = NodeUtil.unwrap(componentNode)
    prefixMatched = false

    ALLOWED_PREFIXES.each(ap -> {
        if (modelPricingNode.getPath().startsWith(ap)) {
            prefixMatched = true
        }
    })

    if (!prefixMatched) {
        skipped.push(componentNode)
        return
    }

    switchable = NodeUtil.collectAllChildren(modelPricingNode, new NodeNamePredicate("switchable"))
    switchableNode = NodeUtil.unwrap(switchable[0])
    field = switchableNode.getProperty("field")
    currentVal = field.getValue().getString()

    if (!LISTING_ONLY_MODE) {
        if (currentVal == VALUE_TO_SET) {
            println "Already set to " + VALUE_TO_SET + " - " + field.toString()
        } else {
            switchableNode.setProperty("field", VALUE_TO_SET)

            println "Changed: " + currentVal + " -> " + switchableNode.getProperty("field").getValue().getString() + " - " + field.toString()
        }
    } else {
        println "Change skipped (Listing mode) - " + field.toString()
    }
}

println '\nSkipped due to prefix mismatch'
skipped.each(s -> {
    println s
})

session.save()
