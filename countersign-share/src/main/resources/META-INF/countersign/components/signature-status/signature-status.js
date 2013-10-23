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

(function()
{
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom,
	Event = YAHOO.util.Event;
	
	/**
	 * Alfresco Slingshot aliases
	 */
	var $userProfileLink = Alfresco.util.userProfileLink;

	/**
	 * SignatureView constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {CounterSign.Signatures} The new component instance
	 * @constructor
	 */
	CounterSign.SignatureStatus = function SignatureStatus_constructor(htmlId)
	{
		CounterSign.SignatureStatus.superclass.constructor.call(this, "CounterSign.SignatureStatus", htmlId, ["datasource", "datatable", "animation"]);
		return this;
	};

	YAHOO.extend(CounterSign.SignatureStatus, Alfresco.component.Base,
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
			nodeRef: null
		},

		/**
		 * Fired by YUI when parent element is available for scripting
		 *
		 * @method onReady
		 */
		onReady: function SignatureStatus_onReady()
		{
			this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
			{
				dataSource:
				{
					url: Alfresco.constants.PROXY_URI + "countersign/signatureStatus?nodeRef=" + this.options.nodeRef,
					doBeforeParseData: this.bind(function(oRequest, oFullResponse)
					{  
						return (
						{
							"data" : oFullResponse
						});
					})
				},
				dataTable:
				{
					container: this.id + "-status",
					columnDefinitions:
					[
					 	{ key: "signatureValid", label: "Signature Valid", sortable: false, formatter: this.bind(this.renderSignatureCell) },
					 	{ key: "hashValid", label: "Hash Valid", sortable: false, formatter: this.bind(this.renderHashCell)},
					 	{ key: "signer", label: "Signer", sortable: false, formatter: this.bind(this.renderSignerCell)},
					 	{ key: "sigDate", label: "Signature Date", sortable: false, formatter: this.bind(this.renderSigDateCell)}
					],
					config:
					{
						MSG_EMPTY: this.msg("message.noSignatures"),
						className: "countersign-datatable"
					}
				}
			});

			// Resize event handler - adjusts the filename container DIV to a size relative to the container width
			Event.addListener(window, "resize", function() 
			{ 
				var width = (Dom.getViewportWidth() * 0.25) + "px",
				nodes = YAHOO.util.Selector.query("h3.thin", this.id + "-body");
				for (var i=0; i<nodes.length; i++)
				{
					nodes[i].style.width = width;
				}
			}, this, true);
		},

		/**
		 * Signature validity indicator
		 *
		 * @method renderSignatureCell
		 */
		renderSignatureCell: function SignatureStatus_renderSignatureCell(elCell, oRecord, oColumn, oData)
		{
			var sigHtml = "";
			var data = oRecord.getData();
			var img = this.getIndicator(data.signatureValid);
			sigHtml += "<img src='" + Alfresco.constants.URL_RESCONTEXT + "/countersign/img/" + img + "'/>";
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Render hash validity indicator
		 * 
		 * @method renderHashCell
		 */
		renderHashCell: function SignatureStatus_renderHashCell(elCell, oRecord, oColumn, oData)
		{
			var sigHtml = "";
			var data = oRecord.getData();
			var img = this.getIndicator(data.hashValid);
			sigHtml += "<img src='" + Alfresco.constants.URL_RESCONTEXT + "/countersign/img/" + img + "'/>";
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Render the info about the signer
		 * 
		 * @method renderSignerCell
		 */
		renderSignerCell: function SignatureStatus_renderSignerCell(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var sigHtml = $userProfileLink(data.signedByUserName, data.signedByFirstName + ' ' + data.signedByLastName, 'class="theme-color-1"');
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Render the info about the signature date
		 * 
		 * @method renderSigDateCell
		 */
		renderSigDateCell: function SignatureStatus_renderSigDateCell(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var sigHtml = data.signatureDate;
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Render the info about the signature reason
		 * 
		 * @method renderReasonCell
		 */
		renderReasonCell: function SignatureStatus_renderReasonCell(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var sigHtml = data.reason;
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Render the info about the signature location
		 * 
		 * @method renderLocationCell
		 */
		renderLocationCell: function SignatureStatus_renderLocationCell(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var sigHtml = data.location;
			elCell.innerHTML = sigHtml;
		},
		
		/**
		 * Get the appropriate validity indicator for the signature status
		 * 
		 * @param valid
		 * @returns {String}
		 */
		getIndicator: function SignatureStatus_getIndicator(valid)
		{
			if(valid)
			{
				return "valid.png";
			}
			else
			{
				return "invalid.png";
			}
		}
	});
})();