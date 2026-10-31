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

import info.magnolia.cms.core.version.VersionManager;
import info.magnolia.importexport.DataTransporter;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.voting.voters.AbstractBoolVoter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * TBD.
 */
@Deprecated
public class DuplicateVersionVoter extends AbstractBoolVoter<Version> {

    private static final Logger logger = LoggerFactory.getLogger(DuplicateVersionVoter.class);

    VersionManager versionManager;

    @Inject
    public DuplicateVersionVoter(VersionManager versionManager) {
        this.versionManager = versionManager;
    }

    @Override
    public boolean boolVote(Version version) {

        try {
            XMLReader initialReader = XMLReaderFactory.createXMLReader(SAXParser.class.getName());
            SkipPropertiesFilter filter = new SkipPropertiesFilter(initialReader);
            filter.setPropertyNames(new HashSet<String>(Arrays.asList("jcr:uuid")));

            Node vnode = version.getFrozenNode();
            ByteArrayOutputStream vbuf = new ByteArrayOutputStream();
            DataTransporter.parseAndFormat(vbuf, filter, RepositoryConstants.VERSION_STORE, vnode.getPath(), vnode.getSession(), true);
            logger.debug("current version: " + vbuf.toString());

            Version predecessor = version.getLinearPredecessor();
            Node pnode = predecessor.getFrozenNode();
            ByteArrayOutputStream pbuf = new ByteArrayOutputStream();
            DataTransporter.parseAndFormat(pbuf, filter, RepositoryConstants.VERSION_STORE, pnode.getPath(), pnode.getSession(), true);
            logger.debug("predecessor version: {}", pbuf.toString());

            if (vbuf.size() != pbuf.size()) {
                logger.debug("version and predecessor sizes differ, changes must have been made");
                return false;
            }

            // do bytewise comparison
            byte[] vcontent = vbuf.toByteArray();
            byte[] pcontent = pbuf.toByteArray();

            for (int i = 0; i < vcontent.length; ++i) {
                if (vcontent[i] != pcontent[i]) {
                    logger.debug("found difference at byte {}", i);
                    return false;
                }
            }

            return true;
        } catch (RepositoryException rx) {
            logger.info("Could not vote on {}, exception: {}", version, rx.getMessage(), rx);
        } catch (IOException iox) {
            logger.info("Could not vote on {}, exception: {}", version, iox.getMessage(), iox);
        } catch (SAXException sx) {
            logger.info("Could not vote on {}, exception: {}", version, sx.getMessage(), sx);
        } catch (ParserConfigurationException pcx) {
            logger.info("Could not determine duplicate version: {}", pcx.getMessage(), pcx);
        } catch (TransformerConfigurationException tcx) {
            logger.info("Could not determine duplicate version: {}", tcx.getMessage(), tcx);
        }

        return false;
    }

}
