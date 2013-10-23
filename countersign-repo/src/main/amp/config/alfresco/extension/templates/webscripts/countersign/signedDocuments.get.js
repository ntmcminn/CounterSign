function main()
{
  var items = [];
  
  // get the current user id
  var userId = person.properties.userName;
  
  // get the range
  var range = args["range"];
  
  // get the start date for the query
  var start = calculateStartDate(range);
  
  // get all signatures created by this user
  //var query = '+TYPE:"csign:signature" +@cm\\:csign:externalSigner\\:"' + userId + '" +@csign\\:signatureDate:[' + start + ' TO MAX]';
  var query = '+TYPE:"csign:signature"';
	  
  // add the date range filter
  if(range !== "none")
  {
	  query = query + "+@csign\\:signatureDate:[" + start + " TO MAX]";
  }
 
  // execute the search
  var results = search.luceneSearch(query);
  
  for each (result in results)
  {
	  var signedDoc = {};
      // get the document that was signed and add to the signed docs list
      signedDoc.document = result.parentAssocs["csign:signatures"][0];
      signedDoc.signature = result;
      items.push(signedDoc);
  }
  
  model.items = items;
}

function calculateStartDate(range)
{
	var start = new Date;
	if(range !== "today")
	{
		start.setDate(start.getDate() - range);
	}
	
	var day = start.getDate();
    var month = start.getMonth() + 1;
    var year = start.getFullYear();
    return (year + "\-" + month + "\-" + day);
}

main();
