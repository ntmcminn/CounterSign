/*
 * Copyright 2012-2013 Alfresco Software Limited.
 * 
 * Licensed under the GNU Affero General Public License, Version 3.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.gnu.org/licenses/agpl-3.0.html
 * 
 * If you do not wish to be bound to the terms of the AGPL v3.0, 
 * A commercial license may be obtained by contacting the author.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 * 
 */

/**
 * CounterSign top-level namespace.
 *
 * @namespace CounterSign
 * @class CounterSign.Signatures
 */

if(typeof CounterSign == "undefined" || !CounterSign)
{
	var CounterSign = {};
}

/**
 * CounterSign util namespace
 * 
 * @namespace CounterSign
 * @class CounterSign.util
 */
CounterSign.util = CounterSign.util || {};
CounterSign.util.JQuery = CounterSign.util.JQuery || {};

/**
 * CounterSign validation namespace
 * 
 * @namespace CounterSign
 * 
 */
CounterSign.validation = CounterSign.validation || {};


(function()
{

	/**
	 * Alfresco Slingshot aliases
	 */
	var $userProfileLink = Alfresco.util.userProfileLink,
	$userAvatar = Alfresco.Share.userAvatar;

	/**
	 * SignatureView constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {CounterSign.Signatures} The new component instance
	 * @constructor
	 */
	CounterSign.SignatureView = function SignatureView_constructor(type, htmlId)
	{
		CounterSign.SignatureView.superclass.constructor.call(this, type, htmlId, ["datasource", "datatable", "animation"]);
		return this;
	};

	YAHOO.extend(CounterSign.SignatureView, Alfresco.component.Base,
	{
		/**
		 * Object container for initialization options
		 *
		 * @property options
		 * @type {object} object literal
		 */
		options:
		{
			/**
			 * Reference to the signed document
			 *
			 * @property nodeRef
			 * @type string
			 */
			nodeRef: null,
		},


		/**
		 * Builds and returns the markup for an individual signature.
		 *
		 * @method getSignatureInstanceMarkup
		 * @param sig {Object} The details for the signature
		 */
		getSignatureInstanceMarkup: function Signatures_getSignatureInstanceMarkup(sig)
		{
			html = "";
			html += "<div class='signature-panel'>";
			html += "<div class='signature-details-left'>";
			html += $userAvatar(sig.signedByUserName, 64);
			html += "</div>";
			html += "<div class='signature-details-right'>";
			html += "Signed By: " + $userProfileLink(sig.signedByUserName, sig.signedByFirstName + " " + sig.signedByLastName, "class='theme-color-1'") + " </br>";
			html += "User ID: " + sig.signedByUserName + " </br>";
			html += "Reason: " + sig.reason + " </br>";
			html += "Location: " + sig.location + " </br>";
			html += "Date Signed: " + sig.signatureDate + " </br>";
			html += "</div>";
			html += "<div class='clear'></div>";
			return html;
		}
	});
})();

//CounterSign configuration object
/**
 * CounterSign.util.Config loads countersign-config.properties, and makes the extension
 * settings available to other client side JavaScript components.
 *
 * @namespace CounterSign
 * @class CounterSign.util.Config
 */
(function()
{

	var Dom = YAHOO.util.Dom,
	Event = YAHOO.util.Event;

	CounterSign.util.UI = function()
	{
		var hideDependentControls = function CounterSign_util_UI_hideDependentControls(element)
		{
			// get the field html id
			var fieldHtmlId = element.id
			// set the value of the hidden field
			var value = YAHOO.util.Dom.get(fieldHtmlId).checked;
			YAHOO.util.Dom.get(fieldHtmlId + "-hidden").value = value;
			// find and hide the dependent controls
			var controls = YAHOO.util.Dom.get(fieldHtmlId + "-tohide").value.split(",");

			for(index in controls)
			{
				var module = new YAHOO.widget.Module((controls[index] + "-control"));
				if(value == true)
				{
					module.show();
				}
				else
				{
					module.hide();
				}
			}
		};
		
		return this;
	};
	
	CounterSign.validation.PasswordStrength = function(field, args, event, form, silent, message)
	{
		CounterSign.util.JQuery.FormHelper = form;
		
		if (Alfresco.logger.isDebugEnabled())
			Alfresco.logger.debug("Validating state of CounterSign password field '" + field.id + "'");

		if(field.value !== "")
	    {
			return true;
	    }
		else
		{
			return false;
		}
	};
})();

//CounterSign utility object
function hideDependentControls(element)
{
	// get the field html id
	var fieldHtmlId = element.id;
	// set the value of the hidden field
	var value = YAHOO.util.Dom.get(fieldHtmlId).checked;
	YAHOO.util.Dom.get(fieldHtmlId + "-hidden").value = value;
	// find and hide the dependent controls
	var controls = YAHOO.util.Dom.get(fieldHtmlId + "-tohide").value.split(",");

	for(index in controls)
	{
		var module = new YAHOO.widget.Module((controls[index] + "-control"));
		if(value == true)
		{
			module.show();
		}
		else
		{
			module.hide();
		}
	}
}
