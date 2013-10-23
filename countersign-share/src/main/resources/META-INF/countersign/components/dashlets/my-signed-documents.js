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
	 * Preferences
	 */
	var PREFERENCES_MYSIGNEDDOCS_DASHLET = "org.alfresco.extension.countersign.dashlet.signed";
	var PREFERENCES_MYSIGNEDDOCS_DASHLET_FILTER = PREFERENCES_MYSIGNEDDOCS_DASHLET + ".filter";
	      
	/**
	 * CounterSign SignedDocuments Dashlet constructor.
	 * 
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {CounterSign.dashlet.SignedDocuments} The new component instance
	 * @constructor
	 */
	CounterSign.dashlet.SignedDocuments = function SignedDocuments_constructor(htmlId)
	{
		CounterSign.dashlet.SignedDocuments.superclass.constructor.call(this, "CounterSign.dashlet.SignedDocuments", htmlId, ["datasource", "datatable", "animation"]);
		return this;
	};

	YAHOO.extend(CounterSign.dashlet.SignedDocuments, Alfresco.component.Base,
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
		onReady: function SignedDocuments_onReady()
		{
			
			// get the container for the artifacts list
			this.list = Dom.get(this.id + "-documents");
			
			// create a reset button for these artifacts
			this.widgets.range = Alfresco.util.createYUIButton(this, "range", this.onFilterChanged,
			{
	            type: "menu",
	            menu: "range-menu",
	            lazyloadmenu: false
	        });
			
			var filter = this.options.filter;
	        filter = Alfresco.util.arrayContains(this.options.validFilters, filter) ? filter : this.options.validFilters[0];
	        this.widgets.range.set("label", this.msg("filter." + filter));
	        this.widgets.range.value = filter;
	        
	        this.reloadDataTable();
	        
		},
		
		/**
		 * Filter Change menu handler
		 *
		 * @method onFilterChange
		 * @param p_sType {string} The event
		 * @param p_aArgs {array}
		 */
		onFilterChanged: function SignedDocuments_onFilterChange(p_sType, p_aArgs)
		{
			var menuItem = p_aArgs[1];
			if (menuItem)
			{
				this.widgets.range.set("label", menuItem.cfg.getProperty("text"));
				this.widgets.range.value = menuItem.value;

				//TODO get preferences persistence working
				//this.services.preferences.set(PREFERENCES_MYSIGNEDDOCS_DASHLET_FILTER, this.widgets.range.value);

				this.reloadDataTable();
			}
		},

		/**
		 * Load failed
		 * @method onLoadFailed
		 */
		onLoadFailed: function SignedDocuments_onLoadFailed()
		{
			// set message in dashlet indicating that load failed
		},
		
		/**
		 * Generate base webscript url.
		 * Can be overridden.
		 *
		 * @method getWebscriptUrl
		 */
		getWebscriptUrl: function SignedDocuments_getWebscriptUrl()
		{
			return Alfresco.constants.PROXY_URI + "countersign/signeddocuments?maxDocs=50&range=" + this.widgets.range.value;
		},
		
		/**
		 * Render an individual signed document in the list
		 * 
		 * @method renderDocumentListItem
		 */
		renderDocumentListItem: function SignedDocuments_renderDocumentListItem(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var docHtml = "";
			docHtml += "<div class='signed-documents-panel'>";
			docHtml += "<div class='signed-documents-panel-left'>";
			docHtml += "<img src='" + Alfresco.constants.PROXY_URI + "api/node/" + data.nodeRef.replace(":/", "") + "/content/thumbnails/doclib?c=queue&ph=true'/>";
			docHtml += "</div>";
			docHtml += "<div class='signed-documents-panel-right'>";
			docHtml += "<h3 class='filename simple-view'><a class='theme-color-1' href='" + Alfresco.util.siteURL("view-signatures?nodeRef=" + data.nodeRef, {}, false) + "'>" + data.documentName + "</a></h3>";
			docHtml += "Signed On: " + data.signatureDate;
			docHtml += "</div>";
			docHtml += "</div>";
			
	        elCell.innerHTML = docHtml;
		},
		
		/**
		 * Reloads the data table used to display the document list
		 */
		reloadDataTable: function SignedDocuments_reloadDataTable()
		{
			this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
            {
               dataSource:
               {
                  url: this.getWebscriptUrl(),
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
                  container: this.id + "-documents",
                  columnDefinitions:
                  [
                     { key: "document", sortable: false, formatter: this.bind(this.renderDocumentListItem) }
                  ],
                  config:
                  {
                     MSG_EMPTY: this.msg("message.noDocuments")
                  }
               }
            });
		}
	});
})();
