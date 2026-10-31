import info.magnolia.jcr.predicate.NodeTypePredicate
import com.tbscg.doosan.magnolia.dxp.core.ui.action.event.DxpActionCommittedEvent
import com.tbscg.doosan.magnolia.bobcat.event.PagesEventHandler

def main() {

    println "-----Committing pages"

    commitNodes("website", "mgnl:page")
}


def commitNodes(workspaceName, predicate) {
    println "---Action for workspace=" + workspaceName + " nodeType=" + predicate

    session = ctx.getJCRSession(workspaceName)
    pageNodes = NodeUtil.collectAllChildren(session.getNode("/bobcat"), new NodeTypePredicate(predicate))
    pagesEventHandler = new PagesEventHandler()
    pageNodes.each {
        node ->
            commitNode(node, pagesEventHandler)
    }
}

def commitNode(node, pagesEventHandler) {
    try {
        println "---Action for node=" + node
        path = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPath(node)
        id = info.magnolia.jcr.util.NodeUtil.getNodeIdentifierIfPossible(node)
        nodeType = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getPrimaryNodeType(node)
        workspace = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.tryGetNodeWorkspace(node).orElse("")
        lastModified = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getLastModifiedDate(node).getTimeInMillis()
        parentId = com.tbscg.doosan.magnolia.dxp.core.util.NodeUtil.getParentNodeId(node)
        pagesEventHandler.onActionCommitted(new DxpActionCommittedEvent(id, parentId, path, nodeType, workspace, lastModified))
    } catch (e) {
        println e
        println "Item " + node.getName() + " not exists"
    }
}

main()

return "done"