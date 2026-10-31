[#macro commonHead]
    [@cms.page /]
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${(content.title!'')?trim}</title>
    <style>[v-cloak]{display:none;opacity:0;transition: .33s all ease-in;}</style>
    [#--<link href="${bobcatfn.config.staticServer}/assets/css/chunk-vendors.css" rel="stylesheet">--]
    <link href="${bobcatfn.config.staticServer}/assets/css/styles.css" rel="stylesheet">
    <script src="${bobcatfn.config.staticServer}/assets/js/chunk-vendors.js" type="text/javascript"></script>
    <script src="${bobcatfn.config.staticServer}/assets/js/styles.js" type="text/javascript"></script>
    <script src="${bobcatfn.config.staticServer}/assets/js/app.js" type="text/javascript"></script>
[/#macro]
