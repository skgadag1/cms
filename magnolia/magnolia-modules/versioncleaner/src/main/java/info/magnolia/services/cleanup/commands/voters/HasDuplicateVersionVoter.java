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
package info.magnolia.services.cleanup.commands.voters;

import info.magnolia.importexport.DataTransporter;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.voting.voters.AbstractBoolVoter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * TBD.
 */
@Deprecated
public class HasDuplicateVersionVoter extends AbstractBoolVoter<VersionHistory> {

    private static final Logger logger = LoggerFactory.getLogger(HasDuplicateVersionVoter.class);

    @Override
    public boolean boolVote(VersionHistory history) {
        try {
            VersionIterator versions = history.getAllVersions();
            while (versions.hasNext()) {
                Version version = versions.nextVersion();

                ByteArrayOutputStream vbuf = new ByteArrayOutputStream();
                DataTransporter.parseAndFormat(vbuf, null, RepositoryConstants.VERSION_STORE, version.getPath(), version.getSession(), true);

                Version predecessor = version.getLinearPredecessor();
                ByteArrayOutputStream pbuf = new ByteArrayOutputStream();
                DataTransporter.parseAndFormat(pbuf, null, RepositoryConstants.VERSION_STORE, predecessor.getPath(), predecessor.getSession(), true);

                if (vbuf.size() != pbuf.size()) {
                    return false;
                }

                // do bytewise comparison
                byte[] vcontent = vbuf.toByteArray();
                byte[] pcontent = pbuf.toByteArray();

                for (int i = 0; i < vcontent.length; ++i) {
                    if (vcontent[i] != pcontent[i]) {
                        return false;
                    }
                }
            }

            return true;
        } catch (RepositoryException rx) {
            logger.info("Could not determine duplicate versions: {}", rx.getMessage(), rx);
            return false;
        } catch (SAXException sx) {
            logger.info("Could not determine duplicate versions: {}", sx.getMessage(), sx);
        } catch (IOException iox) {
            logger.info("Could not determine duplicate version: {}", iox.getMessage(), iox);
        } catch (ParserConfigurationException pcx) {
            logger.info("Could not determine duplicate version: {}", pcx.getMessage(), pcx);
        } catch (TransformerConfigurationException tcx){
            logger.info("Could not determine duplicate version: {}", tcx.getMessage(), tcx);
        }

        return false;
    }

}
