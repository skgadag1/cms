[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
<div id="video"
     data-config='{
         "title": "${content.title!}",
         "subtitle": "${content.subtitle!}"[#if content.halfWidth!?is_boolean],
         "halfWidth": "${content.halfWidth?c}"[/#if],
         "buttonLabel": "${content.buttonLabel!}",
         "videoUrl": "[@damLink content.videoUrl!/]",
         "videoImg": "[@damLink content.videoImg!/]",
         "videoError": "${content.videoError!}"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/video.js" type="module"></script>
[/#if]
