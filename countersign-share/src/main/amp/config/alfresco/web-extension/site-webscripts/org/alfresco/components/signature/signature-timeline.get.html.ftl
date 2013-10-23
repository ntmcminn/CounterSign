<#assign el=args.htmlid?js_string>

<div id="${el}-body" class="signature-details-panel">

	<h2 id="${el}-heading" class="thin dark">
		${msg("header.signatureTimeline")}
	</h2>

	<div class="panel-body signature-timeline">
		<div id="${el}-signature-timeline" class="signature-timeline"></div>
	</div>
	<script type="text/javascript">//<![CDATA[
	(function($)
	{
		var source = (Alfresco.constants.PROXY_URI + 'countersign/signaturetimeline?nodeRef=' + '${nodeRef?js_string}');
		
	    $(document).ready(function() {
			createStoryJS({
				type:			'timeline',
				width:			'100%',
				height:			'400',
				source:			source,
				embed_id:		'${el}-signature-timeline',
				start_at_slide: '1',
				debug:			true,
				css:        	'${page.url.context}/res/countersign/components/thirdparty/TimelineJS/css/timeline.css',     
	            js:         	'${page.url.context}/res/countersign/components/thirdparty/TimelineJS/js/timeline-min.js'
			});
		});
	})(jQuery);
 	//]]></script>
</div>