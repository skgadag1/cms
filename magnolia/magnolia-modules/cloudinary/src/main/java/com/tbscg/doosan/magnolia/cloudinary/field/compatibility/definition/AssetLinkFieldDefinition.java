package com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition;

import com.tbscg.doosan.magnolia.cloudinary.contentapp.filter.AssetFilterDefinition;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Extension of
 * {@link info.magnolia.dam.external.app.field.definition.AssetLinkFieldDefinition}
 * which uses standard property name as a default value of basePropertyName
 */
@Deprecated
public class AssetLinkFieldDefinition extends info.magnolia.dam.external.app.field.definition.AssetLinkFieldDefinition {

	private List<AssetFilterDefinition> filters = Collections.emptyList();

	public AssetLinkFieldDefinition() {
		super();
		setBasePropertyName(null);
	}

	public String getBasePropertyName() {
		String name = super.getBasePropertyName();
		return StringUtils.isEmpty(name) ? getName() : name;
	}

	public List<AssetFilterDefinition> getFilters() {
		return filters;
	}

	public void setFilters(List<AssetFilterDefinition> filters) {
		this.filters = filters;
	}
}
