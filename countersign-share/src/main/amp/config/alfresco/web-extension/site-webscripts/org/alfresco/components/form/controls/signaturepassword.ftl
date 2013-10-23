<!-- 
Signature password control.  Checks to see if user has valid keystore.  If not, shows
a password creation dialog, two password boxes, checks for match between both and checks
for password strength.  If user already has a keystore, shows a single password box
for entry.  May at some point in the future also serve as the place to put the "forgot key 
password, regenerate key" options
 -->
<script type="text/javascript" src="${url.context}/res/countersign/components/thirdparty/PasswordStrength/password-strength.js"></script>
<link rel="stylesheet" type="text/css" href="${url.context}/res/countersign/components/thirdparty/PasswordStrength/password-strength.css" />
<div id="${field.name}-control">
	<input id="${fieldHtmlId}" name="${field.name}" type="hidden" value="" />
	<div class="form-field" id="${fieldHtmlId}-passwordcomponent">
		<label for="${fieldHtmlId}-password">${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
		<input id="${fieldHtmlId}-password" tabindex="0" type="password" />
	</div>
	<div class="form-field" id="${fieldHtmlId}-confirmpasswordcomponent">
		<label for="${fieldHtmlId}-confirmpassword">Confirm ${field.label?html}:<#if field.mandatory><span class="mandatory-indicator">${msg("form.required.fields.marker")}</span></#if></label>
    	<input id="${fieldHtmlId}-confirmpassword" tabindex="0" type="password" />
    </div>
    <script type="text/javascript">//<![CDATA[
    (function($) 
    {          
    	// get the user's signature state.  If they need to create a new key, enable
    	// the password strength meter and show the "confirm password" box.
    	$(document).ready(function() {
    		$.ajax({
				  url: Alfresco.constants.PROXY_URI + "countersign/signature/userkeystore",
				  context: document.body
    		}).done(function(userInfo){
    			$("#${fieldHtmlId}").passStrength({
			    	shortPass: 				"top_shortPass",	
			    	badPass:				"top_badPass",		
			    	goodPass:				"top_goodPass",		
			    	strongPass:				"top_strongPass",
			    	noPass:					"top_noPass",	
			    	baseStyle:				"top_testresult",	
			    	messageloc:				1,
			    	confirm:				!userInfo.signatureAvailable,
					minLength:				6,
					goodScore:				34,
					strongScore:			68,
					onChangeCallback:		CounterSign.validation.PasswordStrength
			    });
    		});
    	});
    })(jQuery);
	//]]></script>
</div>

