<script type="text/javascript" src="${url.context}/res/countersign/components/selectpage/selectpage.js"></script>
<div class="form-field">
	<#if form.mode == "view">
	
	<#else>
		<label for="${fieldHtmlId}">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
		<select id="${fieldHtmlId}" name="${field.name}">
		</select>
	<#/if>
</div>
<script type="text/javascript">//<![CDATA[
var signaturePosition = function() 
{
	
	function setPageCount(response)
	{
		var pageSelect = YAHOO.util.Dom.get("${fieldHtmlId}");
		var pages = parseInt(response.json.pageCount);
		if(pages > 0)
		{
			for(var i = 1;i < pages + 1; i++)
			{
				var opt = document.createElement('option');
				opt.text = i;
				opt.value = i;
				pageSelect.add(opt, null);
			}
		}
	}
	
	// get the page count for this doc
	var pageCount = getPageCount("${context.properties.nodeRef}", setPageCount);
}();
//]]></script>