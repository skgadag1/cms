[#include "/kags/templates/macros/common.ftl"]

<section class="position-relative">
    <div class="banner-main no-navigation"
         [#if content.banner?has_content]
         style="background-image: url('[@damLink content.banner/]')"
         [/#if]
    ></div>

    <div class="container pt-2">
        <div class="row">
            <div class="col-12 pt-4">
                [#assign icon = content.icon!]
                <h1 class="[#if icon != "null"]pb-5 pt-4 py-sm-5 mb-sm-4 pb-lg-2 pb-lg-4[#else]py-4 mt-4 mb-5 mb-lg-4[/#if] text-white d-flex align-items-center justify-content-center flex-column flex-sm-row">
                    [#if icon != "null"]
                        [#if icon == "loader" || icon == "service-analyzer" || icon == "ev"]
                        <bicon class="bobcaticon bobcaticon-${icon} text-primary mr-3"></bicon>
                        [#else]
                        <i class="fas fa-${icon} text-primary mr-3"></i>
                        [/#if]
                    [/#if]
                    ${content.title!}
                </h1>
            </div>
        </div>

        [#if content.showBreadcrumb!?is_boolean && content.showBreadcrumb == true]
        <div class="row">
            <div class="col-12 mt-md-1 mt-lg-4 pt-lg-1">
                <div id="breadcrumb"></div>
            </div>
        </div>
        <script src="${cfgfn.kags.staticServer}/assets/js/breadcrumb.js" type="module"></script>
        [#else]
        <div class="w-100 mt-2 mt-md-2 mt-lg-4 pt-lg-3"></div>
        [/#if]
    </div>
</section>