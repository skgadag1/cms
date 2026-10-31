[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
    <section class="container mb-5">
        <div class="box">
            <div class="row">
                <div class="col-12">
                    <h2>${content.titleField!}</h2>
                </div>
                <div class="col-12 [#if content.sectionImage?has_content]col-md-7[/#if]">
                    [#if content.descriptionField?has_content]${cmsfn.decode(content).descriptionField!}[/#if]
                    [#if content.ctaLink?has_content]
                        <div class="text-center">
                            <a rel="noopener" target="_blank" href="${content.ctaLink}" class="btn btn-primary my-3">
                                [#if content.ctaLabel?has_content]
                                    ${content.ctaLabel!}
                                [#else]
                                    ${content.ctaLink}
                                [/#if]
                            </a>
                        </div>
                    [/#if]
                </div>
                [#if content.sectionImage?has_content]
                    <div class="col-12 col-md-5 text-center">
                        [#if content.imageTransformation?has_content]
                            [#assign rendition = content.imageTransformation]
                        [#else]
                            [#assign rendition = "w_400,c_fit"]
                        [/#if]
                        <img src="[@damLink content.sectionImage rendition/]" class="img-fluid mt-4 mt-md-0 px-5" />
                    </div>
                [/#if]
            </div>
        </div>
    </section>
[/#if]