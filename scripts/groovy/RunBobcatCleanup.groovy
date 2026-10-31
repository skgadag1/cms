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
        workspace -> deleteWorkspace(workspace)
    }
}

def deleteWorkspace(workspace) {
    try {
        println "-Delete workspace=" + workspace

        session = ctx.getJCRSession(workspace)
        root = session.getRootNode().getNode("bobcat")
        root.remove()

        session.save()
    }catch(e) {
        println e
    }

}

main()
return "done"