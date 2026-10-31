[#include "/kags/templates/macros/common.ftl"]

<div id="softwareregistration"
     data-config='{
         "title": "[@escapeQuotes content.title!''?json_string /]",
         "content": "[@singleLineJsonString]${cmsfn.decode(content).content!}[/@singleLineJsonString]",
         "footer": "[@singleLineJsonString]${cmsfn.decode(content).footer!}[/@singleLineJsonString]",
         "notice": "[@escapeQuotes content.notice!''?json_string /]",
         "software": "${content.choosenSoftware!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/softwareregistration.js" type="module"></script>
