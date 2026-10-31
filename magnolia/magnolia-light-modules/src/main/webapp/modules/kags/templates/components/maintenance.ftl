[#include "/kags/templates/macros/common.ftl"]

<div id="maintenanceschedule"
     data-config='{
        "title": "${content.title!}"[#if content.icon?has_content],
        "icon": "${content.icon!}"[/#if][#if content.showBreadcrumb!?is_boolean],
        "showBreadcrumb": "${content.showBreadcrumb?c}"[/#if],
        "bannerUrl": "[@damLink content.banner!/]",
        "maintenanceDownloadTitle": "${content.maintenanceDownloadTitle!}",
        "maintenanceInfoLabel": "${content.maintenanceInfoLabel!}",
        "intervalHeader": "${content.intervalHeader!}",
        "kitHeader": "${content.kitHeader!}",
        "machineNotFound": "${content.machineNotFound!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/maintenance.js" type="module"></script>
