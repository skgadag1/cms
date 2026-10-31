[#include "/kags/templates/functions/utils.ftl"]
[#if isAllowedToViewContent(ctx, content)]
<section class="container mb-4">
    <div class="row">
        <div class="col-12">
            <p class="font-condensed">${content.introField!}</p>
        </div>
    </div>
</section>
[/#if]
