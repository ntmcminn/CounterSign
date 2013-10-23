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
 * CounterSign Dashlet component
 */

if(typeof CounterSign == "undefined" || !CounterSign)
{
	var CounterSign = {};
}

if(typeof CounterSign.dashlet == "undefined" || !CounterSign.dashlet)
{
	CounterSign.dashlet = {};
}

(function()
{
	/**
	 * CounterSign SignatureArtifacts Dashlet constructor.
	 * 
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {CounterSign.dashlet.SignatureArtifacts} The new component instance
	 * @constructor
	 */
	CounterSign.dashlet.SignatureArtifacts = function SignatureArtifacts_constructor(htmlId)
	{
		CounterSign.dashlet.SignatureArtifacts.superclass.constructor.call(this, "CounterSign.dashlet.SignatureArtifacts", htmlId, ["button", "container", "calendar"]);
		return this;
	};

	YAHOO.extend(CounterSign.dashlet.SignatureArtifacts, Alfresco.component.Base,
	{
		/**
		 * Object container for initialization options
		 *
		 * @property options
		 * @type object
		 */
		options:
		{

		},

		/**
		 * Artifact list container.
		 * 
		 * @property list
		 * @type object
		 */
		list: null,
	      
		/**
		 * Fired by YUI when parent element is available for scripting
		 * @method onReady
		 */
		onReady: function SignatureArtifacts_onReady()
		{
			var me = this;
			
			// get the container for the artifacts list
			this.list = Dom.get(this.id + "-list");
			
			// create a reset button for these artifacts
			this.widgets.reset = Alfresco.util.createYUIButton(this, "reset", this.resetKeys);
			this.widgets.reset.set("label", this.msg("action.resetItems"));

			this.load();
		},

		/**
		 * Reset the user's signing keys and signature image
		 */
		resetKeys: function SignatureArtifacts_resetKeys()
		{
			// now load the artifacts for display, with download links
			// Load the activity list
			Alfresco.util.Ajax.request(
			{
				url: Alfresco.constants.PROXY_URI + "/countersign/user/signatureartifacts",
				method:Alfresco.util.Ajax.DELETE,
				successCallback:
				{
					fn: this.onReset,
					scope: this,
				},
				failureCallback:
				{
					fn: this.resetFailed,
					scope: this
				},
				scope: this,
				noReloadOnAuthFailure: true
			});
		},
		
		/**
		 * Loaded successfully
		 * @method onLoaded
		 * @param p_response {object} Response object from request
		 */
		onLoaded: function SignatureArtifacts_onLoaded(response, obj)
		{
			var html = response.serverResponse.responseText;
	        if (YAHOO.lang.trim(html).length === 0 || !response.json.signatureAvailable)
	        {
	        	this.list.innerHTML = '<div class="empty-artifact-list">' + this.msg("message.load.notFound") + '</div>';
	        }
	        else
	        {
	        	var artifactsHtml = "";
	        	artifactsHtml += "<div class='artifact-list'>";
	        	if(response.json.keystoreNode)
	        	{
	        		artifactsHtml += "<div class='artifact-list-item'>";
	        		artifactsHtml += "<a href='" + Alfresco.util.contentURL(response.json.keystoreNode, "keystore.p12", true) + "'>";
	        		artifactsHtml += "<img src='" + Alfresco.constants.URL_RESCONTEXT + "/countersign/img/keystore.png' class='artifact-image'/>";
	        		artifactsHtml += this.msg("label.keystore");
	        		artifactsHtml += "</a>";
	        		artifactsHtml += "</div>";
	        	}
	        	if(response.json.publicKeyNode)
	        	{
	        		artifactsHtml += "<div class='artifact-list-item'>";
	        		artifactsHtml += "<a href='" + Alfresco.util.contentURL(response.json.publicKeyNode, "publicKey.pem", true) + "'>";
	        		artifactsHtml += "<img src='" + Alfresco.constants.URL_RESCONTEXT + "/countersign/img/certificate.png' class='artifact-image'/>";
	        		artifactsHtml += this.msg("label.publicKey");
	        		artifactsHtml += "</a>";
	        		artifactsHtml += "</div>";
	        	}
	        	if(response.json.imageNode)
	        	{
	        		artifactsHtml += "<div class='artifact-list-item'>";
		        	artifactsHtml += "<a href='" + Alfresco.util.contentURL(response.json.imageNode, "signature_image.png", true) + "'>";
		        	artifactsHtml += "<img src='" + Alfresco.constants.URL_RESCONTEXT + "/countersign/img/signature-image.png' class='artifact-image'/>";
		        	artifactsHtml += this.msg("label.image");
	        		artifactsHtml += "</a>";
	        		artifactsHtml += "</div>";
	        	}
	        	artifactsHtml += "</div>";
	        	this.list.innerHTML = artifactsHtml;
	        }
		},

		/**
		 * Load failed
		 * @method onLoadFailed
		 */
		onLoadFailed: function SignatureArtifacts_onLoadFailed()
		{
        	var artifactsHtml = "<div class='empty-artifact-list'>" + this.msg("message.load.error") + "</div>";
        	this.list.innerHTML = artifactsHtml;
		},
		
		/**
		 * Key reset successful
		 * @method onReset
		 */
		onReset: function SignatureArtifacts_onReset()
		{
			 Alfresco.util.PopupManager.displayMessage(
             {
            	 text: this.msg("message.reset.success")
             });
			 this.load();
		},
		
		/**
		 * Key reset operation failed
		 * @method onResetFailed
		 */
		onResetFailed: function SignatureArtifacts_onResetFailed()
		{
			 Alfresco.util.PopupManager.displayMessage(
		     {
		    	 text: this.msg("message.reset.failure")
		     });
		},
		
		/**
		 * Load the signature artifacts (keystore and sig image) from the repo
		 */
		load: function SignatureArtifacts_load()
		{
			// now load the artifacts for display, with download links
			// Load the activity list
			Alfresco.util.Ajax.request(
			{
				url: Alfresco.constants.PROXY_URI + "countersign/signature/userkeystore",
				dataObj:
				{

				},
				successCallback:
				{
					fn: this.onLoaded,
					scope: this,
				},
				failureCallback:
				{
					fn: this.onLoadFailed,
					scope: this
				},
				scope: this,
				noReloadOnAuthFailure: true
			});
		}
	});
})();
