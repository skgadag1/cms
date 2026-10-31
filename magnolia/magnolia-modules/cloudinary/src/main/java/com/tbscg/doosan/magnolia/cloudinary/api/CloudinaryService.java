package com.tbscg.doosan.magnolia.cloudinary.api;

import com.cloudinary.Cloudinary;
import com.cloudinary.Search;
import com.cloudinary.api.ApiResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.net.UrlEscapers;
import com.tbscg.doosan.magnolia.cloudinary.CloudinaryConfig;
import com.tbscg.doosan.magnolia.cloudinary.contentview.CloudinaryAssetContainer;
import com.tbscg.doosan.magnolia.cloudinary.dam.CloudinaryAssetDetails;
import com.tbscg.doosan.magnolia.cloudinary.monitoring.CloudinaryApiMonitor;
import com.tbscg.doosan.magnolia.cloudinary.util.CloudinaryConstants;
import com.tbscg.doosan.magnolia.dxp.core.util.PathUtil;
import info.magnolia.dam.api.AssetQuery;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cloudinary.utils.ObjectUtils.asMap;

public class CloudinaryService {

    private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

    private final CloudinaryConfig config;
    @Inject
    private CloudinaryApiMonitor cloudinaryApiMonitor;
    private final CacheLoader<String, List<ApiObject>> assetCacheLoader = new CacheLoader<String, List<ApiObject>>() {
        public List<ApiObject> load(String key) {
            return getAssetsInFolderInternal(key);
        }
    };
    private final Cache<String, List<ApiObject>> assetCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(15, TimeUnit.MINUTES)
            .build(assetCacheLoader);

    @Inject
    public CloudinaryService(CloudinaryConfig config) {
        this.config = config;
    }

    private static String getExpression(AssetQuery q) {
        List<String> clauses = new ArrayList<>();
        clauses.add(getFolderClause(q));
        if (StringUtils.isNotBlank(q.getAdditionalQueryStatement())) {
            clauses.add(q.getAdditionalQueryStatement());
        }
        return clauses.stream()
                .filter(StringUtils::isNotBlank)
                .map(clause -> "(" + clause + ")")
                .collect(Collectors.joining(" AND "));
    }

    private static String getFolderClause(AssetQuery q) {
        String rootPath = null;
        if (q.getRootFolder() != null) {
            rootPath = q.getRootFolder().getPath();
        } else if (q.getRootPath() != null) {
            rootPath = q.getRootPath();
        }
        if (rootPath != null) {
            String folderClause = "folder=\"" + rootPath;
            if (q.includesDescendants()) {
                folderClause += "/*";
            }
            folderClause += "\"";
            return folderClause;
        }
        return null;
    }

    public Cloudinary getCloudinary() {
        ApiConfig api = config.getApi();
        if (api == null) {
            log.error("Cloudinary API incorrectly configured, check JCR config: /modules/cloudinary/config/api");
            throw new IllegalStateException("Cloudinary API incorrectly configured, check JCR config: /modules/cloudinary/config/api");
        }
        return new Cloudinary(asMap(
                "cloud_name", api.getCloudName(),
                "api_key", api.getApiKey(),
                "api_secret", api.getApiSecret()
        ));
    }

    public ApiObject getAsset(String path) {
        log.info("CLOUDINARY_REQUEST: getAsset(" + path + ")");
        try {
            long startTime = System.currentTimeMillis();
            ApiResponse resp = getCloudinary().search()
                    .expression(getExpressionForPath(path))
                    .withField("context")
                    .withField("metadata")
                    .maxResults(1)
                    .execute();
            List<ApiObject> assets = new ApiObject(resp).getList("resources");
            cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            return assets.isEmpty() ? null : assets.get(0);
        } catch (Exception e) {
            log.error("Failed to get asset: " + path, e);
            throw new RuntimeException(e);
        }
    }

    public boolean isDynamicFolders() {
        return Objects.equals(config.getFolderMode(), CloudinaryConstants.FOLDER_MODE_DYNAMIC);
    }

    private String getExpressionForPath(String path) {
        if (!isDynamicFolders()) {
            return String.format("public_id=\"%s\"", path);
        }

        final String assetFolder = PathUtil.getParentPath(path);
        final String filename = PathUtil.getFileName(path);
        return String.format("asset_folder=\"%s\" AND filename=\"%s\"", assetFolder, filename);
    }

