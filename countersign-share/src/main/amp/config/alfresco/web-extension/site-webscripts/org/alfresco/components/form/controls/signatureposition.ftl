<!-- 
Two types of signature positions are supported here.  The first is a simple location
selection, just like the older digital signature action.  The second is the selection
of a defined "signatureField" in the PDF document.
 -->
<script type="text/javascript" src="${url.context}/res/countersign/components/signature-position/signature-position.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/countersign/components/signature-position/signature-position.css" />
<#include "/org/alfresco/components/form/controls/common/utils.inc.ftl" />

<#assign optionSeparator="#alf#">
<#assign labelSeparator="|">
<#assign fieldValue=field.value>
<#assign placeholderOnly=field.control.params.placeholderOnly>

<div class="form-field" id="${field.name}-control">
	<#if form.mode == "view">
	
	<#else>	
		<#if !(placeholderOnly == "true")>	
			<!-- radio buttons to show / hide position options -->
			<div id="${fieldHtmlId}-positionOptions" class="set-panel">
				<div class="radio-container">
				<label for="${fieldHtmlId}-signatureFieldOption" class="inlinelabel">Signature Field</label>
				<input name="${fieldHtmlId}-positionType" id="${fieldHtmlId}-signatureFieldOption" value="signatureField" type="radio" class="position-radio"/>
				</div>
				<div class="position-radio-separator"></div>
				<div class="radio-container">
				<label for="${fieldHtmlId}-drawnPositionOption" class="inlinelabel">Draw Location</label>
				<input name="${fieldHtmlId}-positionType" id="${fieldHtmlId}-drawnPositionOption" value="drawnPosition" type="radio" class="position-radio"/>
				</div>
				<div class="position-radio-separator"></div>
				<div class="radio-container">
				<label for="${fieldHtmlId}-predefinedLocationOption" class="inlinelabel">Predefined Location</label>
				<input name="${fieldHtmlId}-positionType" id="${fieldHtmlId}-predefinedLocationOption" value="predefinedPosition" type="radio" class="position-radio" checked />
				</div> 
			</div>
			
			<!-- 
			if sig fields are available, show the list of fields
			 -->
			<div id="${fieldHtmlId}-signatureFields" style="display:none;">
				<select id="${fieldHtmlId}-fieldSelect" name="${field.name}-fieldSelect">
					<option name="none">No Signature Fields Available</option>
				</select>
			</div>
	
			<div id="${fieldHtmlId}-drawnPosition" style="display:none;">
				<!-- there is nothing to show if we are using a drawn position -->
			</div>
			
			<!-- show the predefined signature position list -->
			<div id="${fieldHtmlId}-predefinedPositions">
		      <#if field.control.params.options?? && field.control.params.options != "">
		      	 <label for="${fieldHtmlId}-positionSelect" class="inlinelabel">Position</label>
		         <select id="${fieldHtmlId}-positionSelect" name="${field.name}-positionSelect" tabindex="0"
		               <#if field.description??>title="${field.description}"</#if>>
		               <#list field.control.params.options?split(optionSeparator) as nameValue>
		                  <#if nameValue?index_of(labelSeparator) == -1>
		                     <option value="${nameValue?html}"<#if nameValue == fieldValue?string || (fieldValue?is_number && fieldValue?c == nameValue)> selected="selected"</#if>>${nameValue?html}</option>
		                  <#else>
		                     <#assign choice=nameValue?split(labelSeparator)>
		                     <option value="${choice[0]?html}"<#if choice[0] == fieldValue?string || (fieldValue?is_number && fieldValue?c == choice[0])> selected="selected"</#if>>${msgValue(choice[1])?html}</option>
		                  </#if>
		               </#list>
		         </select>
		         <@formLib.renderFieldHelp field=field />
		      <#else>
		         <div id="${fieldHtmlId}" class="missing-options">${msg("form.control.selectone.missing-options")}</div>
		      </#if>
		      <label for="${fieldHtmlId}-pageSelect" class="inlinelabel">Page</label>
		      <select id="${fieldHtmlId}-pageSelect" name="${field.name}-pageSelect">
		      </select>
			</div> 
		</#if>
		<!--  show the page select if a predefined position is selected -->
		<input id="${fieldHtmlId}" name="${field.name}" type="hidden" value='{"type":"predefined","position":"center","page":"1"}' />

	</#if>
</div>

<!-- get signature info -->
<script type="text/javascript">//<![CDATA[
var CounterSignSignaturePosition = new CounterSign.SignaturePosition("${fieldHtmlId}").setOptions(
      {
         nodeRef: "${context.properties.nodeRef?js_string}",
         placeholderOnly: ${placeholderOnly}
      }).setMessages(
         {}
      );
//]]></script>