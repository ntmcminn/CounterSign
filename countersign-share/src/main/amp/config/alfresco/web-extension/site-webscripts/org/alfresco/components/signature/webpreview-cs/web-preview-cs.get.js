<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getDocumentNode(nodeRef, defaultValue)
{

   var metadata = AlfrescoUtil.getMetaData(nodeRef, {});
   if (metadata.properties)
   {
      var node = {},
         mcns = "{http://www.alfresco.org/model/content/1.0}",
         content = metadata.properties[mcns + "content"];

      node.name = metadata.properties[mcns + "name"];
      node.mimeType = metadata.mimetype;
      if (content)
      {
         var size = content.substring(content.indexOf("size=") + 5);
         size = size.substring(0, size.indexOf("|"));
         node.size = size;
      }
      else
      {
         node.size = "0";
      }
      node.thumbnails = AlfrescoUtil.getThumbnails(nodeRef);
      return node;
   }
   else
   {
      return defaultValue;
   }
}

function main()
{
   // Populate model with parameters
   AlfrescoUtil.param("nodeRef");

   // Populate model with data from repo
   var documentNode = getDocumentNode(model.nodeRef, null);
   if (documentNode)
   {
      // Populate model with data from node and config
      model.node = documentNode;
   }
}

// Start the webscript
main();