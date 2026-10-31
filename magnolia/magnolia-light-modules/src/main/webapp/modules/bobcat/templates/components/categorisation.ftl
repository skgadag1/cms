[#include "/bobcat/templates/macros/common.ftl"]

[#assign rootPage = cmsfn.asJCRNode(cmsfn.root(content, "mgnl:page"))!]
[#assign bigCategoryPages = catfn.getContentByCategory(rootPage, "big")!]
[#assign mediumCategoryPages = catfn.getContentByCategory(rootPage, "medium")!]
[#assign smallCategoryPages = catfn.getContentByCategory(rootPage, "small")!]

Big pages:</br>
<ul>
    [#list bigCategoryPages as page]
        [#assign title = cmsfn.asContentMap(page).title!page.getName()]
        <li>${title}</li>
    [/#list]
</ul>
Medium pages:</br>
<ul>
    [#list mediumCategoryPages as page]
        [#assign title = cmsfn.asContentMap(page).title!page.getName()]
        <li>${title}</li>
    [/#list]
</ul>
Small pages:</br>
<ul>
    [#list smallCategoryPages as page]
        [#assign title = cmsfn.asContentMap(page).title!page.getName()]
        <li>${title}</li>
    [/#list]
</ul>

All pages with the tag <i>promo</i>:<br/>
[#assign taggedPages=tagfn.getContentByTags("website", "/bobcat", ["promo"])! /]
[#if taggedPages?has_content]
    [#list taggedPages as page]
        <li>${page}</li>
    [/#list]
[/#if]
</br>
All pages with the tag <i>demo</i>:<br/>
[#assign taggedPages=tagfn.getContentByTags("website", "/bobcat", ["demo"])! /]
[#if taggedPages?has_content]
    [#list taggedPages as page]
        <li>${page}</li>
    [/#list]
[/#if]

