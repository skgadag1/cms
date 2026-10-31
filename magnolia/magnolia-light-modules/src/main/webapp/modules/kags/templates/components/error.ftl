[#assign parametersMap = ctx.getParameters()!]
[#assign displayParamsList = content.displayParamsList!]

<div class="my-5 container">
    <div class="container pb-3">
        [#if parametersMap?size > 0 && displayParamsList?size > 0]
            [#list displayParamsList as param]
                [#if parametersMap[param]??]
                    <div class="pt-3 d-flex text-center">
                        <div>${param} : ${parametersMap[param]}</div>
                    </div>
                [/#if]
            [/#list]
        [/#if]
    </div>
</div>
