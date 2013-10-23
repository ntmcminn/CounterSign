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
 * PDF digital signatures history view component.
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
    * Signatures constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {CounterSign.Signatures} The new component instance
    * @constructor
    */
   CounterSign.SignatureMap = function SignatureMap_constructor(htmlId)
   {
      CounterSign.SignatureMap.superclass.constructor.call(this, "CounterSign.SignatureMap", htmlId);
      return this;
   };

   YAHOO.extend(CounterSign.SignatureMap, CounterSign.SignatureView,
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
      
      map: null,
      
      locations: [],
      
      /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function SignatureMap_onReady()
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
               container: this.id + "-signature-map",
               columnDefinitions:
               [
                  { key: "signature", sortable: false, formatter: this.bind(this.renderCellSignatureMap) }
               ],
               config:
               {
                  MSG_EMPTY: this.msg("message.noSigntures")
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
         
         // initialize map view and render the map as soon as the div for it is in place
         this.map = L.map((this.id + "-signature-map"));
         L.tileLayer(CounterSign.util.Config.properties["countersign.leaflet.mapurl"], {
        	 attribution: CounterSign.util.Config.properties["countersign.leaflet.attribution"],
        	 maxZoom: 18
         }).addTo(this.map);
      },

      /**
       * Signature item renderer
       *
       * @method renderCellSignatureMap
       */
      renderCellSignatureMap: function SignatureMap_renderCellSignatureMap(elCell, oRecord, oColumn, oData)
      {
    	 var sigHtml = this.getSignatureInstanceMarkup(oRecord.getData());
         var sig = oRecord.getData();
         //this.map.setView([sig.signatureLat, sig.signatureLong], 13);
         var loc = new L.LatLng(sig.signatureLat, sig.signatureLong);
         var marker = L.marker(loc).addTo(this.map);
         this.locations.push(loc);
         var bounds = new L.LatLngBounds(this.locations);
         this.map.fitBounds(bounds);
         marker.bindPopup(sigHtml);
      }
   });
})();
