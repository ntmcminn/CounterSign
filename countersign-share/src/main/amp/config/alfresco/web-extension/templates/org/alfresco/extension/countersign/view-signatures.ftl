<#include "../../include/alfresco-template.ftl" />
<@templateHeader>
<@script type="text/javascript" src="${page.url.context}/res/countersign/components/thirdparty/JQuery/jquery-1.8.3.js"></@script>
<@script type="text/javascript" src="${page.url.context}/res/countersign/components/thirdparty/JQueryUI/js/jquery-ui-1.10.3.custom.js"></@script>
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
            <@region id="signature-list" scope="template"/>
            <@region id="signature-map" scope="template"/>
         </div>
      </div>
      <div class="yui-gc"><@region id="signature-timeline" scope="template"/></div>
   </div>
</@>

<@templateFooter>
   <div id="alf-ft">
      <@region id="footer" scope="global"/>
   </div>
</@>