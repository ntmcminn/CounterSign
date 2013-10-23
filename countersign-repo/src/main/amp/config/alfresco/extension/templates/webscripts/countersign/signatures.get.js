function main()
{
	var doc = search.findNode(args["nodeRef"]);

	// add signature metadata to model if signed
	if(doc.hasAspect("csign:signed"))
	{
		// get the associations to existing signatures
		var signatures = doc.childAssocs["csign:signatures"];

		model.isSigned = true;
		model.signatures = signatures;
	}	
}

main();