var username = person.properties["cm:userName"];
var signatureAvailable = countersign.getSignatureAvailable(username)

// add the user's signature availability
model.signatureAvailable = signatureAvailable;

if(signatureAvailable)
{
	// add the user's keystore info
	model.keyStore = countersign.getKeystoreNode(username);
	
	// add the user's signature image info
	model.sigImage = countersign.getSignatureImageNode(username);
	
	// add the user's public key
	model.publicKey = countersign.getPublicKeyNode(username);
}