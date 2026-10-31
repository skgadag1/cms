/**
 * This file Copyright (c) 2021 Magnolia International
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

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.commands.impl.BaseRepositoryCommand;
import info.magnolia.context.Context;
import info.magnolia.services.cleanup.commands.voters.EligibleVersionVoter;
import info.magnolia.services.cleanup.commands.voters.UnneededVersionVoter;
import info.magnolia.voting.voters.VoterSet;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import java.util.Arrays;

/**
 * TBD.
 */
@Slf4j
public class NodeVersionCleanupCommand extends BaseRepositoryCommand {

    protected final VersionManager versionManager;
    protected VoterSet<Version> pruneVersion = new VoterSet<>();
    protected boolean recursive = true;
    protected boolean noop = false;
    private int maxVersionIndex = 2;

    @Inject
    public NodeVersionCleanupCommand(VersionManager versionManager) {
        super();
        this.versionManager = versionManager;
        initPruneVersionVoter();
    }

    @Override
    public boolean execute(Context context) throws RepositoryException {
        Arrays.stream(pruneVersion.getVoters()).forEach(voter -> {
            if (voter instanceof UnneededVersionVoter) {
                UnneededVersionVoter unneededVersionVoter = (UnneededVersionVoter) voter;
                unneededVersionVoter.setVersionsThreshold(maxVersionIndex);
            }
        });
        boolean result = true;

        if (getRepository() == null || getRepository().isEmpty()) {
            log.error("Repository is not specified, cannot clean versions!");
            result = false;
        } else {
            try {
                NodeVersionVisitor visitor = new NodeVersionVisitor(versionManager, noop, recursive, pruneVersion);

                Node node = getJCRNode(context);

                log.debug("Launching node visitor {} to clean up versions", visitor);
                visitor.visit(node);
                log.debug("Cleaned up versions on {}:{}", getRepository(), node.getPath());
            } catch (RepositoryException rx) {
                log.error("Exception when cleaning up versions: {}", rx.getMessage(), rx);
                result = false;
            }
        }

        return result;
    }

    private void initPruneVersionVoter() {
        pruneVersion.setName("pruneVersion");
        pruneVersion.setOp("and");
        EligibleVersionVoter eligibleVersionVoter = new EligibleVersionVoter();
        eligibleVersionVoter.setName("eligibleVersionVoter");
        pruneVersion.addVoter(eligibleVersionVoter);
        UnneededVersionVoter unneededVersionVoter = new UnneededVersionVoter();
        unneededVersionVoter.setName("unneededVersionVoter");
        unneededVersionVoter.setVersionsThreshold(maxVersionIndex);
        pruneVersion.addVoter(unneededVersionVoter);
    }

    public VoterSet<Version> getPruneVersion() {
        return pruneVersion;
    }

    public void setPruneVersions(VoterSet<Version> pruneVersion) {
        this.pruneVersion = pruneVersion;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

    public boolean isNoop() {
        return noop;
    }

    public void setNoop(boolean noop) {
        this.noop = noop;
    }

    public int getMaxVersionIndex() {
        return maxVersionIndex;
    }

    public void setMaxVersionIndex(int maxVersionIndex) {
        this.maxVersionIndex = maxVersionIndex;
    }
}
