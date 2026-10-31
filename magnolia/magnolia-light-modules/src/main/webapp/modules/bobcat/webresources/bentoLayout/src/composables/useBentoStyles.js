import gridstackCss from "gridstack/dist/gridstack.min.css?inline";
import gridstackExtraCss from "gridstack/dist/gridstack-extra.min.css?inline";

const COMPONENT_CSS = ``;

export function injectBentoStyles() {
  if (document.getElementById("gs-bento-css")) return;
  const style = document.createElement("style");
  style.id = "gs-bento-css";
  style.textContent = gridstackCss + gridstackExtraCss + COMPONENT_CSS;
  document.head.appendChild(style);
}
