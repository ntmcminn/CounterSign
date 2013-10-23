
	<#if isSigned??>
		[
			<#list signatures as signature>
			{
				"externalSigner":"none",
				"signatureDate":"${signature.properties["csign:signatureDate"]?datetime}",
				"location":"${signature.properties["csign:location"]}",
				"reason":"${signature.properties["csign:reason"]}",
				"signatureField":"${signature.properties["csign:signatureField"]}",
                "signatureLat":"${signature.properties["cm:latitude"]}",
                "signatureLong":"${signature.properties["cm:longitude"]}",
				"page":"${signature.properties["csign:signaturePage"]}",
				"position":"${signature.properties["csign:signaturePosition"]}",
				<#if signature.assocs["csign:signedBy"]??>
				<#assign signedBy=signature.assocs["csign:signedBy"]>
                "signedByUserName":"${signedBy[0].properties["cm:userName"]}",
                "signedByFirstName":"${signedBy[0].properties["cm:firstName"]}",
                "signedByLastName":"${signedBy[0].properties["cm:lastName"]}",
                "signedByEmail":"${signedBy[0].properties["cm:email"]}",
                "nodeRef":"${signedBy[0].id}"
                <#else>
                "signedByUserName":"${signature.properties["cm:creator"]}",
                "signedByFirstName":"Deleted",
                "signedByLastName":"User",
                "signedByEmail":"Deleted",
                "nodeRef":""
                </#if>
			}<#if signature_has_next>,</#if>
			</#list>
		]
	</#if>
	