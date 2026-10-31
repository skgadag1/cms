[#include "/kags/templates/macros/common.ftl"]

<div id="locationhistory"
     data-config='{
         "title": "${content.title!}"[#if content.icon?has_content],
         "icon": "${content.icon!}"[/#if][#if content.showBreadcrumb!?is_boolean && content.showBreadcrumb == true],
         "showBreadcrumb": "true"[/#if],
         "bannerUrl": "[@damLink content.banner!/]",
         "fromLabel": "${content.fromLabel!}",
         "toLabel": "${content.toLabel!}",
         "startDateLabel": "${content.startDateLabel!}",
         "endDateLabel": "${content.endDateLabel!}",
         "dateTimeLabel": "${content.dateTimeLabel!}",
         "locationLabel": "${content.locationLabel!}",
         "clearFilters": "${content.clearFilters!}",
         "googleMapsAPIKey": "${content.googleMapsAPIKey!}",
         "noLocationsFound": "${content.noLocationsFound!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/locationhistory.js" type="module"></script>
