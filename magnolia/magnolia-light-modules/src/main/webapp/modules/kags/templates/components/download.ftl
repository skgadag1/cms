[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
    <section class="container mb-4">
        <div class="box">
            <div class="row">
                <div class="col-12 col-md-7">
                    <h2>${content.titleField!}</h2>
                    <p>${content.subtitleField!}</p>
                </div>

                [#if content.documentDownloads?has_content]
                <div class="col-12 col-md-5 text-md-right font-condensed">
                    [#list cmsfn.children(content.documentDownloads) as link]
                        [#if link.asset?has_content]
                        <a href="[@damLink link.asset!/]">
                            <i class="fas fa-${link.icon!} text-primary mr-2"></i>${link.label!}
                        </a><br/>
                        [/#if]
                    [/#list]
                </div>
                [/#if]

                [#if content.softwareDownloadfile?has_content]
                <div class="col-12">
                    <div class="box border">
                        <a href="#" class="font-condensed text-black" data-download-link="[@downloadLink content 'softwareDownloadfile'/]">
                            [#assign icon = cmsfn.contentById(content.softwareIconConcept, "concepts")!]
                            [#if icon?has_content]
                                [#assign res = icon['value' + icon.value]?matches("(https[^\"]*)")]
                            [/#if]
                            [#if res?has_content]
                                <img src="${res[0]!}" width="34" height="34" class="mr-2">
                            [#elseif content.softwareIcon?has_content]
                                <img src="${cfgfn.kags.staticServer}/assets/img/icons/${content.softwareIcon!}" height="34" class="mr-2">
                            [/#if]
                            ${content.softwareDownloadlabel!}
                        </a>
                    </div>
                </div>
                [/#if]
            </div>
        </div>
    </section>
[/#if]
