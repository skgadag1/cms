[#include "/kags/templates/macros/common.ftl"]
[#assign fieldmodsPage = cmsfn.parent(content, "mgnl:page")! ]
<div id="fieldmods"
     data-config='{
         "title": "${content.title!}"[#if content.icon?has_content],
         "icon": "${content.icon!}"[/#if][#if content.showBreadcrumb!?is_boolean],
         "showBreadcrumb": "${content.showBreadcrumb?c}"[/#if],
         "bannerUrl": "[@damLink content.banner!/]",
         "statusHeader": "${content.statusHeader!}",
         "classHeader": "${content.classHeader!}",
         "codeHeader": "${content.codeHeader!}",
         "descriptionHeader": "${content.descriptionHeader!}",
         "expiresOnHeader": "${content.expiresOnHeader!}",
         "noPending": "${content.noPending!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/fieldmods.js" type="module"></script>
