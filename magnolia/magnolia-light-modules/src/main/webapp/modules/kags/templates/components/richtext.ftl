[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
<div id="richtext"
     data-config='{
         "title": "${content.title!}"[#if content.richText?has_content],
         "text": "[@singleLineJsonString]${cmsfn.decode(content).richText!}[/@singleLineJsonString]"[/#if][#if content.halfWidth!?is_boolean],
         "halfWidth": "${content.halfWidth?c}"[/#if],
         "imageUrl": "[@damLink content.imageUrl!/]"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/richtext.js" type="module"></script>
[/#if]
