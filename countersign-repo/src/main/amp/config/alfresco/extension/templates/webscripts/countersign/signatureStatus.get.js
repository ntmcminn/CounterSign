function getSignatures()
{
	var doc = search.findNode(args["nodeRef"]);

	// add signature metadata to model if signed
	if(doc.hasAspect("csign:signed"))
	{
		// get the associations to existing signatures
		var signatures = doc.childAssocs["csign:signatures"];
		model.isSigned = true;
		return signatures;
	}	
}

function main()
{
	var signatures = getSignatures();
	var rows = [];
	for(index in signatures)
	{
		var row = {};
		row.signature = signatures[index];
		
		// get the validation status for this row
		status = countersign.validateSignature(signatures[index].nodeRef);
		row.status = status;
		rows.push(row);
	}
	model.rows = rows;
}

main();