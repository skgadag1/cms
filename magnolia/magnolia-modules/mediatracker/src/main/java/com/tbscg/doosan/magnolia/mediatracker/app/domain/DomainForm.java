package com.tbscg.doosan.magnolia.mediatracker.app.domain;

import com.tbscg.doosan.magnolia.mediatracker.data.model.VanityDomain;
import com.tbscg.doosan.magnolia.mediatracker.ui.CaptionedLabel;
import com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import static com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil.formatTimestamp;

/**
 * Class encapsulates domain's edit form
 */
@Getter
public class DomainForm extends VerticalLayout {

    private final VanityDomain domain;
    private final TextField domainName = new TextField("Domain");

    public DomainForm(String title, VanityDomain domain, boolean showRightPanel) {
        setWidth(100, Unit.PERCENTAGE);
        this.domain = domain;
        createForm(title, domain, showRightPanel);
    }

    /**
     * Returns domain object with edited name property
     * @return edited domain object
     */
    public VanityDomain getEditedDomain() {
        VanityDomain edited = new VanityDomain(this.domain);
        edited.setFullDomainName(this.domainName.getValue());
        return edited;
    }

    private void createForm(String title, VanityDomain domain, boolean showRightPanel) {
        addComponent(getHeader(title));
        FormLayout left = createLeftPanel(domain);
        HorizontalLayout layout = null;
        int height = 0;
        if(showRightPanel) {
            FormLayout right = createRightPanel();
            layout = new HorizontalLayout(left, right);
            height = 55 * Math.max(left.getComponentCount(), right.getComponentCount());
        } else {
            layout = new HorizontalLayout(left);
            height = 55 * left.getComponentCount();
        }
        layout.setWidth(100, Unit.PERCENTAGE);
        layout.setHeight(height, Unit.PIXELS);
        addComponent(layout);
    }

    private FormLayout createLeftPanel(VanityDomain domain) {
        FormLayout left = new FormLayout();
        left.setMargin(false);
        left.setWidth(100, Unit.PERCENTAGE);
        left.addComponent(new CaptionedLabel("Division", domain.getDivision()));
        left.addComponent(new CaptionedLabel("Code", domain.getCode()));
        this.domainName.setValue(StringUtils.defaultString(domain.getFullDomainName()));
        left.addComponent(this.domainName);
        return left;
    }

    private FormLayout createRightPanel() {
        FormLayout right = new FormLayout();
        right.setMargin(false);
        right.setWidth(100, Unit.PERCENTAGE);
        right.addComponent(new CaptionedLabel("Status", domain.getStatus()));
        right.addComponent(new CaptionedLabel("Created", formatTimestamp(domain.getCreationTime().toLocalDateTime(), domain.getCreatedBy())));
        right.addComponent(new CaptionedLabel("Modified", formatTimestamp(domain.getModifiedTime().toLocalDateTime(), domain.getModifiedBy())));
        right.addComponent(new CaptionedLabel("Start", DateFormatUtil.formatOffsetDateTime(domain.getStartDate())));
        right.addComponent(new CaptionedLabel("End", DateFormatUtil.formatOffsetDateTime(domain.getEndDate())));
        return right;
    }

    private Component getHeader(String title) {
        Label titleLabel = new Label(title);
        titleLabel.addStyleName("heading-2");
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(titleLabel);
        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return layout;
    }

}
