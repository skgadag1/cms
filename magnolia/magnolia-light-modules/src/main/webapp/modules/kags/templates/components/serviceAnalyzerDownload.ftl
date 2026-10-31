[#include "/kags/templates/macros/common.ftl"]

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
                        <img src="${cfgfn.kags.staticServer}/assets/img/icons/service-analyzer.svg" height="24" class="mr-2">
                        ${content.softwareDownloadlabel!}
                    </a>
                </div>
            </div>
            [/#if]
        </div>
    </div>
</section>
