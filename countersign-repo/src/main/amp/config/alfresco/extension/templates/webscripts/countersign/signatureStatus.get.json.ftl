<#if isSigned??>
[
	<#list rows as row>
	{
		"signatureDate":"${row.signature.properties["csign:signatureDate"]?datetime}",
		"location":"${row.signature.properties["csign:location"]}",
		"reason":"${row.signature.properties["csign:reason"]}",
		"signatureValid":${row.status.signatureValid?string},
		"hashValid":${row.status.hashValid?string},
		<#if row.signature.assocs["csign:signedBy"]??>
		<#assign signedBy=row.signature.assocs["csign:signedBy"]>
        "signedByUserName":"${signedBy[0].properties["cm:userName"]}",
        "signedByFirstName":"${signedBy[0].properties["cm:firstName"]}",
        "signedByLastName":"${signedBy[0].properties["cm:lastName"]}"
        <#else>
        "signedByUserName":"${row.signature.properties["cm:creator"]}",
        "signedByFirstName":"Deleted",
        "signedByLastName":"User"
        </#if>
	}<#if row_has_next>,</#if>
	</#list>
]
</#if>