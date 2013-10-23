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
 * PDF digital signature position component.
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
	Event = YAHOO.util.Event,
	Selector = YAHOO.util.Selector;
	
	/**
	 * SignatureView constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {CounterSign.Signatures} The new component instance
	 * @constructor
	 */
	CounterSign.SignaturePosition = function SignaturePosition_constructor(htmlId)
	{
		CounterSign.SignaturePosition.superclass.constructor.call(this, "CounterSign.SignaturePosition", htmlId, []);
		return this;
	};

	YAHOO.extend(CounterSign.SignaturePosition, Alfresco.component.Base,
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
			
			/**
			 * Only use as a placeholder for drawn position?
			 *
			 * @property placeholderOnly
			 * @type boolean
			 */
			placeholderOnly: false
		},

		position:
		{
			type:"predefined",
			position:"center",
			page:"1",
			box: {startX:"0",startY:"0",endX:"0",endY:"0"}
		},
		
		// set up YUI modules for the three types of positions
		signatureFieldModule: null,
		predefinedPositionModule: null,
		drawnPositionModule: null,
		
		onReady: function SignauturePosition_onReady()
		{
			if(!this.options.placeholderOnly)
			{
				// get the available signature fields for this doc, if available
				this.getSignatureFields(this.options.nodeRef);
				
				// get the page count for this doc
				this.getPageCount(this.options.nodeRef);
				
				// set up YUI modules for the three types of positions
				this.signatureFieldModule = new YAHOO.widget.Module(this.id + "-signatureFields");
				this.predefinedPositionModule = new YAHOO.widget.Module(this.id + "-predefinedPositions");
				this.drawnPositionModule = new YAHOO.widget.Module(this.id + "-drawnPosition");
				
				// set listeners for radio buttons
				YAHOO.util.Event.addListener([this.id + "-signatureFieldOption"], "click", this.showPositionOptions, this);
				YAHOO.util.Event.addListener([this.id + "-predefinedLocationOption"], "click", this.showPositionOptions, this);
				YAHOO.util.Event.addListener([this.id + "-drawnPositionOption"], "click", this.showPositionOptions, this);
				
				//set listeners for select boxes
				YAHOO.util.Event.addListener([this.id + "-fieldSelect"], "change", this.onFieldSelectChange, this);
				YAHOO.util.Event.addListener([this.id + "-positionSelect"], "change", this.onPositionSelectChange, this);
				YAHOO.util.Event.addListener([this.id + "-pageSelect"], "change", this.onPageSelectChange, this);	
			}
			else
			{
				this.position.type = "drawn";
			}
		},
		
		// function to show or hide position options based on radiobutton values
		showPositionOptions: function SignaturePosition_showPositionOptions(event, that)
		{
			var target = event.target || event.srcElement;
			if(event.target.value == "signatureField")
			{
				that.drawnPositionModule.hide();
				that.predefinedPositionModule.hide();
				that.signatureFieldModule.show();
				that.onFieldSelectChange(event, that);
			}
			else if (event.target.value == "drawnPosition")
			{
				that.signatureFieldModule.hide();
				that.predefinedPositionModule.hide();
				that.drawnPositionModule.show();
				that.onDrawnPositionSelect(event, that);
			}
			else 
			{
				that.signatureFieldModule.hide();
				that.drawnPositionModule.hide();
				that.predefinedPositionModule.show();
				that.onPositionSelectChange(event, that);
			}
		},
	
		onFieldSelectChange: function SignaturePosition_onFieldSelectChange(event, that)
		{
			var selected = YAHOO.util.Dom.get(that.id + "-fieldSelect").value;
			that.position.type = "field";
			that.position.position = selected;
			that.setPosition(that);
		},
	
		onPositionSelectChange: function SignaturePosition_onPositionSelectChange(event, that)
		{
			var selectedPosition = YAHOO.util.Dom.get(that.id + "-positionSelect").value;
			that.position.type = "predefined";
			that.position.position = selectedPosition;
			that.setPosition(that);
		},
		
		onPageSelectChange: function SignaturePosition_onPageSelectChange(event, that)
		{
			var page = YAHOO.util.Dom.get(that.id + "-pageSelect").value;
			that.position.page = page;
			that.setPosition(that);
		},
		
		onDrawnPositionSelect: function SignaturePosition_onDrawnPositionSelect(event, that)
		{
			that.position.type = "drawn";
			that.setPosition(that);
		},
		
		setPosition: function SignaturePosition_setPosition(that)
		{
			YAHOO.util.Dom.get(that.id).value = JSON.stringify(that.position);
		},

		getSignatureFields: function SignaturePosition_getSignatureFields(nodeRef)
		{
			Alfresco.util.Ajax.jsonGet(
				{
					url: (Alfresco.constants.PROXY_URI + "countersign/signaturefields?nodeRef=" + nodeRef),
					successCallback:
					{
						fn: function(response)
						{
							var sigSelect = YAHOO.util.Dom.get(this.id + "-fieldSelect");
							var fields = response.json.unusedSignatureFields;
							if(fields && fields.length > 0)
							{
								sigSelect.options.length=0;
								for (var index in fields)
								{
									var opt = document.createElement('option');
									opt.text = fields[index];
									opt.value = fields[index];
									sigSelect.add(opt, null);
								}
							}
						},
						scope: this
					},
					failureCallback:
					{
						fn: function(response)
						{
							Alfresco.util.PopupManager.displayMessage(
								{
									text: "Could not retreive signature fields"
								}
							);
						}
					}
				});
		},

		getPageCount: function getPageCount(nodeRef)
		{
			Alfresco.util.Ajax.jsonGet(
				{
					url: (Alfresco.constants.PROXY_URI + "countersign/pagecount?nodeRef=" + nodeRef),
					successCallback:
					{
						fn: function(response)
						{
							var pageSelect = YAHOO.util.Dom.get(this.id + "-pageSelect");
							var pages = parseInt(response.json.pageCount);
							if(pages > 0)
							{
								for(var i = 1;i < pages + 1; i++)
								{
									var opt = document.createElement("option");
									opt.text = i;
									opt.value = i;
									pageSelect.add(opt, null);
								}
							}
						},
						scope: this
					},
					failureCallback:
					{
						fn: function(response)
						{
							Alfresco.util.PopupManager.displayMessage(
								{
									text: "Could not retreive page count"
								}
							);
						}
					}
				});
		}
	});
})();