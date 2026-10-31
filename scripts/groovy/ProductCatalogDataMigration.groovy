import info.magnolia.jcr.predicate.NodeTypePredicate

ghostMode = true;

def main() {

    println "-----Transfrom legacy nodes V2"
    //v2
    updateNodes("dxp:product", this.&migrateLegacyNodesWithLangsToNewStruct, "products")
    updateNodes("dxp:attachment", this.&migrateLegacyNodesWithLangsToNewStruct, "attachments")

    println "-----Transfrom legacy nodes V1"
    //v1
    updateNodes("dxp:product", this.&migrateLegacyNodesToNewStruct, "products")
    updateNodes("dxp:attachment", this.&migrateLegacyNodesToNewStruct, "attachments")

    println "-----Transfrom legacy feature nodes"
    //FEATURE
    updateNodes("dxp:feature", this.&migrateLegacyFeaturesNodesToNewStruct, "features")

    println "-----Transform category nodes"
    //CATEGORIES
    updateNodes("dxp:productGroup", this.&migrateLegacyNodesWithLangsToNewStruct, "products")
    updateNodes("dxp:category", this.&updatePropsInLegacyCategory, "products")
    updateNodes("dxp:subcategory", this.&updatePropsInLegacyCategory, "products")
    updateNodes("dxp:attachmentGroup", this.&migrateLegacyNodesWithLangsToNewStruct, "attachments")
    updateNodes("dxp:attachmentGroup", this.&migrateLegacyNodesToNewStruct, "attachments")

    println "-----Delete empty nodes"
    //CLEANUP
    updateNodes("dxp:product", this.&deleteOldImageNodes, "products")
    updateNodes("dxp:attachmentGroup", this.&deleteOldImageNodes, "attachments")
    updateNodes("dxp:attachment", this.&deleteOldImageNodes, "attachments")
    updateNodes("dxp:feature", this.&deleteOldFeatureNodes, "features")

}

def updateNodes(predicate, cb, workspaceName) {
    println "---Action for workspace=" + workspaceName + " nodeType=" + predicate

    session = ctx.getJCRSession(workspaceName)
    productNodes = getNodeChildrenByNodeType(session.getRootNode(), predicate)

    productNodes.each {
        node ->
            updateImages(node, cb)
    }

    if (!ghostMode) {
        session.save()
    }
}

def getNodeChildrenByNodeType(node, predicate) {
    return NodeUtil.collectAllChildren(node, new NodeTypePredicate(predicate))
}

def updateImages(componentNode, cb) {
    unwrapedProductNode = NodeUtil.unwrap(componentNode)
    productNodeChildren = getNodeChildrenByNodeType(unwrapedProductNode, "mgnl:contentNode")

    productNodeChildren.each {
        componentChildren ->
            cb(componentChildren, unwrapedProductNode)
    }
}

//V1
def migrateLegacyNodesToNewStruct(node, parent) {
    path = node.getPath()

    if (
    !path.equals(parent.getPath() + "/images") ||
            isNodeExists(node, "asset") ||
            !parent.getNode('images').hasNode('images0') ||
            !parent.getNode('images').getNode('images0').hasProperty('src')) {
        return
    }

    println "-Transform root node " + path
    unwrapedChild = NodeUtil.unwrap(node)
    imageNodes = getNodeChildrenByNodeType(unwrapedChild, "mgnl:contentNode")

    imageNodes.each {
        imageNode ->
            transformLegacyImageNode(imageNode)
    }

}

def isNodeExists(node, name) {
    unwrapedNode = NodeUtil.unwrap(node)
    nodeChildrens = getNodeChildrenByNodeType(unwrapedNode, "mgnl:contentNode")

    nodeExists = false
    nodeChildrens.each {
        nodeChildren ->
            if (nodeChildren.getPath().endsWith(name)) {
                nodeExists = true
            }
    }

    return nodeExists
}

def transformLegacyImageNode(imageNode) {
    src = cutPropFromNode(imageNode, 'src')
    if (src == null) {
        return
    }

    println "Move src from " + imageNode.getPath() + "to new Asset childNode";
    createAssetNodesWithProperties(imageNode, src.getString())
}

def cutPropFromNode(node, propName) {
    try {
        src = node.getProperty(propName).getValue()
        node.getProperty(propName).remove()
        return src
    } catch (ignored) {
        return null
    }
}

