<#assign id = args.htmlid>
<#assign jsid = args.htmlid?js_string>
<script type="text/javascript">//<![CDATA[
(function()
{
   var artifacts = new CounterSign.dashlet.SignatureArtifacts("${jsid}").setOptions(
   {
   }).setMessages(${messages});
})();
//]]></script>

<div class="dashlet">
   	<div class="title">${msg("header")}</div>
   	<div class="toolbar flat-button">
   		<span class="align-right yui-button" id="${id}-reset">
            <span class="first-child">
               <button type="button" tabindex="0"></button>
            </span>
   		</span>
   	</div>
   	<div id="${id}-list" class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>></div>
</div>
