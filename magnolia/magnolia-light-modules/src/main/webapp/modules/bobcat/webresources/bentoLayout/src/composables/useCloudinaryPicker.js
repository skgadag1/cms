import { ref } from "vue";

const CLOUDINARY_SCRIPT_URL =
  "https://media-library.cloudinary.com/global/all.js";

function loadCloudinaryScript() {
  return new Promise((resolve, reject) => {
    if (window.cloudinary && window.cloudinary.createMediaLibrary) {
      resolve();
      return;
    }

    const existing = document.querySelector(
      `script[src="${CLOUDINARY_SCRIPT_URL}"]`,
    );

    if (existing) {
      existing.addEventListener("load", resolve, { once: true });
      existing.addEventListener("error", reject, { once: true });
      return;
    }

    const script = document.createElement("script");
    script.src = CLOUDINARY_SCRIPT_URL;
    script.onload = resolve;
    script.onerror = () =>
      reject(new Error("Failed to load Cloudinary Media Library script"));
    document.head.appendChild(script);
  });
}

export function useCloudinaryPicker(targetImageRef, getCloudConfig) {
  const cloudinaryLoading = ref(false);

  function openCloudinaryPicker() {
    cloudinaryLoading.value = true;

    const config = (getCloudConfig && getCloudConfig()) || {};
    const cloudName = config.cloudName || "doosan-bobcat";

    loadCloudinaryScript()
      .then(() => {
        cloudinaryLoading.value = false;
        const mediaLibraryConfig = {
          cloud_name: cloudName,
          multiple: false,
          max_files: 1,
          search: { expression: "resource_type:image" },
        };
        if (config.apiKey) {
          mediaLibraryConfig.api_key = config.apiKey;
        }
        window.cloudinary.openMediaLibrary(mediaLibraryConfig, {
            insertHandler: (data) => {
              if (data && data.assets && data.assets.length > 0) {
                const asset = data.assets[0];
                const url = asset.secure_url || asset.url || "";
                targetImageRef.value = typeof url === "string" ? url.trim() : "";
              }
            },
          },
        );
      })
      .catch((err) => {
        cloudinaryLoading.value = false;
        console.error("Cloudinary widget load error:", err);
      });
  }

  return {
    cloudinaryLoading,
    openCloudinaryPicker,
  };
}
