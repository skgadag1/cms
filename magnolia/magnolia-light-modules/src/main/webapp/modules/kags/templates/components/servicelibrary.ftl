[#include "/kags/templates/macros/common.ftl"]
<div id="servicelibrary"
     data-config='{
         "bannerUrl": "[@damLink content.banner!/]",
         "componentTitle": "${content.componentTitle!}",
         "welcome": "${content.welcome!}",
         "searchLabel": "${content.searchLabel!}",
         "resultsLabel": "${content.resultsLabel!}",
         "moreThanResultsLabel": "${content.moreThanResultsLabel!}",
         "searchInputPlaceholder": "${content.searchInputPlaceholder!}",
         "refineSearch": "${content.refineSearch!}",
         "titleHeader": "${content.titleHeader!}",
         "descriptionHeader": "${content.descriptionHeader!}",
         "categoryHeader": "${content.categoryHeader!}",
         "languageHeader": "${content.languageHeader!}",
         "bulletin_filter": "${content.bulletin_filter!}",
         "language_filter": "${content.language_filter!}",
         "product_filter": "${content.product_filter!}",
         "manual_filter": "${content.manual_filter!}",
         "instruction_filter": "${content.instruction_filter!}"[#if content.defaultEnglishSelected!?is_boolean],
         "defaultEnglishSelected": "${content.defaultEnglishSelected?c}"[/#if],
         "downloadDetails": "${content.downloadDetails!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/servicelibrary.js" type="module"></script>
