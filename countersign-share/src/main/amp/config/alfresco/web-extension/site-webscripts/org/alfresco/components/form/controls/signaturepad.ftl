<script type="text/javascript" src="${url.context}/res/countersign/components/thirdparty/SignaturePad/jquery.signaturepad.js"></script>
<script type="text/javascript" src="${url.context}/res/countersign/components/thirdparty/SignaturePad/json2.js"></script>
<script type="text/javascript" src="${url.context}/res/countersign/components/thirdparty/SignaturePad/flashcanvas.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/countersign/components/thirdparty/SignaturePad/jquery.signaturepad.css" />
<div class="form-field" id="${field.name}-control">
	<#if form.mode == "view">
		<!-- how to display a signature when in "view" mode? Should we, since the 
		sig is embedded in the doc itself? -->
	<#else>
		<div id="${fieldHtmlId}-sigcontainer">
		  	<ul class="sigNav">
		    	<li class="drawIt"><a href="#draw-it">Draw Signature</a></li>
		    	<li class="clearButton"><a href="#clear">Clear</a></li>
		  	</ul>
		  	<div class="sig sigWrapper">
		    	<div class="typed"></div>
		    	<canvas class="pad" width="350" height="75"></canvas>
		    	<input type="hidden" id="${fieldHtmlId}" name="${field.name}" class="output">
		  	</div>		
		</div>

		<script type="text/javascript">//<![CDATA[
		(function($) 
		{
			$(document).ready(function() {
				$.ajax({
					  url: Alfresco.constants.PROXY_URI + "countersign/signature/user",
					  context: document.body
					}).done(function(sigSource) {
						$('#${fieldHtmlId}-sigcontainer').signaturePad(
								{defaultAction:"drawIt",output:'#${fieldHtmlId}',lineTop:65,drawOnly:true,validateFields:false}
						).regenerate(sigSource);
					});
		  	});
		})(jQuery);
		//]]></script>	
	</#if>
</div>