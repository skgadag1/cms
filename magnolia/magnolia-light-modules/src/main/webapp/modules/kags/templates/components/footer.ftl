[#include "/kags/templates/macros/common.ftl"]
<div id="footer"
     data-config='{
         "contactUrl": "[@pageLink content.contactUrl!/]",
         "privacyPolicyUrl": "[@pageLink content.privacyPolicyUrl!/]",
         "termOfUseUrl": "[@pageLink content.termOfUseUrl!/]"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/footer.js" type="module"></script>