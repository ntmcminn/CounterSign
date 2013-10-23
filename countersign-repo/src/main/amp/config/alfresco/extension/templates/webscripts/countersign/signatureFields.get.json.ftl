{
	<#if unusedSignatureFields??>
		"unusedSignatureFields" : [
			<#list unusedSignatureFields as signatureField>
			"${signatureField}"<#if signatureField_has_next>,</#if>
			</#list>
		],
    </#if>
	<#if allSignatureFields??>
		"allSignatureFields" : [
			<#list allSignatureFields as signatureField>
			"${signatureField}"<#if signatureField_has_next>,</#if>
			</#list>
		]
    </#if>
}