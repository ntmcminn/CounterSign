<#include "../../../include/alfresco-macros.lib.ftl" />
<script type="text/javascript">//<![CDATA[
   new Alfresco.component.ShareFormManager("${args.htmlid}").setOptions(
   {
      failureMessage: "signature-form-mgr.update.failed",
      submitButtonMessageKey: "signature-form-mgr.button.sign"
   }).setMessages(${messages});
//]]></script>
<div style="display:block">&nbsp</div>