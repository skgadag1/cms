package com.tbscg.doosan.magnolia.mediatracker.app.url;

import com.tbscg.doosan.magnolia.mediatracker.data.model.*;
import com.tbscg.doosan.magnolia.mediatracker.error.HtmlFormattedMessageException;
import com.tbscg.doosan.magnolia.mediatracker.ui.CaptionedLabel;
import com.vaadin.ui.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil.*;
import static com.tbscg.doosan.magnolia.mediatracker.util.DateFormatUtil.getLocalDateFromOffsetDateTime;

/**
 * Class encapsulates url's edit form
 */
@Slf4j
@Getter
public class UrlForm extends VerticalLayout {

    private final VanityUrl url;
    private final List<VanityDomain> domains;
    private final TextField path = new TextField("Path");
    private final TextField redirectLocation = new TextField("Redirect");
    private final TextField redirectLocationMobile = new TextField("Redirect Mobile");
    private final TextField redirectLocationDesktop = new TextField("Redirect Desktop");
    private final TextField redirectLocationTablet = new TextField("Redirect Tablet");
    private final TextField redirectLocationOther = new TextField("Redirect Other");
    private final Button redirectLocationValidationBtn = new Button("Validate URL");
    private final Button redirectLocationMobileValidationBtn = new Button("Validate URL");
    private final Button redirectLocationDesktopValidationBtn = new Button("Validate URL");
    private final Button redirectLocationTabletValidationBtn = new Button("Validate URL");
    private final Button redirectLocationOtherValidationBtn = new Button("Validate URL");
    private final TextField placement = new TextField("Placement");
    private final TextField tracking = new TextField("Tracking Parameter");
    private final ComboBox<Region> regionComboBox = new ComboBox<>("Region", Arrays.asList(Region.values()));
    private final ComboBox<VanityDomain> domainComboBox = new ComboBox<>("Domain");
    private final ComboBox<RedirectType> redirectTypeComboBox = new ComboBox<>("Type");
    private final Label redirectTypeComboBoxLabel = new Label("Leave as default value (PERMANENT_301) unless specifically instructed to change.");
    private final CheckBox deviceDependentCheckBox = new CheckBox("Is Device Dependent Redirect");
    private final DateField startDate = new DateField("Start");
    private final DateField endDate = new DateField("End");

    private final List<Component> deviceIndependentComponents = Arrays.asList(
            this.redirectLocation,
            this.redirectLocationValidationBtn
    );

    private final List<Component> deviceDependentComponents = Arrays.asList(
            this.redirectLocationMobile,
            this.redirectLocationDesktop,
            this.redirectLocationTablet,
            this.redirectLocationOther,
            this.redirectLocationMobileValidationBtn,
            this.redirectLocationDesktopValidationBtn,
            this.redirectLocationTabletValidationBtn,
            this.redirectLocationOtherValidationBtn
    );

    public UrlForm(String title, VanityUrl url, List<VanityDomain> domains, boolean showRightPanel) {
        if (domains.isEmpty()) {
            throw new HtmlFormattedMessageException("<em>\n" +
                    "Could not find any domains in MediaTracker.\n" +
                    "er. At least one domain must be present.\n" +
                    "</em>");
        }

        this.url = url;
        this.domains = domains;
        addComponent(getHeader(title));
        addComponent(getForm(url, showRightPanel));
        setWidth(100, Unit.PERCENTAGE);
    }

    public VanityUrl getEditedUrl() {
        VanityUrl edited = new VanityUrl(this.url);
        edited.setRegion(this.regionComboBox.getValue());
        edited.setVanityDomain(this.domainComboBox.getValue());
        edited.setPath(this.path.getValue());
        edited.setDeviceDependentRedirect(this.deviceDependentCheckBox.getValue());
        edited.setRedirectLocation(this.redirectLocation.getValue());
        edited.setRedirectLocationMobile(this.redirectLocationMobile.getValue());
        edited.setRedirectLocationDesktop(this.redirectLocationDesktop.getValue());
        edited.setRedirectLocationTablet(this.redirectLocationTablet.getValue());
        edited.setRedirectLocationOther(this.redirectLocationOther.getValue());
        edited.setRedirectType(this.redirectTypeComboBox.getValue());
        edited.setPlacement(this.placement.getValue());
        edited.setTrackingParameter(this.tracking.getValue());
        edited.setStartDate(getOffsetDateTimeFromLocalDate(this.startDate.getValue()));
        edited.setEndDate(getOffsetDateTimeFromLocalDate(this.endDate.getValue()));
        if(edited.getStatus() == null) {
            edited.setStatus(Status.DRAFT);
        }
        return edited;
    }

