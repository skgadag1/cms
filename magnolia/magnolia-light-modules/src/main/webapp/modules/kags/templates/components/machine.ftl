[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]
[#assign rendition = "w_309,h_200,c_fit"]
<div id="machine"
     data-config='{
         "errorInputUrl": "[@pageLink content.troubleshootingPage!/]",
         "historyPageUrl": "[@pageLink content.historyPageUrl!/]",
         "bannerUrl": "[@damLink content.banner!/]",
         "serviceAnalyzerNationalUrl": "${content.serviceAnalyzerNationalUrl!}",
         "serviceAnalyzerBobcatUrl": "${content.serviceAnalyzerBobcatUrl!}",
         "engineAnalyzerUrl": "${content.engineAnalyzerUrl!}",
         "v2engineAnalyzerUrl": "${content.v2engineAnalyzerUrl!}",
         "orionBMSUrl": "${content.orionBMSUrl!}",
         "locationHistoryPageUrl": "[@pageLink (content.locationHistoryPageUrl!)/]",
         "faultCodesPageUrl": "[@pageLink (content.faultCodesPageUrl!)/]"[#if content.showFaultCodes!?is_boolean],
         "showFaultCodes": "${content.showFaultCodes?c}"[/#if],
         "fieldModsPageUrl": "[@pageLink (content.fieldModsPageUrl!)/]"[#if content.showFieldMods!?is_boolean],
         "showFieldMods": "${content.showFieldMods?c}"[/#if],
         "bulletinsPageUrl": "[@pageLink (content.bulletinsPageUrl!)/]"[#if content.showBulletins!?is_boolean],
         "showBulletins": "${content.showBulletins?c}"[/#if],
         "paginationLimit": "${content.paginationLimit!10}"
          [#if content.blocks?has_content]
          [#assign blocks = cmsfn.children(content.blocks)]
          [#list blocks as block]
          [#if block.abbreviation?has_content && block.dam_url?has_content]
          ,"${block.abbreviation!}": "[@damLink block.dam_url rendition!/]"
          [/#if]
          [/#list][/#if][#if content.udefinedImage?has_content]
          ,"undefinedImage": "[@damLink content.udefinedImage rendition!/]"[/#if]
          ,"softwareForKeyAccounts": "${isAllowedToViewSection(ctx, content, "softwareForKeyAccounts")?c}"
     }'
></div>

<script src="${cfgfn.kags.staticServer}/assets/js/machine.js" type="module"></script>
