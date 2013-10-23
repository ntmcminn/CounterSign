<#assign el=args.htmlid?js_string>

<div id="${el}-body" class="signature-details-panel">

	<h2 id="${el}-heading" class="thin dark">
		${msg("header.signatures")}
	</h2>

	<div class="panel-body">
		<div id="${el}-signatures"></div>
	</div>
	<script type="text/javascript">//<![CDATA[
      new CounterSign.SignatureList("${el}").setOptions(
      {
         nodeRef: "${nodeRef?js_string}",
      }).setMessages(
         ${messages}
      );
      //]]></script>
</div>