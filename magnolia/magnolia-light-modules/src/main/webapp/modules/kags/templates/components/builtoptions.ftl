[#include "/kags/templates/macros/common.ftl"]

<div id="builtoptions"
     data-config='{
         "title": "${content.title!}"[#if content.icon?has_content],
         "icon": "${content.icon!}"[/#if][#if content.showBreadcrumb!?is_boolean],
         "showBreadcrumb": "${content.showBreadcrumb?c}"[/#if],
         "bannerUrl": "[@damLink content.banner!/]",
         "itemNumberLabel": "${content.itemNumberLabel!}",
         "descriptionLabel": "${content.descriptionLabel!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/builtoptions.js" type="module"></script>
