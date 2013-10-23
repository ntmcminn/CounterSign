function deleteUserSigAssets(signer)
{
	var sigImage = signer.childAssocs["csign:signerSignatureImage"];
  	if(sigImage != null)
    {
      	sigImage[0].remove();
    }
  
 	var sigKeystore = signer.childAssocs["csign:signerKeystore"];
  	if(sigKeystore != null)
    {
      	sigKeystore[0].remove();
    }
  	
  	var publicKey = signer.childAssocs["csign:signerPublicKey"];
  	if(publicKey != null)
  	{
  		publicKey[0].remove;
  	}
  	
  	signer.removeAspect("csign:signer");
}

function deleteAll()
{
	// get the list of users that have the signer aspect applied
	var signers = search.luceneSearch('ASPECT:"csign:signer"');
	
  	// for each signer, delete their sig image and keystore, then remove the aspect
  	for(index in signers)
    {
      	var signer = signers[index];      	
      	deleteUserSigAssets(signer);
    }
}

function main()
{
	deleteUserSigAssets(person);
}

main();
