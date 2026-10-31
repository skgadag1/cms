package com.tbscg.doosan.magnolia.cloudinary.api;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.tbscg.doosan.magnolia.dxp.core.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FolderService {

    private static final Logger log = LoggerFactory.getLogger(FolderService.class);

    private final CloudinaryService cloudinary;

    private static class Folder {

        private final String name;
        private final String path;

        public Folder(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public String getPath() {
            return path;
        }
    }

    private CacheLoader<String, List<Folder>> folderCacheLoader = new CacheLoader<String, List<Folder>>() {
        public List<Folder> load(String key) {
            return getSubfoldersInternal(key);
        }
    };

    /**
     * path -> folder names
     */
    private Cache<String, List<Folder>> folderCache = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(folderCacheLoader);

    @Inject
    public FolderService(CloudinaryService cloudinary) {
        this.cloudinary = cloudinary;
    }

    public static boolean isValidFolderPath(String path) {
        char[] invalidChars = new char[]{'#', '?', '&', '\\', '%', '<', '>'};
        for (char invalidChar : invalidChars) {
            for (char c : path.toCharArray()) {
                if (c == invalidChar) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean folderExists(String folderPath) {
        if (PathUtil.ROOT_FOLDER_PATH.equals(folderPath)) {
            return true;
        }
        if (!isValidFolderPath(folderPath)) {
            return false;
        }
        String parentPath = PathUtil.getParentPath(folderPath);
        List<Folder> subfolders = getSubfoldersCached(parentPath);
        return subfolders.stream().anyMatch(f -> folderPath.equals(f.getPath()));
    }

    public List<String> getSubfolders(String path) {
        return getSubfoldersCached(path).stream().map(Folder::getName).collect(Collectors.toList());
    }

    private List<Folder> getSubfoldersCached(String path) {
        try {
            return this.folderCache.get(path, () -> folderCacheLoader.load(path));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Folder> getSubfoldersInternal(String path) {
        List<ApiObject> subFolders = this.cloudinary.getSubFolders(path);
        return subFolders.stream()
                .map(f -> new Folder(f.getString("name"), f.getString("path")))
                .collect(Collectors.toList());
    }
}
