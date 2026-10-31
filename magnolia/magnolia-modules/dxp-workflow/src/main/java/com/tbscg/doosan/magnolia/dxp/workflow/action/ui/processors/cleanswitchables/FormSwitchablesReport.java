package com.tbscg.doosan.magnolia.dxp.workflow.action.ui.processors.cleanswitchables;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * Holds information about switchable fields in form definition.
 */
@Data
@Builder(setterPrefix = "with")
@ToString
public
class FormSwitchablesReport {
    private List<SwitchableFieldDesc> fieldDescList;
}
