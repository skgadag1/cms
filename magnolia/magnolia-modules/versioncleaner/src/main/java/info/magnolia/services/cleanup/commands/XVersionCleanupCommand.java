/**
 * This file Copyright (c) 2017 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This program and the accompanying materials are made
 * available under the terms of the Magnolia Network Agreement
 * which accompanies this distribution, and is available at
 * http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.services.cleanup.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.JcrConstants;

import info.magnolia.cms.core.SystemProperty;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;

/**
 * TBD.
 */
@Slf4j
public class XVersionCleanupCommand extends MgnlCommand {

    // Versions older than MAX_AGE days are going to be removed
    private static final long MAX_AGE = 90; // IN DAYS

    // The numbers of versions we are going to keep even if they are older than MAX_AGE
    private static final int MIN_VERSIONS = 3;

    // All versions above MAX_VERSIONS will be deleted no matter their age
    private static final int MAX_VERSIONS = 5;

    // If DRYRUN is true the deletion is omitted. Only the statistics are created
    private static final boolean DRYRUN = false;

    // If ARCHIVE is true the deleted versions are going to be archived. They are exported as a jcr docview
    private static final boolean ARCHIVE = true;

    // The destination for the archived versions. The JCR structure will be replicated
    private static final String ARCHIVE_DEST = SystemProperty.getProperty(SystemProperty.MAGNOLIA_APP_ROOTDIR) + "/archive";

    private DateFormat format = SimpleDateFormat.getDateInstance();
    Date limit = new Date(System.currentTimeMillis() - (MAX_AGE * 24L * 60L * 60L * 1000L));

    private Session session;

    long versionHistoryCount;
    long versionCount;
    long deletedVersionCount;

    private final class VisitVersionHistories implements ItemVisitor {

        @Override
        public void visit(Node node) throws RepositoryException {
            if(node.isNodeType(JcrConstants.NT_VERSIONHISTORY)){
                versionHistoryCount++;
                VersionHistory history = (VersionHistory) node;
                VersionIterator versions = history.getAllVersions();
                TreeMap<Calendar, Version> sortedByDate = new TreeMap<Calendar, Version>();

                while(versions.hasNext()){
                    Version version = versions.nextVersion();
                    versionCount++;
                    // don't add the root version.
                    if(!version.getName().equals(JcrConstants.JCR_ROOTVERSION)){
                        sortedByDate.put(version.getCreated(), version);
                    }
                }

                int count=0;
                for (Entry<Calendar, Version> versionId : sortedByDate.descendingMap().entrySet()) {
                    if(count>=MIN_VERSIONS){
                        Calendar created = versionId.getKey();
                        Version version = versionId.getValue();
                        if(count > MAX_VERSIONS || created.getTime().before(limit)){
                            if(!DRYRUN){
                                if(ARCHIVE){
                                    File archiveDir = new File(ARCHIVE_DEST);
                                    String path = StringUtils.removeStart(version.getPath(), "/jcr:system/jcr:versionStorage/");
                                    File file = new File(archiveDir, path + ".xml");
                                    file.getParentFile().mkdirs();
                                    FileOutputStream out;
                                    try {
                                        out = new FileOutputStream(file);
                                        session.exportDocumentView(version.getPath(), out , false, false);
                                        IOUtils.closeQuietly(out);
                                    } catch (IOException e) {
                                        log.error("Can't write archive file", e);
                                    }
                                }
                                log("Deleting: " + version.getPath());
                                history.removeVersion(version.getName());

                                // save periodically but not too often
                                if(deletedVersionCount % 100 == 0){
                                    session.save();
                                }
                            }
                            deletedVersionCount++;
                        }
                    }
                    count++;
                }
            }
            else{
                NodeIterator children = node.getNodes();
                while(children.hasNext()){
                    Node child = children.nextNode();
                    child.accept(this);
                }
            }
            if(versionHistoryCount % 10000 ==0){
                stats();
            }
        }

        @Override
        public void visit(Property property) throws RepositoryException {
            // we don't visit properties
        }
    }

    @Override
    public boolean execute(Context ctx) throws Exception {
        log("Going to delete versions older than " + format.format(limit) + " or versions above the limit of " + MAX_VERSIONS + " versions, but will keep at least " + MIN_VERSIONS + " versions.");
        log("Start deleting old versions");
        session = ctx.getJCRSession("mgnlVersion");
        Node storage = session.getNode("/jcr:system/jcr:versionStorage");
        new VisitVersionHistories().visit(storage);
        log("Finished deleting old versions");
        stats();
        return false;
    }

    private void log(String msg) {
        if(DRYRUN){
            msg = "DRYRUN: " + msg;
        }
        log.info(msg);
    }

    private void stats() {
        log("Statistic: Versioned nodes: " + versionHistoryCount + ", versions: " + versionCount + ", deleted: " + deletedVersionCount);
    }

    public static void main(String[] args) throws Exception {
        new XVersionCleanupCommand().execute(MgnlContext.getInstance());
    }

}