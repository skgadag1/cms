import com.cloudinary.Cloudinary
import info.magnolia.jcr.predicate.NodePropertyNamePredicate
import org.cloudinary.json.JSONObject
import static com.cloudinary.utils.ObjectUtils.asMap

client = getCloudinary()

workspaces = [
        "formsType",
        "formsConfiguration",
        "formsEmailConfig",
        "website",
        "features",
        "products",
        "attachments",
        "marketingCampaigns",
        "marketing-tags",
        "concepts",
        "dxpTags",
        "faq",
        "facets",
        "navigation",
        "regions"
]

def main() {
    workspaces.each {
        workspace -> updateWorkspace(workspace)
    }
}

def updateWorkspace(workspace) {
    try {
        println "-Update workspace=" + workspace
        session = ctx.getJCRSession(workspace)
        bobcat = session.getRootNode().getNode("bobcat")
        bobcat.getNodes().each {
            println "Update root=" + it.getPath()
            it.getNodes().each { subRoot -> updateSubNodesOfParent(subRoot, session) }
        }
        updateSubNodesOfParent(bobcat, session)
    } catch (e) {
        println e
    }
}

def updateSubNodesOfParent(subRoot, session) {
    println "Execute for subRoot=" + subRoot.getPath()
    nodes = getAllSrc(subRoot)
    println "Nodes to update: " + nodes.size()
    nodes.each {
            println "For node with path" + it.getPath()
            newSrc = getNewAssetSrc(it.getProperty("src").getValue().getString())
            it.setProperty("src", newSrc)
    }

    session.save()
}

def getAsset(path) {
    resp = client.search()
            .expression("public_id=\"" + path + "*\"")
            .withField("context")
            .withField("metadata")
            .maxResults(1)
            .execute()
    return new JSONObject(resp).getJSONArray("resources").getJSONObject(0)
}

def getCloudinary() {
    return new Cloudinary(asMap(
            "cloud_name", "doosan-bobcat-qa",
            "api_key", "",
            "api_secret", ""
    )) as Object
}

static def encode(str) {
    return str.bytes.encodeBase64().toString()
}

static def decode(str) {
    return str.decodeBase64()
}

def getAllSrc(root) {
    nodes = NodeUtil.collectAllChildren(root, new NodePropertyNamePredicate("src"))
    unwrappedNodes = []
    nodes.each {
            unwrappedNodes.push(NodeUtil.unwrap(it))
    }

    return nodes.findAll {
            prop = it.getProperty("src").getValue().getString()
            if (prop.startsWith("cloudinary") && !it.getName().equals("video") && !prop.startsWith("cloudinary:prod-assets"))
                return true
            return false
    }
}

def getNewAssetSrc(oldPath) {
    pathToSearch = "prod-assets/" + oldPath.split("cloudinary:")[1].split("#")[0]
    try {
        map = getAsset(pathToSearch)
        public_id = map.get("public_id")
        format = map.get("format")
        map.put("public_id", public_id + "." + format)

        postfix = encode(map.toString())
        return "cloudinary:" + public_id + "#" + postfix
    } catch (ignored) {
        println oldPath + " not found"
        return pathToSearch
    }
}

main()
return "done"