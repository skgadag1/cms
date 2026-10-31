window.com_tbscg_doosan_magnolia_custom_resources_vaadin_JsResourcesComponent = function() {
  const connector = this;
  let currentScript = null;
  const COMPONENT_ID = 'custom-js-component';
  const SCRIPT_ID = 'dynamic-js-script';

  this.onStateChange = function() {
    const state = connector.getState();
    if (!state || !state.content) {
        console.warn(`[${COMPONENT_ID}] No content to inject`);
      return;
    }

    if (currentScript){
      console.warn(`[${COMPONENT_ID}] Script already injected`);
      return;
    }

    try {
      currentScript = document.createElement('script');
      currentScript.id = SCRIPT_ID;
      currentScript.type = 'text/javascript';
      currentScript.textContent = state.content;

      document.head.appendChild(currentScript);
    } catch (error) {
      console.error(`[${COMPONENT_ID}] Error injecting script:`, error);
    }
  };
};