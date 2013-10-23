<#include "/org/alfresco/include/alfresco-macros.lib.ftl" />
<#assign el=args.htmlid?js_string>

<div id="${el}-body" class="signature-details-panel">

	<h2 id="${el}-heading" class="thin dark">
		${msg("header.signatureValidation")}
	</h2>

	<div class="panel-body">
		<div id="${el}-status"></div>
	</div>
	<div class="panel-body">
		<a href="${page.url.context}/components/signature/signature-status.html">
			<img src="${page.url.context}/res/countersign/img/printer.png"/>
			${msg("page.signatureValidation.printable")}
		</a>
		<div class="link-pad"></div>
		<a href="${siteURL("view-signatures?nodeRef=" + context.properties.nodeRef?url)}">
			<img src="${page.url.context}/res/components/documentlibrary/actions/view-signatures-16.png"/>
			${msg("page.signatureValidation.viewSignatures")}
		</a>
	</div>
	<script type="text/javascript">//<![CDATA[
     new CounterSign.SignatureStatus("${el}").setOptions(
     {
        nodeRef: "${nodeRef?js_string}",
     }).setMessages(
        ${messages}
     );
     //]]></script>
</div>