[#include "/kags/templates/macros/common.ftl"]
[#assign troubleshootingPage = cmsfn.parent(content, "mgnl:page")! ]
<div id="troubleshooting"
     data-config='{
         "title": "${content.title!}",
         "errorInputUrl": "${cmsfn.link(troubleshootingPage)}",
         "keyword": "${ctx.getParameter('keyword')!}",
         "bannerUrl": "[@damLink content.banner!/]",
         "loadMoreButton": "${content.loadMoreButton!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/troubleshooting.js" type="module"></script>
