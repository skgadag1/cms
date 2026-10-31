import info.magnolia.jcr.predicate.NodeTypePredicate;

workspaces = [
        ["products","dxp:product"],
        ["attachments","dxp:attachment"]
]

def main() {
    workspaces.each {
        workspace -> updateWorkspace(workspace[0], workspace[1])
    }
}

def updateWorkspace(workspace, nodeType) {
    println "-Update workspace=" + workspace
    session = ctx.getJCRSession(workspace)
    bobcat = session.getRootNode().getNode("bobcat")
    nodes =  NodeUtil.collectAllChildren(bobcat, new NodeTypePredicate(nodeType))
    println "Nodes to update: " + nodes.size()
    nodes.each {
        if(it.hasNode("tags")){

            tags = it.getNode("tags").getNodes()
            tagsIds = []
            tags.each { tag ->
                tagsIds.add(tag.getProperty("dxpTag").getValue().getString())
            }
            PropertyUtil.setProperty(it, "tags", tagsIds)
            it.getNode("tags").remove()
        }
    }

    session.save()
}

main()