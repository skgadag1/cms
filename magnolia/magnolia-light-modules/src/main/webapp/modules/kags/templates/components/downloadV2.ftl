[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
    <div id="downloadV2"
         data-config='{
         "title": "${content.titleField!}",
         "subtitle": "${content.subtitleField!}",
         "softwares": [
         [#list cmsfn.children(content.softwareItems) as link]
            {
                "softwareLabel": "${link.softwareLabel!}",
                "chosenSoftware": "${link.chosenSoftware!}",
                "softwareCategory": "[#if link.softwareCategory?has_content]${cmsfn.contentById(link.softwareCategory, 'category').displayName!}[/#if]",
                "softwareIcon": "${link.softwareIcon!}",
                "releaseNotesLabel": "${link.releaseNotesLabel!}"
            }[#if link?has_next],[/#if]
        [/#list]
        ],
        "documents": [
        [#if content.documentDownloads?has_content]
            [#list cmsfn.children(content.documentDownloads) as link]
               {
                    "documentDownloadsLabel": "${link.label!}",
                    "assetLink": "[@damLink link.asset!/]",
                    "icon": "${link.icon!}"
               }[#if link?has_next],[/#if]
            [/#list]
        [/#if]
        ]
     }'
    ></div>
    <script src="${cfgfn.kags.staticServer}/assets/js/download.js" type="module"></script>
[/#if]