def createAssetNodesWithProperties(node, src) {
    println "Create asset in " + node.getPath() + " and set src";
    node.addNode("asset", "mgnl:contentNode").setProperty("field", "img")
    node.addNode("asset/img", "mgnl:contentNode").setProperty("src", src)
}

//V2
def migrateLegacyNodesWithLangsToNewStruct(node, parent) {
    path = node.getPath()

    if (
    !path.equals(parent.getPath() + "/images") ||
            isNodeExists(node, "asset") ||
            !isNodeExists(node, "image") ||
            !parent.getNode('images').hasNode('images0')) {
        return
    }

    println "-Transform root node " + parent.getPath()

    languages = extractLanguages(parent)
    imagesCount = node.getNodes().size()

    transformLangNodes(parent, languages, imagesCount)
}

def extractLanguages(node) {
    props = node.getNode('images').getNode('images0').getNode("image").getProperties()
    return getLanguageList(props)
}

def getLanguageList(props) {
    languagesList = [] as HashSet

    for (prop in props) {
        if (prop.getName().contains("_")) {
            languagesList.add(prop.getName().split("_")[1])
        }
    }
    return languagesList
}

def transformLangNodes(parent, languages, imagesCount) {
    createLanguageImagesStructure(parent, languages, imagesCount)
    moveProperties(parent, languages, imagesCount)
}

def createLanguageImagesStructure(parent, langs, count) {
    createBasicImageNodes(parent, langs)
    createBasicStructureInRootNode(parent, count)

    for (lang in langs) {
        createStructureForImagesByLang(parent, "_" + lang, count)
    }
}

def createBasicImageNodes(parent, langs) {
    for (lang in langs) {
        try {
            parent.addNode("images_" + lang, "mgnl:contentNode")
            println "Added images_" + lang + " to node=" + parent.getPath()
        } catch (ignored) {
        }
    }
}

def createBasicStructureInRootNode(parent, count) {
    imgNode = parent.getNode("images")
    count.times {
        i ->
            createAssetNodesWithProperties(imgNode.getNode("images" + i), "")
    }
}

def createStructureForImagesByLang(parent, lang, count) {
    imgNode = parent.getNode("images" + lang)

    for (i = 0; i < count; i++) {
        nodeName = "images" + i + lang
        createBasicLangNode(imgNode, "images" + i + lang)
    }
}

def createBasicLangNode(imgNode, nodeName) {
    try {
        println "Create " + nodeName;
        node = imgNode.addNode(nodeName, "mgnl:contentNode")
        createAssetNodesWithProperties(node, "")
    } catch (ignore) {
    }
}

def moveProperties(parent, langs, count) {
    for (lang in langs) {
        movePropertiesByLang(parent, "_" + lang, count)
    }

    movePropertiesInMainNode(parent, count)
}

def movePropertiesByLang(parent, lang, count) {
    rootNode = parent.getNode("images" + lang)
    sourceRootNode = parent.getNode("images")
    for (i = 0; i < count; i++) {
        imgNode = rootNode.getNode("images" + i + lang).getNode("asset").getNode("img")
        sourceNode = sourceRootNode.getNode("images" + i).getNode("image")

        copyPropBetweenNodes(sourceNode, "src", imgNode, "src")
        movePropBetweenNodes(sourceNode, "alt" + lang, imgNode, "imageAlt" + lang)
        movePropBetweenNodes(sourceNode, "title" + lang, imgNode, "imageTitle" + lang)
    }
}

def copyPropBetweenNodes(source, sourceProp, destination, destinationProp) {
    println "COPY prop=" + source.getPath() + "/" + sourceProp + " to prop=" + destinationProp
    prop = source.getProperty(sourceProp).getValue()
    destination.setProperty(destinationProp, prop)
}

def movePropBetweenNodes(source, sourceProp, destination, destinationProp) {
    prop = cutPropFromNode(source, sourceProp)
    if (!prop) {
        return
    }
    println "MOVE prop=" + source.getPath()  + "/" + sourceProp + " to prop=" + destinationProp
    destination.setProperty(destinationProp, prop.getString())
}

def movePropertiesInMainNode(parent, count) {
    rootNode = parent.getNode("images")
    for (i = 0; i < count; i++) {
        imgNode = rootNode.getNode("images" + i).getNode("asset").getNode("img")
        sourceNode = rootNode.getNode("images" + i).getNode("image")

        movePropBetweenNodes(sourceNode, "src", imgNode, "src")
        movePropBetweenNodes(sourceNode, "alt", imgNode, "imageAlt")
        movePropBetweenNodes(sourceNode, "title", imgNode, "imageTitle")
    }
}

