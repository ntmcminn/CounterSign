function main()
{
	var doc = search.findNode(args["nodeRef"]);
	var allSignatureFields = doc.properties["csign:signatureFields"];
	// get the associations to existing signatures
	var signatures = doc.childAssocs["csign:signatures"];
	
	// add signable form field info to model
	if(doc.hasAspect("csign:signable"))
	{
		// get the signature fields and add to the doc
		model.allSignatureFields = allSignatureFields
		var unusedSignatureFields = allSignatureFields.slice(0);
		
		for(index in signatures)
		{
			// if the sig field has already been signed, remove the sig field from the list
          	var indexOfSig = allSignatureFields.indexOf(signatures[index].properties["csign:signatureField"]);
			if(indexOfSig > -1)
			{
				unusedSignatureFields.splice(indexOfSig, 1);
			}
		}
		model.unusedSignatureFields = unusedSignatureFields;
	}
}

main();