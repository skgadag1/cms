[#include "/kags/templates/macros/common.ftl"]

<div id="warranty"
     data-config='{
         "title": "${content.title!}",
         [#if content.icon?has_content]"icon": "${content.icon!}",[/#if]
         [#if content.showBreadcrumb!?is_boolean && content.showBreadcrumb == true]"showBreadcrumb": "true",[/#if]
         "bannerUrl": "[@damLink content.banner!/]",
         "noResults": "${content.noResults!}",
         "statusHeader": "${content.statusHeader!}",
         "descriptionHeader": "${content.descriptionHeader!}",
         "typeHeader": "${content.typeHeader!}",
         "startDateHeader": "${content.startDateHeader!}",
         "endDateHeader": "${content.endDateHeader!}",
         "hoursHeader": "${content.hoursHeader!}",
         "unauthorizedError": "${content.unauthorizedError!}",
         "noWarrantyInclusions": "${content.noWarrantyInclusions!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/warranty.js" type="module"></script>
