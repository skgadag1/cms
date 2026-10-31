[#include "/kags/templates/macros/common.ftl"]

<section class="container mb-5">
    <div class="box">
        <div class="row">
            <div class="col-12">
                <h2>${content.titleField!}</h2>
            </div>

            [#if content.sectionImage?has_content]
            <div class="col-12 col-md-7">
                [#if content.descriptionField?has_content]${cmsfn.decode(content).descriptionField!}[/#if]
            </div>
            <div class="col-12 col-md-5 text-center">
                [#if content.imageTransformation?has_content]
                    [#assign rendition = content.imageTransformation]
                [#else]
                    [#assign rendition = "w_400,c_fit"]
                [/#if]
                <img src="[@damLink content.sectionImage rendition/]" class="img-fluid mt-4 mt-md-0 px-5" />
            </div>
            [#else]
            <div class="col-12">
                [#if content.descriptionField?has_content]${cmsfn.decode(content).descriptionField!}[/#if]
            </div>
            [/#if]
        </div>
    </div>
</section>
