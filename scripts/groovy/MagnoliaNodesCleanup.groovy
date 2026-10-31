import info.magnolia.jcr.predicate.NodeTypePredicate

ghostMode = true

def main() {

    println "-----Cleanup"
    cleanupData = [
            ["mgnl:contentNode", "products", "tags_"],
            ["mgnl:contentNode", "attachments", "tags_"],
            ["mgnl:contentNode", "website", "dxpTags_"]
    ]

    cleanupData.each {
        data -> deleteNodes(data[0], data[1], data[2])
    }
}

def getNodeChildrenByNodeType(node, predicate) {
    return NodeUtil.collectAllChildren(node, new NodeTypePredicate(predicate))
}

def deleteNodes(predicate, workspaceName, prefix) {
    println "---Action for workspace=" + workspaceName + " nodeType=" + predicate + " prefix=" + prefix

    session = ctx.getJCRSession(workspaceName)
    productNodes = getNodeChildrenByNodeType(session.getNode("/bobcat"), predicate)

    filteredNodes = productNodes.findAll {
        node -> nodesFilter(node, prefix)
    }

    filteredNodes.each {
        node ->
            deleteNode(node)
    }

    if (!ghostMode) {
        session.save()
    }
}

def nodesFilter(node, prefix) {
    values = node.getPath().split('/')
    return values[values.size() - 1].startsWith(prefix)
}

def deleteNode(node) {
    try {
        println "-Delete node=" + node.getPath()
        node.remove()
    } catch (ignore) {
        println "Item " + node.getName() + " not exists"
    }
}

main()

return "done"