function handleCloudinaryMediaLibrary(cloudName, apiKey, username, scriptSource, config = { expression: "expression", resource_type: "image", type: "image", public_id: "id", methodName: "handleCloudinarySelection", errorMethodName: "handleCloudinaryError"}) {
    const { errorMethodName } = config;
    try {
        const newConfigHash = getConfigHash(config);

        if (window.mlInstance && window._mlConfigHash === newConfigHash) {
            window.mlInstance.show();
            return;
        }

        removeCloudinaryIframes();
        window._mlConfigHash = newConfigHash;

        if (typeof cloudinary === 'undefined') {
            const script = document.createElement('script')
            script.src = scriptSource
            script.onload = () => initMediaLibrary(cloudName, apiKey, username, config)
            script.onerror = function(event) {
                const errorMsg = `Failed to load Cloudinary script}`;
                console.error(errorMsg, event);
                if (window[errorMethodName]) {
                    window[errorMethodName](errorMsg + ' — ' + (event?.message || 'Script load error'));
                }
            };
            document.head.appendChild(script)
        } else {
            initMediaLibrary(cloudName, apiKey, username, config)
        }
    } catch (error) {
        console.error('Cloudinary initialization error:', error)
        if (window[errorMethodName]) {
            window[errorMethodName](error.message || String(error))
        }
    }
}

function initMediaLibrary(cloudName, apiKey, username, initialConfig) {
    const { expression, resource_type, type, public_id, methodName, errorMethodName } = initialConfig;
    try {
        const config = {
            cloud_name: cloudName,
            api_key: apiKey,
            username: username
        };

        if (isNonEmptyString(expression)) {
            config.search = { expression };
        }

        if (isNonEmptyString(resource_type) && isNonEmptyString(type) && isNonEmptyString(public_id)) {
            config.asset = { resource_type, type, public_id };
        }

        window.mlInstance = cloudinary.createMediaLibrary(config, {
            insertHandler: function(data) {
                data.assets.forEach((asset) => {
                    if (window[methodName]) {
                        window[methodName](JSON.stringify(asset))
                    }
                });
            },
            errorHandler: function(error) {
                console.error('Cloudinary Error:', error)
                if (window[errorMethodName]) {
                    window[errorMethodName](error.message || String(error))
                }
            }
        });

        window.mlInstance.show()
    } catch (error) {
        console.error('Cloudinary Media Library Error:', error)
        if (window[errorMethodName]) {
            window[errorMethodName](error.message || String(error))
        }
    }
}

function isNonEmptyString(value) {
    return typeof value === 'string' && value.trim() !== '';
}

function getConfigHash(config) {
    return JSON.stringify(config);
}

function removeCloudinaryIframes() {
    const xpath = "//div[iframe[contains(@src, 'cloudinary.com')]]";
    const result = document.evaluate(xpath, document, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
    for (let i = 0; i < result.snapshotLength; i++) {
        const el = result.snapshotItem(i);
        el?.remove();
    }
}
