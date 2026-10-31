package com.tbscg.doosan.magnolia.product.catalog.field.product;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tbscg.doosan.magnolia.product.catalog.field.ui.TableCell;
import com.vaadin.data.provider.Query;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.LIST;
import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.NESTED_LIST;
import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.NUMBER;
import static com.tbscg.doosan.magnolia.product.catalog.field.product.ProductPropertyType.TEXT;
import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsMapper.EMPTY_VALUE_REPLACEMENT_STRING;
import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsMapper.mapListPropertiesToHtmlList;
import static com.tbscg.doosan.magnolia.product.catalog.field.ui.ProductSpecsMapper.mapProductPropertyToHtmlList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ProductSpecificationTemplateTest {

    final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void shouldGetComponents_returnCorrectComponents() throws IOException {
        final FileInputStream fileInputStream = new FileInputStream("src/test/resources/model-mock.json");
        final String jsonString = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
        final JsonNode jsonNode = mapper.readTree(jsonString);

        final ProductSpecification productSpecs = mapper.treeToValue(jsonNode, ProductSpecification.class);
        final ProductSpecificationTemplate specsTemplate = new ProductSpecificationTemplate(productSpecs);
        final Component[] components = specsTemplate.getComponents();
        assertEquals(7, components.length);
        assertTopLevelTextFields(components, Arrays.asList(
                "S570 T4",
                "M0259_TEST",
                "current",
                "2021/05/21 14:45:50",
                "https://fms.qa.dice-tools.com/136296",
                "Lorem ipsum"));

        final Component accordionComponent = components[6];
        assertAccordion(accordionComponent);

        final Accordion accordion = (Accordion) accordionComponent;
        final List<String> sectionNames = Arrays.asList(
                "Properties",
                "Core",
                "Machine",
                "Simple list",
                "Nested list",
                "Nested list with properties",
                "Simple table",
                "Table with references"
        );
        IntStream.range(0, sectionNames.size()).forEach(i -> assertSection(accordion.getTab(i), sectionNames.get(i), getSectionExpectedComponents(i)));
    }

    @Test
    public void shouldGetComponents_returnCorrectComponents_whenSomeTopLevelFieldsAreMissing() {
        final ProductSpecification productSpecs = new ProductSpecification("name", "", "", "", null, null, null);
        final ProductSpecificationTemplate specsTemplate = new ProductSpecificationTemplate(productSpecs);
        final Component[] components = specsTemplate.getComponents();
        assertTopLevelTextFields(components, Arrays.asList(
                "name",
                EMPTY_VALUE_REPLACEMENT_STRING,
                EMPTY_VALUE_REPLACEMENT_STRING,
                EMPTY_VALUE_REPLACEMENT_STRING,
                EMPTY_VALUE_REPLACEMENT_STRING,
                EMPTY_VALUE_REPLACEMENT_STRING));
        assertEquals(6, components.length);
    }

    private void assertTopLevelTextFields(final Component[] components, final List<String> expectedValues) {
        IntStream.range(0, expectedValues.size()).forEach(i -> {
            assertEquals(TextField.class, components[i].getClass());
            assertEquals(expectedValues.get(i), extractValuesToString(components[i], TextField.class));
        });
    }

    private void assertAccordion(final Component component) {
        assertEquals(Accordion.class, component.getClass());
        assertEquals("Sections", component.getCaption());
    }

    private void assertSection(final TabSheet.Tab tab, final String title, final List<SectionField> expectedFields) {
        assertEquals(title, tab.getCaption());
        assertEquals(FormLayout.class, tab.getComponent().getClass());
        final FormLayout layout = (FormLayout) tab.getComponent();
        assertEquals(expectedFields.size(), layout.getComponentCount());
        IntStream.range(0, expectedFields.size()).forEach(i -> {
            final Class<?> fieldClass = layout.getComponent(i).getClass();
            final String actualFlatValues = extractValuesToString(layout.getComponent(i), fieldClass);
            assertTrue("Actual field type does not match given type of expected value.", expectedFields.get(i).containsKey(fieldClass));
            assertEquals(expectedFields.get(i).get(fieldClass), actualFlatValues);
        });
    }

    private String extractValuesToString(final Component component, final Class<?> type) {
        if (type.equals(TextField.class)) {
            return ((TextField) component).getValue();
        }
        if (type.equals(Label.class)) {
            return ((Label) component).getValue();
        }
        if (type.equals(Grid.class)) {
            final String header = ((Grid<?>) component).getColumns().stream().map(Grid.Column::getCaption).collect(joining(";", "[", "]"));
            final String items = ((Grid<?>) component).getDataProvider()
                    .fetch(new Query<>())
                    .map(c -> (TableCell) c)
                    .map(c -> c.getValues().stream().collect(joining(";", "[", "]")))
                    .collect(joining(";"));
            return String.format("%s;%s", header, items);
        }
        if (type.equals(FormLayout.class)) {
            final FormLayout layout = ((FormLayout) component);
            final StringJoiner joiner = new StringJoiner(";", "[", "]");
            IntStream.range(0, layout.getComponentCount())
                    .forEach(i -> joiner.add(extractValuesToString(layout.getComponent(i), layout.getComponent(i).getClass())));
            return joiner.toString();
        } else {
            return "Unknown type: " + type;
        }
    }

    private List<SectionField> getSectionExpectedComponents(final int sectionIndex) {
        switch (sectionIndex) {
            case 0:
                return singletonList(
                        new SectionField(Grid.class, "[Spec Label;Spec Value;Filtering ID];[Identifier;S570 T4;--]")
                );
            case 1:
                return singletonList(
                        new SectionField(Grid.class, "[Spec Label;Spec Value;Filtering ID];" +
                                "[Identifier;S570 T4;--];" +
                                "[Image;--;--];" +
                                "[Fuel Tank;--;capacityFuelTank];" +
                                "[Turbocharged Engine;yes;--];" +
                                "[Auxiliary High Flow (l/min);101.1;auxiliaryHighFlow]")
                );
            case 2:
                return Arrays.asList(
                        new SectionField(TextField.class, "7104371"),
                        new SectionField(TextField.class, "7102125"),
                        new SectionField(TextField.class, "6905277"),
                        new SectionField(FormLayout.class, "[[Spec Label;Spec Value;Filtering ID];[DIM2 REF1;VALUE 11111;--];[DIM2 REF2;VALUE 222222;--]]")
                );
            case 3:
                return singletonList(new SectionField(Label.class, mapListPropertiesToHtmlList(
                        Arrays.asList(
                                new ListProperty("Air conditioning", "bla bla bla"),
                                new ListProperty("Back-up alarm", null),
                                new ListProperty("Basic tow hitch", null)
                        )
                )));
            case 4:
                final String nestedList1 = mapListPropertiesToHtmlList(
                        Arrays.asList(
                                new ListProperty("Deluxe pressurized cab with heating", "A secret note from the producer of Spiderman"),
                                new ListProperty("23 x 8.50-12 6-ply, heavy duty tyres", null),
                                new ListProperty("Back-up alarm", null)
                        )
                );
                final String nestedList2 = mapListPropertiesToHtmlList(
                        Arrays.asList(
                                new ListProperty("7 and 14-pin electrical attachment control kit", null),
                                new ListProperty("Back up alarm kit", null),
                                new ListProperty("Cab enclosure kit", null)
                        )
                );
                return singletonList(new SectionField(Label.class, String.format(
                        "<ul><li><b>Factory Options</b>: %s</li>\n<li><b>Dealer Installed Options</b>: %s</li></ul>",
                        nestedList1,
                        nestedList2)
                ));
            case 5:
                return Arrays.asList(
                        new SectionField(Label.class, mapProductPropertyToHtmlList(
                                new ProductProperty("deckWidth", LIST, "Width", new PropertyValue(Arrays.asList(
                                        new ProductProperty(null, NUMBER, null, new PropertyValue("1099"), "mm"),
                                        new ProductProperty(null, NUMBER, null, new PropertyValue("1321"), "mm"),
                                        new ProductProperty(null, NUMBER, null, new PropertyValue("1219"), "mm")
                                ), ProductProperty.class), null)
                        )),
                        new SectionField(Label.class, mapProductPropertyToHtmlList(
                                new ProductProperty("test", NESTED_LIST, "NESTED TEST", new PropertyValue(singletonList(
                                        new ProductProperty(null, LIST, "nested so much", new PropertyValue(Arrays.asList(
                                                new ProductProperty(null, NUMBER, "Sample 1", new PropertyValue("111"), "mm"),
                                                new ProductProperty(null, TEXT, "Sample 2", new PropertyValue("sample"), null),
                                                new ProductProperty(null, NUMBER, "Sample 3", new PropertyValue("33"), "mm")
                                        ), ProductProperty.class), null)
                                ), ProductProperty.class), null)
                        )),
                                new SectionField(Grid.class, "[Spec Label;Spec Value;Filtering ID];[Mower Deck;TufDeck™ 10-gauge Top with 7-gauge Spindle Supports; 7-gauge Side Skirts;--]")
                );
            case 6:
                return singletonList(
                        new SectionField(Grid.class, Stream.of(
                                Arrays.asList("Description", "Item number"),
                                Arrays.asList("Angle Broom 122 cm", "6907000"),
                                Arrays.asList("Angle Broom 173 cm ACD", "6905805"),
                                Arrays.asList("Angle Broom 213 cm ACD", "6905806")
                        ).map(i -> i.stream().collect(joining(";", "[", "]"))).collect(joining(";")))
                );
            case 7:
                return singletonList(
                        new SectionField(Grid.class, Stream.of(
                                Arrays.asList("Description", "Item number", "Compatible with loader"),
                                Arrays.asList("Auger 10 Unit Drive", "6808799", "MT55E, T450E V, S70E, S100E"),
                                Arrays.asList("Auger S500", "S550-12345", "S70E, S100E"),
                                Arrays.asList("Auger S500 list", "S550-12345 list", "list in table (Note: just a tiny note), OMG, LOL")
                        ).map(i -> i.stream().collect(joining(";", "[", "]"))).collect(joining(";")))
                );
            default:
                return emptyList();
        }
    }

    private static class SectionField extends HashMap<Class<?>, String> {
        public SectionField(final Class<?> type, final String value) {
            super.put(type, value);
        }
    }
}
