com_tbscg_doosan_magnolia_custom_resources_vaadin_CssResourcesComponent = function () {
  let customCss = document.createElement('style');
  customCss.id = "custom-css-style";
  let head = document.getElementsByTagName("head")[0];
  head.appendChild(customCss);

  this.onStateChange = function () {
    if (this.getState().content) {
      customCss.innerHTML = this.getState().content;
    }
  };
};