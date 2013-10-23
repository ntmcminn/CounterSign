<#assign id = args.htmlid>
<#assign jsid = args.htmlid?js_string>
<#assign currentFilter = preferences.filter!"today">
<script type="text/javascript">//<![CDATA[
(function()
{
   var artifacts = new CounterSign.dashlet.SignedDocuments("${jsid}").setOptions(
   {
   		filter: "${currentFilter?js_string}",
   		validFilters: [<#list filterRanges as filter>"${filter.type?js_string}"<#if filter_has_next>,</#if></#list>]
   }).setMessages(${messages});
})();
//]]></script>

<div class="dashlet">
   	<div class="title">${msg("header")}</div>
   	<div class="toolbar flat-button">
   		<span class="align-left signed-documents-title">
   			<span class="first-child">${msg("filter.selectLabel")}</span>
   		</span>
   		<span class="align-left yui-button yui-menu-button" id="${id}-range">
            <span class="first-child">
               <button type="button" tabindex="0"></button>
            </span>
         </span>
         <select id="${id}-range-menu">
         <#list filterRanges as filter>
            <option value="${filter.type?html}">${msg("filter." + filter.label)}</option>
         </#list>
         </select>
   	</div>
   	<div id="${id}-documents" class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>></div>
</div>
