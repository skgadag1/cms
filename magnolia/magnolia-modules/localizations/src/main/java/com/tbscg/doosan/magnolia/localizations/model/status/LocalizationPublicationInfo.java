package com.tbscg.doosan.magnolia.localizations.model.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Calendar;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LocalizationPublicationInfo implements ItemVisitor {

    private final static String STATUS_PROPERTY = "status";
    private final static String LAST_PUBLICATION_PROPERTY = "lastPublication";
    private final static String MESSAGE_PROPERTY = "message";

    private LocalizationPackageStatus status;
    private Calendar lastPublication;
    private String message;

    @Override
    public void visit(Property property) throws RepositoryException {

    }

    @Override
    public void visit(Node node) throws RepositoryException {
        try {
            status = LocalizationPackageStatus.valueOf(node.getProperty(STATUS_PROPERTY).getString());
            message = node.getProperty(MESSAGE_PROPERTY).getString();
            lastPublication = node.getProperty(LAST_PUBLICATION_PROPERTY).getDate();
        } catch (Exception e) {
            status = LocalizationPackageStatus.NOT_PUBLISHED;
        }
    }

    public void save(Node node) throws RepositoryException {
        node.setProperty(STATUS_PROPERTY, status.name());
        node.setProperty(LAST_PUBLICATION_PROPERTY, lastPublication);
        node.setProperty(MESSAGE_PROPERTY, message);
        node.getSession().save();
    }
}