    private ApiObject getAssetDetails(ApiObject asset) {
        log.info("CLOUDINARY_REQUEST: getAssetDetails(" + ApiObject.toJsonString(asset.getProperties()) + ")");
        try {
            long startTime = System.currentTimeMillis();
            String publicId = asset.getString(CloudinaryAssetDetails.PUBLIC_ID);
            ApiResponse response = getCloudinary().api().resource(publicId, asMap(
                    CloudinaryAssetDetails.RESOURCE_TYPE, asset.getString(CloudinaryAssetDetails.RESOURCE_TYPE),
                    CloudinaryAssetDetails.TYPE, asset.getString(CloudinaryAssetDetails.TYPE)
            ));
            cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            return new ApiObject(response);
        } catch (Exception e) {
            log.error("Failed to get asset details: " + ApiObject.toJsonString(asset.getProperties()), e);
            throw new RuntimeException(e);
        }
    }

    public List<ApiObject> getAssetsInFolder(String folderPath) {
        try {
            return this.assetCache.get(folderPath, () -> assetCacheLoader.load(folderPath));
        } catch (ExecutionException e) {
            log.error("Failed to get assets in folder: " + folderPath, e);
            throw new RuntimeException(e);
        }
    }

    private List<ApiObject> getAssetsInFolderInternal(String folderPath) {
        log.info("CLOUDINARY_REQUEST: getAssetsInFolder(" + folderPath + ")");
        try {
            long startTime = System.currentTimeMillis();
            AssetQuery query = new AssetQuery.Builder()
                    .fromPath(folderPath)
                    .excludeDescendants()
                    .withMaxResults(CloudinaryAssetContainer.DEFAULT_PAGE_LENGTH)
                    .build();
            List<ApiObject> result = new ArrayList<>();
            try {
                search(query).getResults().forEachRemaining(result::add);
            } catch (NoSuchElementException exception) {
                log.info("Iteration was wrongly finished");
            }
            cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            return result;
        } catch (Exception e) {
            log.error("Failed to get assets in folder: " + folderPath, e);
            throw new RuntimeException(e);
        }
    }

    public List<ApiObject> getSubFolders(String folderPath) {
        log.info("CLOUDINARY_REQUEST: getSubFolders(" + folderPath + ")");
        try {
            long startTime = System.currentTimeMillis();
            ApiResponse response = getCloudinary().api().subFolders(folderPath, asMap());
            cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            return new ApiObject(response).getList("folders");
        } catch (Exception e) {
            log.error("Failed to get subfolders: " + folderPath, e);
            throw new RuntimeException(e);
        }
    }

    public SearchResult search(AssetQuery q) {
        LazySearchIterator iterator = new LazySearchIterator(getCloudinary(), q);
        return new SearchResult(iterator, iterator.totalCount);
    }

    public int getSearchCount(AssetQuery q) {
        Search search = getCloudinary().search();
        String expression = getExpression(q);
        log.info("CLOUDINARY_REQUEST: getSearchCount(" + expression + ")");
        try {
            long startTime = System.currentTimeMillis();
            ApiResponse apiResp = search
                    .expression(expression)
                    .maxResults(0)
                    .execute();
            ApiObject response = new ApiObject(apiResp);
            cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            return response.getInt("total_count");
        } catch (Exception e) {
            log.error("Failed to get search count: " + expression, e);
            throw new RuntimeException(e);
        }
    }

    private class LazySearchIterator implements Iterator<ApiObject> {

        private final Cloudinary cloudinary;
        private final AssetQuery q;
        private String nextCursor = null;
        private Iterator<ApiObject> internal;
        private int totalCount = 0;

        public LazySearchIterator(Cloudinary cloudinary, AssetQuery q) {
            this.cloudinary = cloudinary;
            this.q = q;
            pullData();
        }

        private void pullData() {
            try {
                long startTime = System.currentTimeMillis();
                Search search = this.cloudinary.search();
                if (StringUtils.isNotEmpty(this.nextCursor)) {
                    search = search.nextCursor(this.nextCursor);
                }
                String expression = getExpression(this.q);

                log.info("CLOUDINARY_REQUEST: search(" + expression + "), maxResults:" + this.q.getMaxResults());
                ApiResponse apiResp = search
                        .expression(expression)
                        .sortBy("_score", "desc")
                        .withField("context")
                        .maxResults((int) this.q.getMaxResults())
                        .execute();

                ApiObject response = new ApiObject(apiResp);
                this.nextCursor = response.getString("next_cursor");
                this.internal = response.getList("resources").iterator();
                this.totalCount = response.getInt("total_count");
                cloudinaryApiMonitor.logExecutionTime(System.currentTimeMillis() - startTime);
            } catch (Exception e) {
                log.error("Failed to search: " + getExpression(this.q), e);
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return this.internal.hasNext() || StringUtils.isNotEmpty(this.nextCursor);
        }

        @Override
        public ApiObject next() {
            if (internal.hasNext()) {
                ApiObject next = internal.next();
                if (!this.internal.hasNext() && StringUtils.isNotEmpty(this.nextCursor)) {
                    pullData();
                }

                return next;
            }

            throw new NoSuchElementException("No more elements");
        }
    }
}
