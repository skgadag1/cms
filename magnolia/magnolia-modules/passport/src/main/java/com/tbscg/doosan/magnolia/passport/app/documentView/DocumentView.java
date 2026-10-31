  package com.tbscg.doosan.magnolia.passport.app.documentView;

  import com.tbscg.doosan.magnolia.passport.data.model.*;
  import com.tbscg.doosan.magnolia.passport.data.provider.api.ApiService;
  import com.tbscg.doosan.magnolia.passport.ui.LocalFileUpload;
  import com.tbscg.doosan.magnolia.passport.ui.documents.ButtonBar;
  import com.tbscg.doosan.magnolia.passport.util.DocumentConstants;
  import com.vaadin.icons.VaadinIcons;
  import com.vaadin.ui.*;
  import lombok.Getter;
  import lombok.extern.slf4j.Slf4j;
  import org.vaadin.dialogs.ConfirmDialog;

  import java.util.ArrayList;
  import java.util.Collections;
  import java.util.List;
  import java.util.stream.Collectors;

  @Slf4j
  @Getter
  public class DocumentView extends VerticalLayout {

      private final ApiService apiService;
      private PassportDocumentWithDetails document;
      private final TextField documentName = new TextField("Document");
      private final Grid<PassportFileTranslation> filesGrid = new Grid<>();
      private List<PassportFileTranslation> files = Collections.emptyList();
      private List<PropertySimple> webRoles;
      private final TwinColSelect<PropertySimple> webRolesSelect = new TwinColSelect<>();
      private List<PropertySimple> contractTypes;
      private final TwinColSelect<PropertySimple> contractTypesSelect = new TwinColSelect<>();
      private List<PropertySimple> contractCodes;
      private final TwinColSelect<PropertySimple> contractCodesSelect = new TwinColSelect<>();
      private List<String> accountTypes;
      private final TwinColSelect<String> accountTypesSelect = new TwinColSelect<>();
      private List<PropertyCodeName> countryCodes;
      private final TwinColSelect<PropertyCodeName> countryCodesSelect = new TwinColSelect<>();


      public DocumentView(ApiService apiService, PassportDocumentWithDetails document) {
          setWidth(100, Unit.PERCENTAGE);
          this.apiService = apiService;
          this.document = document;
          createContent();
      }

      private void createContent() {
          PassportDocumentTranslation defTranslation = document.getContents().stream()
                  .filter(c -> "en".equals(c.getLanguage()))
                  .findFirst().orElse(new PassportDocumentTranslation());

          addComponent(getHeader(defTranslation.getTitle(), defTranslation.getDescription()));
          HorizontalLayout layout = new HorizontalLayout();
          layout.setSizeFull();
          layout.addComponent(propertiesPanel());
          layout.addComponent(filesPanel());
          addComponent(layout);
      }

      private Component propertiesPanel() {

          PassportDocumentDefinitionResponse definitions = apiService.getDefinitions();

          this.webRoles = definitions.getWebSecurityRoles();
          this.webRolesSelect.setCaption("Web Roles");
          this.webRolesSelect.setItemCaptionGenerator(PropertySimple::getName);
          this.webRolesSelect.setItems(this.webRoles);
          this.webRolesSelect.setWidth(95, Unit.PERCENTAGE);

          HorizontalLayout rowOne = new HorizontalLayout();
          rowOne.setSizeFull();
          rowOne.addComponents(webRolesSelect);

          this.contractTypes = definitions.getContractTypes();
          this.contractTypesSelect.setCaption("Contract Types");
          this.contractTypesSelect.setItemCaptionGenerator(PropertySimple::getName);
          this.contractTypesSelect.setItems(this.contractTypes);
          this.contractTypesSelect.setWidth(90, Unit.PERCENTAGE);

          this.contractCodes = definitions.getContractCodes();
          this.contractCodesSelect.setCaption("Contract Codes");
          this.contractCodesSelect.setItemCaptionGenerator(PropertySimple::getName);
          this.contractCodesSelect.setItems(this.contractCodes);
          this.contractCodesSelect.setWidth(90, Unit.PERCENTAGE);

          HorizontalLayout rowTwo = new HorizontalLayout();
          rowTwo.setSizeFull();
          rowTwo.addComponents(contractTypesSelect, contractCodesSelect);

          this.accountTypes = definitions.getAccountTypes().stream()
                  .map(PropertyName::getName).collect(Collectors.toList());
          this.accountTypesSelect.setCaption("Account Types");
          this.accountTypesSelect.setItemCaptionGenerator(String::trim);
          this.accountTypesSelect.setItems(this.accountTypes);
          this.accountTypesSelect.setWidth(90, Unit.PERCENTAGE);

          this.countryCodes = definitions.getCountryCodes();
          this.countryCodesSelect.setCaption("Country Codes");
          this.countryCodesSelect.setItemCaptionGenerator(code -> code.getCode() + " - " + code.getName());
          this.countryCodesSelect.setItems(this.countryCodes);
          this.countryCodesSelect.setWidth(90, Unit.PERCENTAGE);

          HorizontalLayout rowThree = new HorizontalLayout();
          rowThree.setSizeFull();
          rowThree.addComponents(accountTypesSelect, countryCodesSelect);

          Button saveDocumentButton = new Button("Save Settings");
          saveDocumentButton.addClickListener(e -> {
              PassportDocumentSavePayload updatedPayload = preparePayload();
              this.apiService.updateDocument(updatedPayload);
              this.reloadAll();
          });

          ButtonBar buttonBar = new ButtonBar(saveDocumentButton);
          buttonBar.setComponentAlignment(saveDocumentButton, Alignment.TOP_RIGHT);

          HorizontalLayout rowFour = new HorizontalLayout();
          rowFour.setWidthFull();
          rowFour.addComponents(buttonBar);

          initDocumentProperties();

          VerticalLayout layout = new VerticalLayout();
          layout.setSizeFull();
          layout.addComponent(rowOne);
          layout.addComponent(rowTwo);
          layout.addComponent(rowThree);
          layout.addComponent(rowFour);
          return layout;
      }

      private PassportDocumentSavePayload preparePayload() {
          PassportDocumentSavePayload updated = new PassportDocumentSavePayload(this.document);
          updated.setContractCodeIds(this.contractCodesSelect.getSelectedItems().stream()
                  .map(PropertySimple::getId).collect(Collectors.toList()));
          updated.setContractTypeIds(this.contractTypesSelect.getSelectedItems().stream()
                  .map(PropertySimple::getId).collect(Collectors.toList()));
          updated.setAccountTypes(new ArrayList<>(this.accountTypesSelect.getSelectedItems()));
          updated.setWebSecurityRoles(this.webRolesSelect.getSelectedItems().stream()
                  .map(PropertySimple::getName).collect(Collectors.toList()));
          updated.setCountryCodes(this.countryCodesSelect.getSelectedItems().stream()
                  .map(PropertyCodeName::getCode).collect(Collectors.toList()));
          return updated;
      }

      private void reloadAll() {
          reloadProperties();
          reloadFiles();
      }

      private void reloadProperties() {
          this.document = apiService.getDocument(this.document.getId());
          initDocumentProperties();
      }

      private void reloadFiles() {
          List<PassportFileTranslation> files = apiService.getDocumentFiles(document.getId());
          this.files = files;
          filesGrid.setItems(files);
      }

      private void initDocumentProperties() {
          this.webRolesSelect.select(this.document.getWebSecurityRoles().toArray(new PropertySimple[0]));
          this.contractTypesSelect.select(this.document.getContractTypes().toArray(new PropertySimple[0]));
          this.contractCodesSelect.select(this.document.getContractCodes().toArray(new PropertySimple[0]));

          String[] accountTypesStr = this.document.getAccountTypes().stream()
                  .map(PropertyAccountType::getName).toArray(String[]::new);
          this.accountTypesSelect.select(accountTypesStr);

          this.countryCodesSelect.select(this.document.getCountryCodes().toArray(new PropertyCodeName[0]));
      }

      private Component filesPanel() {
          filesGrid.setSelectionMode(Grid.SelectionMode.NONE);
          filesGrid.setCaption("Files:");
          filesGrid.setSizeFull();
          filesGrid.addColumn(PassportFileTranslation::getStorageReference).setCaption("Reference").setWidthUndefined();
          filesGrid.addColumn(PassportFileTranslation::getLocale).setCaption("Locale").setWidth(80);
          filesGrid.addColumn(PassportFileTranslation::getContentType).setCaption("Content type").setWidth(120);
          filesGrid.addComponentColumn(this::fileActions).setCaption("Actions").setWidth(120);
          reloadFiles();

          Button uploadButton = new Button("Upload new file", VaadinIcons.UPLOAD);
          uploadButton.addClickListener(e -> {
              List<Locale> selectableLocales = (this.files.size() == 0 ) ? Collections.singletonList(DocumentConstants.getDefaultLocale())
                      :this.apiService.getLocales().stream()
                      .filter(l -> this.files.stream().noneMatch(f -> f.getLocale().equals(l.getCode())))
                      .collect(Collectors.toList());
              getUI().addWindow(new LocalFileUpload(
                      data -> apiService.addDocumentFile(document.getId(), data).getUploadUrl(),
                      selectableLocales,
                      this::reloadFiles));
          });

          VerticalLayout layout = new VerticalLayout();
          layout.setSizeFull();
          layout.addComponent(uploadButton);
          layout.addComponentsAndExpand(filesGrid);
          return layout;
      }

      private Component fileActions(PassportFileTranslation file) {
          MenuBar menu = new MenuBar();
          menu.addItem("delete", VaadinIcons.CLOSE, item -> ConfirmDialog.show(
                  getUI(),
                  String.format("Are you sure to remove %s file?", file.getStorageReference()),
                  dialog -> {
                      if (dialog.isConfirmed()) {
                          apiService.deleteDocumentFile(document.getId(), file);
                          reloadFiles();
                      }
                  }));
          return menu;
      }

      private Component getHeader(String title, String desc) {
          Label titleLabel = new Label(title);
          Label descLabel = new Label(desc);
          titleLabel.addStyleName("heading-2");
          VerticalLayout layout = new VerticalLayout();
          layout.addComponent(titleLabel);
          layout.addComponent(descLabel);
          layout.setWidth(100, Unit.PERCENTAGE);
          return layout;
      }

  }
