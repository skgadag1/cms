[#macro toJson node depth=0]
[@compress single_line=true]
	${jsonfn.from(node).down(depth).addAll().exclude("mgnl.*", "jcr.*", "@.*").print()}
[/@compress]
[/#macro]

[#macro pageLink path]
[@compress]
	[#if path?has_content]
		[#assign targetPage = cmsfn.contentById(path)!]
		[#if targetPage?has_content]
			${cmsfn.link(targetPage)}
		[/#if]
	[/#if]
[/@compress]
[/#macro]

[#macro damLink asset rendition='']
[@compress]
	[#if asset?has_content]
		[#assign transformation = damfn.getTransformation(asset, rendition)]
		[#if transformation?has_content]
			${transformation.link}
		[/#if]
	[/#if]
[/@compress]
[/#macro]

[#macro downloadLink node propertyName]
[@compress]
	[#if node?has_content && (node[propertyName])?has_content]
		${cfgfn.kags.contentApiServer}/download/link/${node.@id}@${propertyName}
	[/#if]
[/@compress]
[/#macro]

[#macro singleLineJsonString]
[@compress single_line=true]
	[#local captured][#nested][/#local]
	${captured?replace("^\\s+|\\s+$|\\n|\\r", "", "rm")?json_string?replace("&quot;", r"\&quot;")}
[/@compress]
[/#macro]

[#macro escapeQuotes content]
[@compress single_line=true]
	${content?replace("'", "&#39;")?replace("&quot;", r"\&quot;")?replace("\"", r"\&quot;")}
[/@compress]
[/#macro]
