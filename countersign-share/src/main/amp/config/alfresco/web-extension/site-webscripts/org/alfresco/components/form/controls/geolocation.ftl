<!-- 
	geolocation control uses HTML5/JS to get user location, passes this back to the server as a 
	lat / long, comma delimited
-->

<div class="form-field">
	<input id="${fieldHtmlId}" type="hidden" name="${field.name}" value="" />
	<#if !field.disabled>
	<script type="text/javascript">//<![CDATA[
    var geolocation = function()
    {
    	
    	var field = YAHOO.util.Dom.get("${fieldHtmlId}");
    	
    	function callback(position)
      	{
    		var field = YAHOO.util.Dom.get("${fieldHtmlId}");
    		field.value = position.coords.latitude + "," + position.coords.longitude
      	}
    	
    	function error(error)
    	{
    		var field = YAHOO.util.Dom.get("${fieldHtmlId}");
    		
    	  	switch(error.code) 
    	  	{
    	    	case error.PERMISSION_DENIED:
    	      		field.value = "NOLOCATION: User denied the request for geolocation."
    	      		break;
    	    	case error.POSITION_UNAVAILABLE:
    	    		field.value = "NOLOCATION: Location information is unavailable."
    	    	  	break;
    	    	case error.TIMEOUT:
    	    		field.value = "NOLOCATION: The request to get user location timed out."
    	      		break;
    	    	case error.UNKNOWN_ERROR:
    	    		field.value = "NOLOCATION: An unknown error occurred."
    	      		break;
    	    }
    	 }
    	 
		
      	if (navigator.geolocation)
        {
        	navigator.geolocation.getCurrentPosition(callback, error);
        }
      	else
      	{
      		var field = YAHOO.util.Dom.get("${fieldHtmlId}");
      		field.value = "NOLOCATION: Geolocation is not supported by this browser.";
      	}
    }();
		//]]></script>
	</#if>
</div>
