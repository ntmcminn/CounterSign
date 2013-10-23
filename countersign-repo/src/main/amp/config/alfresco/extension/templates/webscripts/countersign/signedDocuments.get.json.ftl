[
	<#list items as item>
	{
		"documentName":"${item.document.properties["cm:name"]}",
		"signatureDate":"${item.signature.properties["csign:signatureDate"]?datetime}",
		"reason":"${item.signature.properties["csign:reason"]}",
		"location":"${item.signature.properties["csign:location"]}",
		"nodeRef":"${item.document.nodeRef}"
	}<#if item_has_next>,</#if>
	</#list>
]
