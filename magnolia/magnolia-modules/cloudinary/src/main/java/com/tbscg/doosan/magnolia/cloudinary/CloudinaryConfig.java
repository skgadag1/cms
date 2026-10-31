package com.tbscg.doosan.magnolia.cloudinary;

import com.google.common.base.Preconditions;
import com.google.common.net.MediaType;
import com.tbscg.doosan.magnolia.cloudinary.api.ApiConfig;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.config.AssetPreviewConfiguration;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.AssetLinkFieldDefinition;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.definition.CloudinaryRichTextFieldDefinition;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.migration.AssetLinkFieldDefinitionConverter;
import com.tbscg.doosan.magnolia.cloudinary.field.compatibility.migration.CloudinaryRichTextFieldDefinitionConverter;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import static info.magnolia.ui.form.definition.DefinitionConverter.FIELD_DEFINITION_CONVERTERS;

/**
 * This class represents the configuration for the cloudinary module.
 * By exposing simple getter/setter/adder methods, this bean can be configured via content2bean
 * using the properties and node from <tt>config:/modules/cloudinary</tt>.
 * If you don't need this, simply remove the reference to this class in the module descriptor xml.
 */
@Slf4j
@Getter
@Setter
public class CloudinaryConfig implements ModuleLifecycle {
	private ApiConfig api;
	private String renditionUrlTemplate;
	private String thumbnailTransformation;
	private List<AssetPreviewConfiguration> assetPreviewConfigurations;
	private String cloudPlayer;
	private String folderMode;
	private String scriptSource;

	@Override
	public void start(ModuleLifecycleContext moduleLifecycleContext) {
		log.debug("START cloudinary module");

		//TODO: remove once everything is migrated to V6
		addCustomFieldConverters();
	}

	@Override
	public void stop(ModuleLifecycleContext moduleLifecycleContext) {
		log.debug("STOP cloudinary module");
	}

    /**
	 * Gets configured Cloudinary cloud name from config workspace.
	 *
	 * @return cloud name; <code>null</code> if no configuration is available
	 */
	public String getCloudName() {
		return this.api != null ? this.api.getCloudName() : null;
	}

	/**
	 * Returns the best matching asset preview configuration for a given media type.
	 *
	 * @param mediaType requested media type.
	 * @return the {@link AssetPreviewConfiguration} configured for the requested mediaType.
	 * throw new IllegalArgumentException if no {@link AssetPreviewConfiguration} is found in config.
	 */
	public AssetPreviewConfiguration getAssetPreviewConfigurationForMediaType(MediaType mediaType) {

		Preconditions.checkArgument(mediaType != null, "Media type can't be null");

		AssetPreviewConfiguration resolvedConfiguration = null;
		MediaType bestCandidate = MediaType.ANY_TYPE;

		for (AssetPreviewConfiguration configuration : this.assetPreviewConfigurations) {

			resolvedConfiguration = this.getResolvedConfiguration(configuration, mediaType, bestCandidate);

			if (resolvedConfiguration != null) {
				break;
			}
		}

		Preconditions.checkNotNull(resolvedConfiguration, "There is no AssetPreviewConfiguration defined for the " +
				"following mediaType" + mediaType.toString());

		return resolvedConfiguration;
	}

	/**
	 * Returns given configuration if supports the reference media type. Otherwise returns <code>null</code>.
	 *
	 * @param configuration configuration to be returned in case that supports the reference media type.
	 * @param refMediaType reference media type that our configuration should support.
	 * @param bestCandidate matching candidate which should be equal to reference media type.
	 * @return {@link AssetPreviewConfiguration} configuration that supports reference media type. <code>null</code>
	 * if it is not supported.
	 */
	private AssetPreviewConfiguration getResolvedConfiguration(AssetPreviewConfiguration configuration,
															  MediaType refMediaType, MediaType bestCandidate) {

		for (MediaType configuredMediaType : configuration.getSupportedMediaTypes()) {

			if (refMediaType.is(configuredMediaType)) {
				bestCandidate = this.getBestMatchingCandidate(bestCandidate, configuredMediaType, refMediaType);

				if (bestCandidate == configuredMediaType) {
					return configuration;
				}
			}
		}

		return null;
	}

	/**
	 * Gets the best supported media type between 2 candidates for a given reference.
	 *
	 * @param candidate1 first candidate media type to evaluate.
	 * @param candidate2 second candidate media type to evaluate.
	 * @param reference reference media type.
	 * @return best matching {@link MediaType} for 'reference' between 'candidate1' and 'candidate2'.
	 * If both candidate have the same matching average, return 'candidate1'.
	 */
	private MediaType getBestMatchingCandidate(MediaType candidate1, MediaType candidate2, MediaType reference) {
		if(reference.equals(candidate1)) {
			return candidate1;
		}
		if(reference.equals(candidate2)) {
			return candidate2;
		}

		if (voteOnMediaType(candidate1, reference) >= voteOnMediaType(candidate2, reference)) {
			return candidate1;
		}
		return candidate2;
	}

	/**
	 * Establishes a matching voting score for suitability of a candidate media type given a reference media type.
	 *
	 * @param candidate candidate media type.
	 * @param reference reference media type.
	 * @return matching voting score.
	 */
	private int voteOnMediaType(MediaType candidate, MediaType reference) {
		int res = 0;
		if (!reference.parameters().isEmpty() && candidate.parameters().entries().containsAll(reference.parameters().entries())) {
			res += 1;
		}
		if (StringUtils.equals(reference.type(), candidate.type())) {
			res += 2;
		}
		if (StringUtils.equals(reference.subtype(), candidate.subtype())) {
			res += 3;
		}
		return res;
	}

	private static void addCustomFieldConverters() {
		FIELD_DEFINITION_CONVERTERS.put(CloudinaryRichTextFieldDefinition.class, new CloudinaryRichTextFieldDefinitionConverter());
		FIELD_DEFINITION_CONVERTERS.put(AssetLinkFieldDefinition.class, new AssetLinkFieldDefinitionConverter());
	}
}
