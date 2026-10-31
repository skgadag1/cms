[#include "/kags/templates/macros/common.ftl"]

<div id="machinesearch"
     data-config='{
         "bannerUrl": "[@damLink content.banner!/]",
         "title": "${content.title!}",
         "subtitle": "${content.subtitle!}",
         "recentLabel": "${content.recentLabel!}",
         "machineUrl": "[@pageLink content.mikeUrl!/]"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/machinesearch.js" type="module"></script>
