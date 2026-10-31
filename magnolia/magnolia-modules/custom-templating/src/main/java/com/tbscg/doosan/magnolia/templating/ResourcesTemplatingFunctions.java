package com.tbscg.doosan.magnolia.templating;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import info.magnolia.module.resources.ResourceLinker;
import info.magnolia.resourceloader.Resource;
import info.magnolia.resourceloader.ResourceOrigin;
import info.magnolia.resourceloader.ResourceVisitor;

/**
 * Based on magnolia's default resource templating library. Extends the ability
 * to customize the attributes on resource links.
 * 
 * @see info.magnolia.modules.resources.templating.ResourcesTemplatingFunctions
 */
public class ResourcesTemplatingFunctions {

	public static final String NAME = "resfn2";

	private static final HtmlTag SCRIPT = new HtmlTag("script", "src", true)
			.attr("type", "text/javascript");

	private static final HtmlTag LINK = new HtmlTag("link", "href", false)
			.attr("rel", "stylesheet")
			.attr("type", "text/css");

	private final ResourceOrigin origin;
	private final ResourceLinker linker;

	@Inject
	public ResourcesTemplatingFunctions(ResourceOrigin resourceOrigin, ResourceLinker linker) {
		this.origin = resourceOrigin;
		this.linker = linker;
	}

	// css
	public String css(String pattern) {
		return css(pattern, Collections.emptyMap());
	}

	public String css(List<String> patterns) {
		return css(patterns, Collections.emptyMap());
	}

	public String css(String pattern, Map<String, String> attributes) {
		return css(Arrays.asList(pattern), attributes);
	}

	public String css(List<String> patterns, Map<String, String> attributes) {
		return generate(patterns, false, LINK, attributes);
	}

	// cached css
	public String cachedCss(String pattern) {
		return cachedCss(pattern, Collections.emptyMap());
	}

	public String cachedCss(List<String> patterns) {
		return cachedCss(patterns, Collections.emptyMap());
	}

	public String cachedCss(String pattern, Map<String, String> attributes) {
		return cachedCss(Arrays.asList(pattern), attributes);
	}

	public String cachedCss(List<String> patterns, Map<String, String> attributes) {
		return generate(patterns, true, LINK, attributes);
	}

	// js
	public String js(String pattern) {
		return js(pattern, Collections.emptyMap());
	}

	public String js(List<String> patterns) {
		return js(patterns, Collections.emptyMap());
	}

	public String js(String pattern, Map<String, String> attributes) {
		return js(Arrays.asList(pattern), attributes);
	}

	public String js(List<String> patterns, Map<String, String> attributes) {
		return generate(patterns, false, SCRIPT, attributes);
	}

	// cached js
	public String cachedJs(String pattern) {
		return cachedJs(pattern, Collections.emptyMap());
	}

	public String cachedJs(List<String> patterns) {
		return cachedJs(patterns, Collections.emptyMap());
	}

	public String cachedJs(String pattern, Map<String, String> attributes) {
		return cachedJs(Arrays.asList(pattern), attributes);
	}

	public String cachedJs(List<String> patterns, Map<String, String> attributes) {
		return generate(patterns, true, SCRIPT, attributes);
	}

	public String custom(
			String tagName, 
			String resourceAttribute, 
			boolean closed, 
			List<String> patterns, 
			Map<String, String> attributes) {
		HtmlTag tag = new HtmlTag(tagName, resourceAttribute, closed);
		return generate(patterns, false, tag, attributes);
	}

	public String cachedCustom(
			String tagName, 
			String resourceAttribute, 
			boolean closed, 
			List<String> patterns, 
			Map<String, String> attributes) {
		HtmlTag tag = new HtmlTag(tagName, resourceAttribute, closed);
		return generate(patterns, true, tag, attributes);
	}

	// internal
	private String generate(List<String> patterns, boolean cached, HtmlTag tag, Map<String, String> attributes) {
		List<String> resourceLinks = getResourceLinks(patterns, cached);
		return resourceLinks.stream()
				.map(s -> tag.generate(s, attributes))
				.collect(Collectors.joining("\n"));
	}

	private List<String> getResourceLinks(List<String> patterns, final boolean cached) {
		Map<String, List<String>> intermediates = new LinkedHashMap<>();
		patterns.forEach(pattern -> intermediates.put(pattern, new ArrayList<>()));
		origin.traverseWith(new ResourceVisitor() {
			@Override
			public void visitFile(final Resource resource) {
				patterns.stream()
					.filter(pattern -> match(resource.getPath(), pattern))
					.map(intermediates::get)
					.forEach(results -> results.add(resource.getPath()));
			}

			private boolean match(String resource, String pattern) {
				try {
					return resource.matches(pattern);
				} catch (PatternSyntaxException e) {
					return false;
				}
			}

			@Override
			public boolean visitDirectory(Resource resource) {
				return true;
			}
		});
		Set<String> set = new LinkedHashSet<>();
		intermediates.values().forEach(list -> set.addAll(list));
		return set.stream()
				.map(s -> linker.linkTo(s, cached))
				.collect(Collectors.toList());
	}

	private static class HtmlTag {

		private String name;
		private String resourceAttribute;
		private boolean closed;
		private Map<String, String> defaultAttributes = new LinkedHashMap<>();

		public HtmlTag(String name, String resourceAttribute, boolean closed) {
			this.name = name;
			this.resourceAttribute = resourceAttribute;
			this.closed = closed;
		}

		public HtmlTag attr(String name, String value) {
			this.defaultAttributes.put(name, value);
			return this;
		}

		private String processAttributes(String resourceLink, Map<String, String> customAttributes) {
			Map<String, String> attributes = new HashMap<>(this.defaultAttributes);
			attributes.putAll(customAttributes);
			attributes.put(this.resourceAttribute, resourceLink);
			return attributes.entrySet().stream()
					.map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
					.collect(Collectors.joining(" "));
		}

		public String generate(String resourceLink, Map<String, String> attributes) {
			StringBuilder result = new StringBuilder("<");
			String attrString = processAttributes(resourceLink, attributes);
			result.append((this.name + " " + attrString).trim());
			if (this.closed) {
				result.append("></");
				result.append(this.name);
			}
			result.append(">");
			return result.toString();
		}
	}
}
