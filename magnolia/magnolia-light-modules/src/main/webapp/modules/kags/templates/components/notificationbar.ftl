[#include "/kags/templates/macros/common.ftl"]
<div id="notificationbar"
     data-config='{
       [#if ctx.user?exists]
         "user": {
           "id": "[@escapeQuotes ctx.user.getProperty('userid')! /]"
         },
       [/#if]
        "closeButton": "Close"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/notificationbar.js" type="module"></script>
