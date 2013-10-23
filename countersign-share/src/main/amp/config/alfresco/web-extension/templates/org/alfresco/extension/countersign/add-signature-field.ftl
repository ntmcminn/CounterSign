<#include "../../include/alfresco-template.ftl" />
<@templateHeader>
<@script type="text/javascript" src="${page.url.context}/res/countersign/components/common/countersign-common.js"></@script>
</@>

<@templateBody>
   <div id="alf-hd">
      <@region id="countersign-config" scope="template"/>
      <@region id="header" scope="global"/>
      <@region id="title" scope="template"/>
      <@region id="signature-toolbar" scope="template"/>
   </div>
   <div id="bd">
      <div class="yui-gc">
         <div class="yui-u first">
            <@region id="web-preview" scope="template"/>
         </div>
         <div class="yui-u">
         	<@region id="add-signature-field-form-mgr" scope="template"/>
            <@region id="add-signature-field-form" scope="template"/>
         </div>
      </div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
</@>