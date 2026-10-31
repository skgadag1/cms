[#include "/kags/templates/macros/common.ftl"]
[#assign faultcodesPage = cmsfn.parent(content, "mgnl:page")! ]
<div id="faultcodes"
     data-config='{
         "title": "${content.title!}"[#if content.icon?has_content],
         "icon": "${content.icon!}"[/#if][#if content.showBreadcrumb!?is_boolean],
         "showBreadcrumb": "${content.showBreadcrumb?c}"[/#if],
         "bannerUrl": "[@damLink content.banner!/]",
         "activeCodesTitle": "${content.activeCodesTitle!}",
         "statusLabel": "${content.statusLabel!}",
         "severityLabel": "${content.severityLabel!}",
         "faultCodeLabel": "${content.faultCodeLabel!}",
         "descriptionLabel": "${content.descriptionLabel!}",
         "startTimeLabel": "${content.startTimeLabel!}",
         "filterStartDateLabel": "${content.filterStartDateLabel!}",
         "filterEngineHoursLabel": "${content.filterEngineHoursLabel!}",
         "filterEngineHoursMinLabel": "${content.filterEngineHoursMinLabel!}",
         "filterEngineHoursMaxLabel": "${content.filterEngineHoursMaxLabel!}",
         "engineHoursLabel": "${content.engineHoursLabel!}",
         "exportCodesLabel": "${content.exportCodesLabel!}",
         "resultsLabel": "${content.resultsLabel!}",
         "troubleshootingUrl": "[@pageLink content.troubleshootingPage!/]",
         "noResults": "${content.noResults!}",
         "engineHoursNegativeError": "${content.engineHoursNegativeError!}",
         "engineHoursStartGreaterError": "${content.engineHoursStartGreaterError!}",
         "startDateGreaterError": "${content.startDateGreaterError!}",
         "loadMoreButton": "${content.loadMoreButton!}",
         "searchLabel": "${content.searchLabel!}",
         "descriptionSearchLabel": "${content.descriptionSearchLabel!}",
         "codeSearchLabel": "${content.codeSearchLabel!}",
         "clearFilters": "${content.clearFilters!}",
         "showFilters": "${content.showFilters!}",
         "hideFilters": "${content.hideFilters!}",
         "paginationLimit": "${content.paginationLimit!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/faultcodes.js" type="module"></script>
