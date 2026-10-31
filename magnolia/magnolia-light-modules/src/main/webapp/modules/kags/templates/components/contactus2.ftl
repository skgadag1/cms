[#include "/kags/templates/macros/common.ftl"]
[#include "/kags/templates/functions/utils.ftl"]

[#if isAllowedToViewContent(ctx, content)]
<div class="mb-5 container flex-grow-1 flex-shrink-0">
  <div class="row">
    <div class="col-12">
      <h2>${content.headline!}</h2>
      <div class="font-condensed">${cmsfn.decode(content).subtitle!}</div>
    </div>
  </div>

  [#if content.blocks?has_content]
  [#assign blocks = cmsfn.children(content.blocks)]
  [#assign blockWidth = 12 / (blocks?size)]

  <div class="row align-content-stretch">
    [#list blocks as block]
    <div class="col-md-${blockWidth?ceiling} col-12 mb-3">
        <div class="box mb-3 h-100 d-flex flex-column">
            <div class="flex-grow-1">
                [#if block.title?has_content]
                <h2>${block.title}</h2>
                [/#if]

                [#if block.body?has_content]
                ${cmsfn.decode(block).body}
                [/#if]
            </div>

            <div>
                [#if block.phoneNumber?has_content]
                <a href="tel:${block.phoneNumber}" class="font-condensed text-black mb-3 d-block">
                  <i class="fas fa-phone-alt mr-2"></i>[#if block.phoneLabel?has_content]${block.phoneLabel}[#else]${block.phoneNumber}[/#if]
                </a>
                [/#if]

                [#if block.emailAddress?has_content]
                <a href="mailto:${block.emailAddress}" class="font-condensed text-black mb-3 d-block">
                  <i class="fas fa-paper-plane mr-2"></i>[#if block.emailLabel?has_content]${block.emailLabel}[#else]${block.emailAddress}[/#if]
                </a>
                [/#if]

                [#if block.hours?has_content]
                <p class="font-condensed mb-3">
                  <i class="fas fa-clock mr-2"></i>${block.hours}
                </p>
                [/#if]

                [#if block.chatLink?has_content]
                <a rel="noopener" target="_blank" href="${block.chatLink}" class="btn btn-primary mb-3">
                  <i class="fas fa-comments mr-2"></i>${block.chatLabel!}
                </a>
                [/#if]
            </div>
        </div>
    </div>
    [/#list]
  </div>
  [/#if]

</div>
[/#if]
