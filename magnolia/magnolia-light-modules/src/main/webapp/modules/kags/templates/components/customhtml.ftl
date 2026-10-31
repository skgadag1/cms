<#if content.editHTML?has_content>
    <#assign htmlContent = cmsfn.decode(content).editHTML />

    <#assign bodyContentPattern = "(?is).*?<body[^>]*>(.*?)</body>.*" />
    <#if htmlContent?matches(bodyContentPattern)>
        <#assign htmlContent = htmlContent?replace(bodyContentPattern, "$1", "r") />
    </#if>

    <#assign htmlContent = htmlContent?trim />

    <div class="custom-html">
        ${htmlContent}
    </div>
</#if>
