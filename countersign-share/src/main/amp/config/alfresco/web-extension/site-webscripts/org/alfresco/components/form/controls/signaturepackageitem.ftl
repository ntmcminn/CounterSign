<#include "/org/alfresco/components/form/controls/association.ftl" />

<#macro renderPackageItems field>

   <#local documentLinkResolver>
	function(item)
	{
	   return item.isContainer ? Alfresco.util.siteURL("folder-details?nodeRef=" + item.nodeRef, { site: item.site }) : Alfresco.util.siteURL("document-details?nodeRef=" + item.nodeRef, { site: item.site });
	}
   </#local>
   
   <#local signatureLinkResolver>
	function(item)
	{
	   return Alfresco.util.siteURL("sign-document?nodeRef=" + item.nodeRef, { site: item.site });
	}
   </#local>

   <#local actions = []>

   <#local viewMoreAction = { "name": "view_details", "label": "form.control.object-picker.countersign.workflow.view_details", "link": documentLinkResolver }>
   <#local signAction = { "name" : "sign_document", "label":"form.control.object-picker.countersign.workflow.sign_document", "link": signatureLinkResolver }>
   <#local actions = actions + [viewMoreAction]>
   <#local actions = actions + [signAction]>

   <script type="text/javascript">//<![CDATA[
   (function()
   {
      <#-- Modify the properties on the object finder created by association control-->
      var picker = Alfresco.util.ComponentManager.get("${controlId}");
      picker.setOptions(
      {
         showLinkToTarget: true,
         targetLinkTemplate: ${documentLinkResolver},         
      <#if form.mode == "create" && form.destination?? && form.destination?length &gt; 0>
         startLocation: "${form.destination?js_string}",
      <#elseif field.control.params.startLocation??>
         startLocation: "${field.control.params.startLocation?js_string}",
      </#if>
         itemType: "cm:content",
         displayMode: "${field.control.params.displayMode!"list"}",
         listItemActions: [
         <#list actions as action>
         {
            name: "${action.name}",
            <#if action.link??>
            link: ${action.link},
            <#elseif action.event>
            event: "${action.event}", 
            </#if>
            label: "${action.label}"
         }<#if action_has_next>,</#if>
         </#list>],
         allowRemoveAction: false,
         allowRemoveAllAction: false,
         allowSelectAction: false,
         selectActionLabel: ""
      });
   })();
   //]]></script>

</#macro>

<@renderPackageItems field />