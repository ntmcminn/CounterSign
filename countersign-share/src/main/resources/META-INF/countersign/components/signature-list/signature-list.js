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
 * CounterSign signature history list view component.
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
    * SignatureList constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {CounterSign.SignatureList} The new component instance
    * @constructor
    */
   CounterSign.SignatureList = function SignatureList_constructor(htmlId)
   {
      CounterSign.SignatureList.superclass.constructor.call(this, "CounterSign.SignatureList", htmlId);
      return this;
   };

   YAHOO.extend(CounterSign.SignatureList, CounterSign.SignatureView,
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
          * Reference to the current document
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
      onReady: function SignatureList_onReady()
      {
         this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
         {
            dataSource:
            {
               url: Alfresco.constants.PROXY_URI + "countersign/signatures?nodeRef=" + this.options.nodeRef,
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
               container: this.id + "-signatures",
               columnDefinitions:
               [
                  { key: "signature", sortable: false, formatter: this.bind(this.renderCellSignature) }
               ],
               config:
               {
                  MSG_EMPTY: this.msg("message.noSignatures")
               }
            }
         });
         
         // Resize event handler - adjusts the filename container DIV to a size relative to the container width
         Event.addListener(window, "resize", function() 
         { 
            var width = (Dom.getViewportWidth() * 0.25) + "px",
                nodes = YAHOO.util.Selector.query('h3.thin', this.id + "-body");
            for (var i=0; i<nodes.length; i++)
            {
               nodes[i].style.width = width;
            }
         }, this, true);
      },

      /**
       * Signature item renderer
       *
       * @method renderCellSignatures
       */
      renderCellSignature: function SignatureList_renderCellSignatures(elCell, oRecord, oColumn, oData)
      {
    	 var sigHtml = this.getSignatureInstanceMarkup(oRecord.getData());
         elCell.innerHTML = sigHtml;
      },
   });
})();