//PROPS

def updatePropsInLegacyCategory(componentChildren, parent) {
    path = componentChildren.getPath()

    if (!path.equals(parent.getPath() + "/image")) {
        return
    }

    println "-Transform root node " + path

    for (prop in componentChildren.getProperties()) {
        name = prop.getName()
        if (name.contains('alt') || name.contains('title')) {
            propData = cutPropFromNode(componentChildren, name)
            println "Transform " + name + " to image" + capitalizeString(name)
            componentChildren.setProperty("image" + capitalizeString(name), propData)
        }
    }
}

def capitalizeString(str) {
    return str.substring(0, 1).toUpperCase() + str.substring(1)
}

//FEATURES
def migrateLegacyFeaturesNodesToNewStruct(node, parent) {
    path = node.getPath()
    if (
    isNodeExists(parent, "assets") ||
            !path.endsWith('img') ||
            !node.hasProperty("src")
    ) {
        return
    }
    println "-Transform root node " + parent

    languages = extractLanguagesFromFeatureNode(parent)
    transformFeatureNodes(parent, languages)
}

def extractLanguagesFromFeatureNode(parent) {
    props = parent.getNode('img').getProperties()
    return getLanguageList(props)
}

def transformFeatureNodes(parent, languages) {
    createFeaturesImagesStructure(parent, languages)
    moveFeatureProperties(parent, languages)
}

def createFeaturesImagesStructure(parent, langs) {
    createBasicFeatureNodes(parent, langs)

    createBasicFeatureStructureInRootNode(parent)

    for (lang in langs) {
        createBasicFeatureStructureByLang(parent, "_" + lang)
    }

}

def createBasicFeatureNodes(parent, langs) {
    for (lang in langs) {
        try {
            parent.addNode("assets_" + lang, "mgnl:contentNode")
            println "Added assets_" + lang + " to node=" + parent.getPath()
        } catch (ignored) {
        }
    }

    println "Added assets to node=" + parent.getPath()
    parent.addNode("assets", "mgnl:contentNode")
}

def createBasicFeatureStructureInRootNode(parent) {
    println "Added assets0 to node=" + parent.getPath() + "/assets"

    imgNode = parent.getNode("assets").addNode("assets0", "mgnl:contentNode")
    createAssetNodesWithProperties(imgNode, "")
}

def createBasicFeatureStructureByLang(parent, lang) {
    imgNode = parent.getNode("assets" + lang)
    createBasicFeatureNode(imgNode, "assets0" + lang)
    createBasicFeatureNode(imgNode, "assets0" + lang)

}

def createBasicFeatureNode(imgNode, nodeName) {
    try {
        node = imgNode.addNode(nodeName, "mgnl:contentNode")
        println "Added " + nodeName + " to node=" + imgNode.getPath();
        createAssetNodesWithProperties(node, "")
    } catch (ignore) {
    }
}

def moveFeatureProperties(parent, langs) {
    for (lang in langs) {
        moveFeaturePropertiesByLang(parent, true,  "_" + lang)
    }

    moveFeaturePropertiesByLang(parent, false, "")
}

def moveFeaturePropertiesByLang(parent, isCopy, lang) {
    rootNode = parent.getNode("assets" + lang)
    sourceRootNode = parent.getNode("img")

    imgNode = rootNode.getNode("assets0" + lang).getNode("asset").getNode("img")

    if(isCopy) {
        copyPropBetweenNodes(sourceRootNode, "src", imgNode, "src")
    } else {
        movePropBetweenNodes(sourceRootNode, "src", imgNode, "src")
    }

    movePropBetweenNodes(sourceRootNode, "alt" + lang, imgNode, "imageAlt" + lang)
    movePropBetweenNodes(sourceRootNode, "title" + lang, imgNode, "imageTitle" + lang)
}

//CLEANUP
def deleteOldImageNodes(node, ignore) {
    deleteNodeByName(node, "image")
}

def deleteOldFeatureNodes(node, ignore) {
    deleteNodeByName(node, "img")
}

def deleteNodeByName(node, name) {
    try {
        path = node.getPath()
        if (!path.endsWith(name) || path.contains("asset")) {
            return
        }
        println "-Remove node=" + path;
        node.remove()
    } catch (ignore) {}
}

main()

return "done"