    private HorizontalLayout getForm(VanityUrl url, boolean showRightPanel) {
        HorizontalLayout layout;
        FormLayout left = buildLeftPart(url);
        if(showRightPanel) {
            FormLayout right = buildRightPart(url);
            layout = new HorizontalLayout(left, right);
        } else {
            layout = new HorizontalLayout(left);
        }
        layout.setWidth(100, Unit.PERCENTAGE);
        return layout;
    }

    private FormLayout buildRightPart(VanityUrl url) {
        FormLayout right = new FormLayout();
        right.setMargin(false);
        right.setWidth(100, Unit.PERCENTAGE);
        String createdTimestamp = formatTimestamp(url.getCreationTime().toLocalDateTime(), url.getCreatedBy());
        String modifiedTimestamp = formatTimestamp(url.getModifiedTime().toLocalDateTime(), url.getModifiedBy());
        right.addComponent(new CaptionedLabel("Status", url.getStatus()));
        right.addComponent(new CaptionedLabel("Created", createdTimestamp));
        right.addComponent(new CaptionedLabel("Modified", modifiedTimestamp));

        LocalDate currentDate = getLocalDateFromOffsetDateTime(OffsetDateTime.now());
        initDateField(startDate, url.getStartDate(), url.getStatus(), currentDate);
        initDateField(endDate, url.getEndDate(), url.getStatus(), currentDate);
        right.addComponent(startDate);
        right.addComponent(endDate);

        return right;
    }

    private void initDateField(DateField dateField, OffsetDateTime date, Status status, LocalDate currentDate) {
        dateField.setValue(getLocalDateFromOffsetDateTime(date));
        dateField.setDateFormat(DATE_FORMAT);
        dateField.setRangeStart(currentDate);
        dateField.setReadOnly(Status.ARCHIVED == status);
    }

    private FormLayout buildLeftPart(VanityUrl url) {
        FormLayout left = new FormLayout();
        left.setMargin(false);
        left.setWidth(100, Unit.PERCENTAGE);

        addDivisionLabel(url, left);
        addCodeLabel(url, left);
        addRegionComboBox(url, left);
        addDomainComboBox(url, left);
        addPathTextField(url, left);
        addDeviceDependentCheckBox(url, left);
        addRedirectLocationFields(url, left);
        addRedirectTypeComboBox(url, left);
        addPlacementTextField(url, left);
        addTrackingTextField(url, left);

        return left;
    }

    private void addCodeLabel(VanityUrl url, FormLayout left) {
        if (StringUtils.isNotEmpty(url.getCode())) {
            left.addComponent(new CaptionedLabel("Code", url.getCode()));
        }
    }

    private void addDivisionLabel(VanityUrl url, FormLayout left) {
        left.addComponent(new CaptionedLabel("Division", url.getDivision()));
    }

    private void addRegionComboBox(VanityUrl url, FormLayout left) {
        this.regionComboBox.setValue(url.getRegion());
        this.regionComboBox.setEmptySelectionAllowed(true);
        left.addComponent(this.regionComboBox);
    }

    private void addDomainComboBox(VanityUrl url, FormLayout left) {
        this.domainComboBox.setEmptySelectionAllowed(false);
        this.domainComboBox.setItemCaptionGenerator(e -> e.getFullDomainName());
        this.domainComboBox.setItems(domains.stream().filter(d -> !d.getStatus().equals(Status.ARCHIVED)).collect(Collectors.toList()));
        VanityDomain selectedDomain = getSelectedDomain(url);
        if (selectedDomain != null) {
            this.domainComboBox.setSelectedItem(getSelectedDomain(url));
        } else {
            log.error("Domain ({}) assigned to URL {} doesn't exist", url.getVanityDomain().getCode(), url.getCode());
        }
        left.addComponent(this.domainComboBox);
    }

    private void addPathTextField(VanityUrl url, FormLayout left) {
        this.path.setValue(StringUtils.defaultString(url.getPath()));
        left.addComponent(this.path);
    }

