[#include "/kags/templates/functions/utils.ftl"]

<!DOCTYPE html>
<html xml:lang="${cmsfn.language()}" lang="${cmsfn.language()}">
    <head>
        [@cms.page /]
        <meta charset="utf-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1"/>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>${content.title!}</title>
        [@cms.area name="headerScripts"/]
        <link rel="icon" type="image/x-icon" href="${cfgfn.kags.staticServer}/assets/favicon.ico"/>
        <link href="${cfgfn.kags.staticServer}/assets/css/chunk-common.css" rel="stylesheet">
        <script src="${cfgfn.kags.staticServer}/assets/js/chunk-vendors.js" type="module"></script>
        <script src="${cfgfn.kags.staticServer}/assets/js/links.js" type="text/javascript" defer></script>
    </head>
    <body [#if ctx.request.getSession().getAttribute("access_token")?exists]data-api-token="${ctx.request.getSession().getAttribute("access_token")}"[/#if]
		  [#if ctx.request.getSession().getAttribute("id_token")?exists]data-id-token="${ctx.request.getSession().getAttribute("id_token")}"[/#if]
          data-asset-server="${cfgfn.kags.staticServer}"
          data-api-servers='${cfgfn.kags.apiServersJSON}'
          [#if ctx.response.getHeader("x-amzn-trace-id")!?exists]data-trace-id='${ctx.response.getHeader("x-amzn-trace-id")}'[/#if]
          [#if cmsfn.editMode]class="magnolia-edit-mode" data-is-edit='${cmsfn.isEditMode()?string}'[/#if]

    >
    [@cms.area name="bodyBeginScripts"/]
    <noscript>
        <strong>We're sorry but bobcat_default doesn't work properly without JavaScript enabled. Please enable it to
            continue.</strong>
    </noscript>

    [#if !(content.hideHeader!?is_boolean) || content.hideHeader != true]
    [@cms.area name="header"/]
    [/#if] 

    [@cms.area name="notificationbararea"/]

    [#if isAllowedToViewContent(ctx, content)]
        [@cms.area name="main"/]
        [#if content.showMenu!?is_boolean && content.showMenu == true]
            <div class="mb-5 flex-grow-1 flex-shrink-0">
                <div class="container">
                    <div class="row">
                        <div class="pr-lg-0 px-0 px-sm-3 col-lg-3 col-12">
                            [@cms.area name="menu"/]
                        </div>
                        <div class="px-0 col-lg-9 col-12">
                            [@cms.area name="content"/]
                        </div>
                    </div>
                </div>
            </div>
        [/#if]
        [#if content.showColumnCentered!?is_boolean && content.showColumnCentered == true]
            <section class="container flex-shrink-0">
                <div class="row[#if cmsfn.editMode] flex-column[/#if]">
                    [@cms.area name="columnCentered"/]
                </div>
            </section>
        [/#if]
    [#else]
        <section class="container my-5">
            <div class="row">
                <div class="col-12 d-flex align-items-start my-5">
                    <div class="box bg-gray-400 w-100"><span class="d-inline-block small align-self-end text-gray-600 mr-2"><svg aria-hidden="true" focusable="false" data-prefix="fas" data-icon="lock-alt" role="img" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="svg-inline--fa fa-lock-alt fa-w-14"><path fill="currentColor" d="M400 224h-24v-72C376 68.2 307.8 0 224 0S72 68.2 72 152v72H48c-26.5 0-48 21.5-48 48v192c0 26.5 21.5 48 48 48h352c26.5 0 48-21.5 48-48V272c0-26.5-21.5-48-48-48zM264 392c0 22.1-17.9 40-40 40s-40-17.9-40-40v-48c0-22.1 17.9-40 40-40s40 17.9 40 40v48zm32-168H152v-72c0-39.7 32.3-72 72-72s72 32.3 72 72v72z" class=""></path></svg></span> You are not allowed to view this page</div>
                </div>
            </div>
        </section>
    [/#if]

    [#if !(content.hideFeedback!?is_boolean) || content.hideFeedback != true]
    [@cms.area name="feedback"/]
    [/#if]

    [#if !(content.hideFooter!?is_boolean) || content.hideFooter != true]
    [@cms.area name="footer"/]
    [/#if]

    </body>
</html>
