package com.tbscg.doosan.magnolia.bobcat.commands;

import com.tbscg.doosan.magnolia.bobcat.BobcatModule;
import com.tbscg.doosan.magnolia.bobcat.commands.monitoring.BackupMonitor;
import com.tbscg.doosan.magnolia.dxp.core.util.TracingUtil;
import com.tbscg.doosan.magnolia.regions.service.RegionsService;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.DataTransporter;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.StorageClass;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Getter
@Setter
public class BackupCommand extends BaseRepositoryCommand {
    private static final String SEPARATOR = ",";
    private static final String PATH_SEPARATOR = "/";
    private static final String BASE_PATH = "bobcat";
    private final String bucketName;
    private final S3Client s3Client;
    private final RegionsService regionsService;
    private final String groupNamePath;
    private final String roleArnPath;

    private String[] workspacesToSave = new String[0];
    private String destination = "";
    private boolean backupRoot = false;
    private boolean backupOnlyLatest = false;
    private String date;

    @Inject
    public BackupCommand(final Provider<BobcatModule> dxpCoreModuleProvider, RegionsService regionsService) {
        this.regionsService = regionsService;
        bucketName = dxpCoreModuleProvider.get().getBackupCommandConfig().getS3Bucket();
        s3Client = S3Client.create();
        groupNamePath = dxpCoreModuleProvider.get().getBackupCommandConfig().getGroupNamePath();
        roleArnPath = dxpCoreModuleProvider.get().getBackupCommandConfig().getRoleArnPath();

    }

    @Override
    public boolean execute(Context context) throws Exception {
        date = getCurrentDate();
        List<String> regions = regionsService.getRegionsForSite(BASE_PATH);
        BackupMonitor productBackupMonitor = new BackupMonitor(groupNamePath, roleArnPath);
        productBackupMonitor.scheduleAlarm(workspacesToSave);

        for (String workspace : workspacesToSave) {
            saveWorkspaceInS3(workspace, regions);
        }
        productBackupMonitor.clearSchedule();

        return true;
    }


    private void saveWorkspaceInS3(String workspace, List<String> regions) {
        try {
            log.debug("Start executing backup of workspace {}", workspace);
            if (backupRoot) {
                saveRootInS3(workspace);
                return;
            }
            for (String region : regions) {
                saveRegionInS3(workspace, region);
            }
        } catch (Exception e) {
            log.error("Failed to save workspace {}", workspace);
            log.error("ERROR", e);
            throw e;
        }
    }

    private void saveRegionInS3(String workspace, String region) {
        try {
            String path = buildBasePath(workspace, region);
            log.debug("Start executing backup of region {}", region);
            Session session = MgnlContext.getJCRSession(workspace);
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            String regionInPath = region.isEmpty() ? "" : PATH_SEPARATOR + region;
            DataTransporter.executeExport(fos, false, false, session, PATH_SEPARATOR + BASE_PATH + regionInPath, workspace, DataTransporter.XML);
            saveOutputStreamInS3(fos, path);
        } catch (IOException | RepositoryException e) {
            log.error("Failed to save region {} in workspace {}", region, workspace);
            log.error("ERROR", e);
        }
    }

    private void saveRootInS3(String workspace) {
        saveRegionInS3(workspace, "");
    }

    private String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private String buildBasePath(String workspace, String region) {
        StringBuilder builder = new StringBuilder();
        builder.append(destination);
        if (!backupOnlyLatest) {
            builder.append(PATH_SEPARATOR).append(date);
        }
        if (!region.isEmpty()) {
            builder.append(PATH_SEPARATOR).append(region);
        }
        builder.append(PATH_SEPARATOR).append(workspace);
        return builder.toString();
    }


    private void saveOutputStreamInS3(ByteArrayOutputStream out, String keyName) {
        log.debug("Uploading file to path {}", keyName);
        InputStream inputStream = outputToInputStream(out);
        uploadInputStreamToS3(inputStream, keyName, out.size());
        log.debug("Successfully uploaded file {}", keyName);
    }

    private void uploadInputStreamToS3(InputStream inputStream, String keyName, long contentLength) {
        final PutObjectRequest request = PutObjectRequest.builder()
                .storageClass(StorageClass.STANDARD_IA)
                .bucket(bucketName)
                .key(keyName)
                .contentType("text/xml")
                .build();

        final RequestBody body = RequestBody.fromContentProvider(
                () -> inputStream,
                contentLength,
                "application/octet-stream"
        );

        TracingUtil.callWithTracing(traceID -> s3Client.putObject(request, body));
    }

    private InputStream outputToInputStream(ByteArrayOutputStream out) {
        return new ByteArrayInputStream(out.toByteArray());
    }
}
