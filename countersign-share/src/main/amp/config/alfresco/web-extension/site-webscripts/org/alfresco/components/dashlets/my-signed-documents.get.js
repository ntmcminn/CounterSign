<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getFilters(filterType)
{
   var myConfig = new XML(config.script),
       filters = [];

   for each (var xmlFilter in myConfig[filterType].filter)
   {
      filters.push(
      {
         type: xmlFilter.@type.toString(),
         label: xmlFilter.@label.toString()
      });
   }

   return filters;
}

function getMaxItems()
{
   var myConfig = new XML(config.script),
      maxItems = myConfig["max-items"];

   if (maxItems)
   {
      maxItems = myConfig["max-items"].toString();
   }
   return parseInt(maxItems && maxItems.length > 0 ? maxItems : 50, 10);
}

model.preferences = AlfrescoUtil.getPreferences("org.alfresco.extension.countersign.dashlet.signed");
model.maxItems = getMaxItems();
model.filterRanges = getFilters("filter-range");