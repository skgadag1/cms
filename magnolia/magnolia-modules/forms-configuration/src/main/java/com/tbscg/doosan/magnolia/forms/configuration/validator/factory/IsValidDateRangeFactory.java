package com.tbscg.doosan.magnolia.forms.configuration.validator.factory;

import com.tbscg.doosan.magnolia.forms.configuration.validator.definition.IsValidDateRangeDefinition;
import com.vaadin.data.ValidationResult;
import com.vaadin.data.Validator;
import com.vaadin.data.ValueContext;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import info.magnolia.ui.field.AbstractFieldValidatorFactory;
import info.magnolia.ui.framework.layout.field.FieldLayoutComponent;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.StreamSupport;


public class IsValidDateRangeFactory extends AbstractFieldValidatorFactory<IsValidDateRangeDefinition, LocalDate> {
    private final String startDateFieldName;
    private final String endDateFieldName;
    private final Period period;

    public IsValidDateRangeFactory(IsValidDateRangeDefinition definition) {
        super(definition);
        startDateFieldName = definition.getStartDateFieldName();
        endDateFieldName = definition.getEndDateFieldName();
        period = Period.parse(definition.getPeriod());
    }

    @Override
    public Validator<LocalDate> createValidator() {
        return (value, ctx) -> {
            if (value == null) {
                return ValidationResult.error("Dates must be set");
            }
            try {
                LocalDate startDate = getDateByComponentName(ctx, startDateFieldName);
                LocalDate endDate = getDateByComponentName(ctx, endDateFieldName);
                //This is done so the field validation doesn't fail when the other field is empty,
                //because on field initialization, while checking the first field, the other field's value is not yet filled which results in an error confusing the user
                if (ObjectUtils.anyNull(startDate, endDate)) {
                    return ValidationResult.ok();
                }

                if (isInFuture(startDate, endDate)) {
                    return ValidationResult.error("Dates should not be in the future");
                }

                if (isNegativePeriod(startDate, endDate)) {
                    return ValidationResult.error("Invalid period, maximum dates diff is " + period.getMonths() + " months and " + period.getDays() + " days");
                }
            } catch (IllegalArgumentException e) {
                return ValidationResult.error("Error occured during validation");
            }
            return ValidationResult.ok();
        };
    }

    private LocalDate getDateByComponentName(ValueContext ctx, String name) {
        HorizontalLayout commonParent = getCommonParent(ctx.getComponent().get());
        FieldLayoutComponent.Root dateFieldParent = (FieldLayoutComponent.Root) StreamSupport.stream(commonParent.spliterator(), false)
                .filter(component -> name.equalsIgnoreCase(component.getCaption()))
                .findFirst().orElse(null);
        return (LocalDate) dateFieldParent.getValue();
    }

    private HorizontalLayout getCommonParent(Component component) {
        do {
            component = component.getParent();
            if (component == null) {
                throw new IllegalArgumentException("No common parent found");
            }
        } while (!isCommonParent(component));
        return (HorizontalLayout) component;
    }

    private boolean isCommonParent(Component component) {
        if (!(component instanceof HorizontalLayout)) {
            return false;
        }
        HorizontalLayout layout = (HorizontalLayout) component;
        Component child1 = layout.getComponent(0);
        Component child2 = layout.getComponent(1);
        return child1 instanceof FieldLayoutComponent.Root && child2 instanceof FieldLayoutComponent.Root;
    }

    private boolean isInFuture(LocalDate startDate, LocalDate endDate) {
        LocalDate now = LocalDate.now();
        return now.isBefore(startDate) || now.isBefore(endDate);
    }


    private boolean isNegativePeriod(LocalDate startDate, LocalDate endDate) {
        Period between = Period.between(startDate, endDate);
        int betweenDays = between.getDays() + between.getMonths() * 30 + between.getYears() * 365;
        int basicPeriodInDays = period.getDays() + period.getMonths() * 30 + period.getYears() * 365;
        return between.isNegative() || basicPeriodInDays - betweenDays < 0;
    }

}
