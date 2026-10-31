[#include "/kags/templates/macros/common.ftl"]
<div id="header"
     data-config='{
       [#if ctx.user?exists]
         "user": {
           "name": "[@escapeQuotes ctx.user.name! /]",
           "givenName": "[@escapeQuotes ctx.user.getProperty('given_name')! /]",
           "familyName": "[@escapeQuotes ctx.user.getProperty('family_name')! /]",
           "fullName": "[@escapeQuotes ctx.user.getProperty('fullName')! /]",
           "id": "[@escapeQuotes ctx.user.getProperty('userid')! /]",
           "companyId": "[@escapeQuotes ctx.user.getProperty('companyId')! /]",
           "employeeNumber": "[@escapeQuotes ctx.user.getProperty('employeeNumber')! /]"
           [#if ctx.user.getProperty('expertLevel')!?has_content]
           ,"expertLevel": "[@escapeQuotes ctx.user.getProperty('expertLevel')! /]"
           [/#if]
           [#assign internal = ctx.user.getProperty('internal')!]
           [#if cmsfn.authorInstance]
             [#assign internal = "true"]
           [/#if]
           [#if ctx.user.getProperty('internal')!?has_content],"internal": ${internal}[/#if]
         },
       [/#if]
         "mainPage": "[@pageLink content.mainPage!/]"
     }'
></div>
<script src="${cfgfn.kags.staticServer}/assets/js/header.js" type="module"></script>
