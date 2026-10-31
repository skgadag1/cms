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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.Set;


/**
 * TBD.
 */
@Deprecated
public class SkipPropertiesFilter extends XMLFilterImpl {

    protected Logger log = LoggerFactory.getLogger(SkipPropertiesFilter.class);

    protected String lastNodeName;

    protected boolean skipProperty;

    protected Set<String> propertyNames;

    private List<BufferedElement> elementBuffer = new ArrayList<BufferedElement>();

    public SkipPropertiesFilter() {
        super();
    }

    /**
     * Instantiates a new filter.
     * @param parent wrapped XMLReader
     */
    public SkipPropertiesFilter(XMLReader parent) {
        super(parent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (skipProperty) {
            if ("sv:property".equals(qName)) {
                skipProperty = false;
                elementBuffer.clear();
            }
            return;
        }

        super.endElement(uri, localName, qName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        String svname = atts.getValue("sv:name");
        if ("sv:node".equals(qName)) {
            lastNodeName = svname;
        }
        else if ("sv:property".equals(qName) && (filterProperty(svname))) {
            skipProperty = true;
        }

        if (skipProperty) {
            return;
        }

        super.startElement(uri, localName, qName, atts);

    }

    protected boolean filterProperty(String propertyName) {
        if (propertyNames == null) return false;

        return propertyNames.contains(propertyName);
    }

    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    public void setPropertyNames(Set<String> propertyNames) {
        this.propertyNames = propertyNames;
    }

    /**
     * Temporary element storage node.
     */
    public static class BufferedElement {

        private String uri;

        private String localName;

        private String qName;

        private Attributes atts;

        public BufferedElement(String uri, String localName, String qName, Attributes atts) {
            this.atts = atts;
            this.localName = localName;
            this.qName = qName;
            this.uri = uri;
        }

        /**
         * Returns the uri.
         * @return the uri
         */
        public String getUri() {
            return uri;
        }

        /**
         * Sets the uri.
         * @param uri the uri to set
         */
        public void setUri(String uri) {
            this.uri = uri;
        }

        /**
         * Returns the localName.
         * @return the localName
         */
        public String getLocalName() {
            return localName;
        }

        /**
         * Sets the localName.
         * @param localName the localName to set
         */
        public void setLocalName(String localName) {
            this.localName = localName;
        }

        /**
         * Returns the qName.
         * @return the qName
         */
        public String getQName() {
            return qName;
        }

        /**
         * Sets the qName.
         * @param name the qName to set
         */
        public void setQName(String name) {
            qName = name;
        }

        /**
         * Returns the atts.
         * @return the atts
         */
        public Attributes getAtts() {
            return atts;
        }

        /**
         * Sets the atts.
         * @param atts the atts to set
         */
        public void setAtts(Attributes atts) {
            this.atts = atts;
        }
    }
}