    private void addDeviceDependentCheckBox(VanityUrl url, FormLayout left) {
        this.deviceDependentCheckBox.setValue(url.isDeviceDependentRedirect());
        this.deviceDependentCheckBox.addValueChangeListener(event -> applyVisibilityRules(event.getValue()));
        left.addComponent(this.deviceDependentCheckBox);
    }

    private void addTrackingTextField(VanityUrl url, FormLayout left) {
        this.tracking.setValue(StringUtils.defaultString(url.getTrackingParameter()));
        left.addComponent(this.tracking);
    }

    private void addPlacementTextField(VanityUrl url, FormLayout left) {
        this.placement.setValue(StringUtils.defaultString(url.getPlacement()));
        left.addComponent(this.placement);
    }

    private void addRedirectTypeComboBox(VanityUrl url, FormLayout left) {
        this.redirectTypeComboBox.setValue(url.getRedirectType());
        this.redirectTypeComboBox.setItems(RedirectType.values());
        this.redirectTypeComboBox.setEmptySelectionAllowed(false);
        left.addComponent(this.redirectTypeComboBox);
        left.addComponent(this.redirectTypeComboBoxLabel);
    }

    private void addRedirectLocationFields(VanityUrl url, FormLayout left) {
        this.redirectLocation.setValue(StringUtils.defaultString(url.getRedirectLocation()));
        this.redirectLocationValidationBtn.addClickListener(click -> {
            getUI().getPage().open(buildLink(this.redirectLocation.getValue()), "_blank");
        });
        left.addComponent(this.redirectLocation);
        left.addComponent(this.redirectLocationValidationBtn);

        this.redirectLocationMobile.setValue(StringUtils.defaultString(url.getRedirectLocationMobile()));
        this.redirectLocationMobileValidationBtn.addClickListener(click -> {
            getUI().getPage().open(buildLink(this.redirectLocationMobile.getValue()), "_blank");
        });
        left.addComponent(this.redirectLocationMobile);
        left.addComponent(this.redirectLocationMobileValidationBtn);

        this.redirectLocationDesktop.setValue(StringUtils.defaultString(url.getRedirectLocationDesktop()));
        this.redirectLocationDesktopValidationBtn.addClickListener(click -> {
            getUI().getPage().open(buildLink(this.redirectLocationDesktop.getValue()), "_blank");
        });
        left.addComponent(this.redirectLocationDesktop);
        left.addComponent(this.redirectLocationDesktopValidationBtn);

        this.redirectLocationTablet.setValue(StringUtils.defaultString(url.getRedirectLocationTablet()));
        this.redirectLocationTabletValidationBtn.addClickListener(click -> {
            getUI().getPage().open(buildLink(this.redirectLocationTablet.getValue()), "_blank");
        });
        left.addComponent(this.redirectLocationTablet);
        left.addComponent(this.redirectLocationTabletValidationBtn);

        this.redirectLocationOther.setValue(StringUtils.defaultString(url.getRedirectLocationOther()));
        this.redirectLocationOtherValidationBtn.addClickListener(click -> {
            getUI().getPage().open(buildLink(this.redirectLocationOther.getValue()), "_blank");
        });
        left.addComponent(this.redirectLocationOther);
        left.addComponent(this.redirectLocationOtherValidationBtn);

        applyVisibilityRules(url.isDeviceDependentRedirect());
    }

    private String buildLink(String value) {
        if(StringUtils.isNotBlank(value)) {
            if(!value.startsWith("http://") && !value.startsWith("https://")) {
                return "http://" + value;
            }
        }
        return value;
    }

    private VanityDomain getSelectedDomain(VanityUrl url) {
        if(StringUtils.isNotBlank(url.getCode())) {
            return url.scanForDomain(domains);
        } else {
            return domains.get(0);
        }
    }

    private void applyVisibilityRules(boolean deviceDependent) {
        this.deviceDependentComponents.forEach(c -> c.setVisible(deviceDependent));
        this.deviceIndependentComponents.forEach(c -> c.setVisible(!deviceDependent));
    }

    private Component getHeader(String title) {
        Label titleLabel = new Label(title);
        titleLabel.addStyleName("heading-2");
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(titleLabel);
        layout.setWidth(100, Unit.PERCENTAGE);
        return layout;
    }

}
