{
	"signatureAvailable":${signatureAvailable?string}
	<#if signatureAvailable>,
	"keystoreNode":<#if keyStore??>"${keyStore.nodeRef}"<#else>null</#if>,
	"imageNode":<#if sigImage??>"${sigImage.nodeRef}"<#else>null</#if>,
	"publicKeyNode":<#if publicKey??>"${publicKey.nodeRef}"<#else>null</#if>
	</#if>
}
