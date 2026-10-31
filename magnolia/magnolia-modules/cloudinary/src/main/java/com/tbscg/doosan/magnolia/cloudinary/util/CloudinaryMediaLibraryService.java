package com.tbscg.doosan.magnolia.cloudinary.util;

import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.api.ApiConfig;
import com.tbscg.doosan.magnolia.cloudinary.api.ApiObject;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAsset;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetDetails;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetProvider;
import com.vaadin.ui.UI;
import info.magnolia.dam.api.AssetProviderRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cloudinary.json.JSONObject;

import javax.inject.Inject;
import java.util.*;

import static com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryUtils.convertJsonObjectToMap;
import static com.tbscg.doosan.magnolia.dxp.core.util.MagnoliaUserUtils.getCurrentUserEmail;

@Slf4j
public class CloudinaryMediaLibraryService {
    private final CloudinaryAssetProvider cloudinaryAssetProvider;
    private final CloudinaryConfig cloudinaryConfig;

    private final static String ASSET_DISPLAY_NAME = "display_name";
    private final static String CLOUDINARY_ASSET_PROVIDER_ID = "cloudinary";

    @Inject
    public CloudinaryMediaLibraryService(CloudinaryConfig cloudinaryConfig,
                                         AssetProviderRegistry assetProviderRegistry) {
        this.cloudinaryConfig = cloudinaryConfig;
        this.cloudinaryAssetProvider = (CloudinaryAssetProvider) assetProviderRegistry.getProviderById(CLOUDINARY_ASSET_PROVIDER_ID);

    }

    public void executeCloudinaryMediaLibrary(String searchExpression, String resourceType, String type, String publicId, String methodName, String errorMethodName) {
        ApiConfig apiConfig = cloudinaryConfig.getApi();
        String scriptSource = cloudinaryConfig.getScriptSource();

        String username = getCurrentUserEmail();

        if (apiConfig == null) {
            throw new IllegalStateException("Cloudinary API configuration is missing");
        }

        scriptSource = StringUtils.isNotBlank(scriptSource) ? scriptSource :  StringUtils.EMPTY;
        searchExpression = StringUtils.isNotBlank(searchExpression) ? searchExpression :  StringUtils.EMPTY;
        resourceType = StringUtils.isNotBlank(resourceType) ? resourceType :  StringUtils.EMPTY;
        type = StringUtils.isNotBlank(type) ? type :  StringUtils.EMPTY;
        publicId = StringUtils.isNotBlank(publicId) ? publicId :  StringUtils.EMPTY;

        String jsContent = String.format(
                "handleCloudinaryMediaLibrary('%s', '%s', '%s', '%s', {" +
                        "expression: '%s'," +
                        "resource_type: '%s'," +
                        "type: '%s'," +
                        "public_id: '%s'," +
                        "methodName: '%s'," +
                        "errorMethodName: '%s'" +
                        "});",
                apiConfig.getCloudName(),
                apiConfig.getApiKey(),
                username,
                scriptSource,
                searchExpression,
                resourceType,
                type,
                publicId,
                methodName,
                errorMethodName
        );

        UI.getCurrent().getPage().getJavaScript().execute(jsContent);
    }

    public CloudinaryAsset createCloudinaryAsset(String assetDetailsJson) {
        try {
            JSONObject jsonObject = new JSONObject(assetDetailsJson);
            Map<String, Object> assetDetailsMap = convertJsonObjectToMap(jsonObject);
            assetDetailsMap.put(CloudinaryAssetDetails.FILENAME, assetDetailsMap.get(ASSET_DISPLAY_NAME));

            CloudinaryAssetDetails assetDetails = new CloudinaryAssetDetails(new ApiObject(assetDetailsMap));
            return new CloudinaryAsset(cloudinaryAssetProvider, assetDetails);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Cloudinary asset", e);
        }
    }
}