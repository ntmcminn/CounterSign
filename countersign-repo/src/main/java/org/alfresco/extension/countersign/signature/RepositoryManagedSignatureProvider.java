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
package org.alfresco.extension.countersign.signature;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.security.auth.x500.X500Principal;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.extension.countersign.service.CounterSignService;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class RepositoryManagedSignatureProvider implements SignatureProvider
{
    private static Log          logger				= LogFactory.getLog(RepositoryManagedSignatureProvider.class);
	private ServiceRegistry 	serviceRegistry;
	private String				user;
	private Properties			config;
	private CounterSignService	counterSignService;	
    
	public RepositoryManagedSignatureProvider(ServiceRegistry serviceRegistry, 
			CounterSignService counterSignService, String user, Properties config)
	{
		this.config = config;
		this.serviceRegistry = serviceRegistry;
		this.user = user;
		this.counterSignService = counterSignService;
		
		// check to see if this user has the signer aspect.  If not, add it.
		// this is temporary until I get the management interface sorted out.
		if(!signatureAvailable())
		{
			NodeRef person = serviceRegistry.getPersonService().getPerson(user);
			if(person != null)
			{
				serviceRegistry.getNodeService().addAspect(person, CounterSignSignatureModel.ASPECT_SIGNER, null);
			}
		}
		
	}
	
	@Override
	public KeyStore getUserKeyStore(String storePassword) 
	{
		
		try 
		{
			NodeRef person = serviceRegistry.getPersonService().getPerson(user);
			
			if(person == null)
			{
				return null;
			}
			
			KeyStore keystore = KeyStore.getInstance("pkcs12");
			
			// check to see if a keystore exists for this user
			NodeRef keystoreNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERKEYSTORE);
			
			// if no keystore, create one, persist it and associate it with the user
			if(keystoreNode == null)
			{
				keystore = createUserKeyStore(person, storePassword);
			}
			else
			{
		        // open the reader to the key and load it
		        ContentReader keyReader = serviceRegistry.getContentService().getReader(keystoreNode, ContentModel.PROP_CONTENT);
		        keystore.load(keyReader.getContentInputStream(), storePassword.toCharArray());
			}
			
			// return the keystore
			return keystore;
		}
		catch(KeyStoreException kse)
		{
			throw new AlfrescoRuntimeException(kse.getMessage());
		} 
		catch (java.security.cert.CertificateException ce) 
		{
			throw new AlfrescoRuntimeException(ce.getMessage());
		}
		catch(NoSuchAlgorithmException nsaex)
		{
			throw new AlfrescoRuntimeException(nsaex.getMessage());
		}
		catch (IOException ioex) 
		{
			throw new AlfrescoRuntimeException(ioex.getMessage());
		} 
		catch (NoSuchProviderException nspex)
		{
			throw new AlfrescoRuntimeException(nspex.getMessage());
		}
	}

	@Override
	public BufferedImage getSignatureImage() 
	{
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person == null)
		{
			return null;
		}
		
		NodeRef sigImage = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERSIGNATUREIMAGE);
		
		if(sigImage != null)
		{
	        ContentReader imageReader = serviceRegistry.getContentService().getReader(sigImage, ContentModel.PROP_CONTENT);
	        
	        try 
	        {
	        	return ImageIO.read(imageReader.getContentInputStream());
	        }
	        catch(IOException ioex)
	        {
	        	logger.warn("Could not retrieve signature image as a child of person: " + ioex);
	        	// generate a default image?
	        }
		}
		
		return null;
	}

	@Override
	public String getSignatureSource() 
	{
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person == null)
		{
			return null;
		}
		
		NodeRef sigImage = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERSIGNATUREIMAGE);
		
		if(sigImage != null)
		{
	        return String.valueOf(
	        	serviceRegistry.getNodeService().getProperty(sigImage, CounterSignSignatureModel.PROP_SIGNATUREJSON));
		}
		
		return null;
	}

	@Override
	public void saveSignatureImage(BufferedImage image, String source) 
	{
		
		// save the signature image as a child of the person
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		
		if(person != null)
		{
			
			NodeRef sigNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERSIGNATUREIMAGE);
			
			if(sigNode == null)
			{
				// set up JSON source as property
				Map<QName, Serializable> sigProps = new HashMap<QName, Serializable>();
				sigProps.put(CounterSignSignatureModel.PROP_SIGNATUREJSON, source);
				
		    	QName assocQName = QName.createQName(
		    			CounterSignSignatureModel.COUNTERSIGN_SIGNATURE_MODEL_1_0_URI,
		    			QName.createValidLocalName(user + "-signatureimage"));
		    		
		    	ChildAssociationRef sigChildRef = serviceRegistry.getNodeService().createNode(
		    			person,
		    			CounterSignSignatureModel.ASSOC_SIGNERSIGNATUREIMAGE, 
		    			assocQName, 
		    			CounterSignSignatureModel.TYPE_SIGNATUREIMAGE,
		    			sigProps);
		    	
		    	sigNode = sigChildRef.getChildRef();
			}
			else
			{
				serviceRegistry.getNodeService().setProperty(sigNode, CounterSignSignatureModel.PROP_SIGNATUREJSON, source);
			}
			
			// get a writer, store the image content
			ContentWriter writer = serviceRegistry.getContentService().
					getWriter(sigNode, ContentModel.PROP_CONTENT, true);

			try 
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
				
				PermissionService ps = serviceRegistry.getPermissionService();
				ps.clearPermission(sigNode, PermissionService.ALL_AUTHORITIES);
				ps.setInheritParentPermissions(sigNode, false);
			}
			catch(IOException ioex)
			{
				logger.warn("Could not save signature image as child of person: " + ioex);
			}
		}
	}

	@Override
	public boolean signatureAvailable() 
	{
		// signature is only available if this user has the "signer" aspect applied.
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		if(person != null && serviceRegistry.getNodeService().hasAspect(person, CounterSignSignatureModel.ASPECT_SIGNER))
		{	
			// now check to see if the user has a keystore available.  If so, then
			// they can sign without creating a new keystore.
			NodeRef keystoreNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERKEYSTORE);
			if(keystoreNode != null && serviceRegistry.getNodeService().exists(keystoreNode))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Get the user's public key
	 * 
	 */
	public PublicKey getPublicKey()
	{
		NodeRef person = serviceRegistry.getPersonService().getPerson(user);
		NodeRef keyNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERPUBLICKEY);
		
		if(keyNode != null)
		{
			PEMReader parser = null;
			
			try
			{
		        ContentReader keyReader = serviceRegistry.getContentService().getReader(keyNode, ContentModel.PROP_CONTENT);
		        parser = new PEMReader(new InputStreamReader(keyReader.getContentInputStream()));
		        PublicKey key = (PublicKey)parser.readObject();
		        parser.close();
		        return key;
			}
			catch(Exception ioex)
			{
				logger.warn("Error reading user public key: " + ioex.getLocalizedMessage());
			}
			finally
			{
				try {if(parser != null) parser.close();}
				catch(IOException ioex){logger.warn("Error closing PEMReader");}
			}
		}
		
		return null;
	}

	@Override
	public boolean validateSignature(byte[] sig, byte[] hash)
	{
		String alg = config.getProperty(RepositoryManagedSignatureProviderFactory.SIGNATURE_ALGORITHM);
		String prov = config.getProperty(RepositoryManagedSignatureProviderFactory.JAVA_SIGNATURE_PROVIDER);
		
		boolean valid = false;
		
		try
		{
			Signature validate = Signature.getInstance(alg, prov);
			validate.initVerify(getPublicKey());
			validate.update(hash);
			valid = validate.verify(sig);
		}
		catch(NoSuchProviderException nspe)
		{
			throw new AlfrescoRuntimeException("Provider: " + prov + " was not found: " + nspe.getMessage());
		} 
		catch (NoSuchAlgorithmException nsae) 
		{
			throw new AlfrescoRuntimeException("Algorithm: " + alg + " is not available: " + nsae.getMessage());
		}
		catch(SignatureException se)
		{
			valid = false;
		}
		catch(InvalidKeyException ike)
		{
			valid = false;
		}
		
		return valid;
	}
	
	/**
	 * Sign a hash using the user's private key
	 * 
	 * @param hash
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public byte[] signHash(byte[] hash, String password) throws Exception {
		
		String alg = config.getProperty(RepositoryManagedSignatureProviderFactory.SIGNATURE_ALGORITHM);
		String prov = config.getProperty(RepositoryManagedSignatureProviderFactory.JAVA_SIGNATURE_PROVIDER);
		String alias = config.getProperty(RepositoryManagedSignatureProviderFactory.ALIAS);
		
		KeyStore ks = getUserKeyStore(password);
		PrivateKey key = (PrivateKey)ks.getKey(alias, password.toCharArray());
	    Signature signer = Signature.getInstance(alg, prov);
	    signer.initSign(key);
	    signer.update(hash);
	    return signer.sign();
	}
	
	/**
	 * Compute a hash of the input stream using the configured algorithm
	 * 
	 * @param contentSteam
	 * @return a hash of the content stream
	 */
	public byte[] computeHash(InputStream contentStream) {
		
		String alg = config.getProperty(RepositoryManagedSignatureProviderFactory.HASH_ALGORITHM);
		
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(alg);
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unable to process algorithm type: " + alg);
            return null;
        }
        messageDigest.reset();
        byte[] buffer = new byte[1024];
        int bytesRead = -1;
        try {
            while ((bytesRead = contentStream.read(buffer)) > -1) {
                messageDigest.update(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            logger.error("Unable to read content stream.", e);
            return null;
        } finally {
            try {
                contentStream.close();
            } catch (IOException e) {}
        }
        byte[] digest = messageDigest.digest();
        return convertByteArrayToHex(digest).getBytes();
    }
	
	private String convertByteArrayToHex(byte[] array) {
        StringBuffer hashValue = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            String hex = Integer.toHexString(0xFF & array[i]);
            if (hex.length() == 1) {
                hashValue.append('0');
            }
            hashValue.append(hex);
        }
        return hashValue.toString().toUpperCase();
    }
	
	/**
	 * Save the user's public key, associated as a child with the person
	 * that owns it.  Make this world-readable, as anybody should be able
	 * to use it for validating signatures.
	 * 
	 * @param person
	 * @param publicKey
	 */
	private void saveUserPublicKey(NodeRef person, PublicKey publicKey)
	{
		NodeRef keyNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERPUBLICKEY);

		if(keyNode == null)
		{
			QName assocQName = QName.createQName(
					CounterSignSignatureModel.COUNTERSIGN_SIGNATURE_MODEL_1_0_URI,
					QName.createValidLocalName(user + "-publickey"));

			ChildAssociationRef keyChildRef = serviceRegistry.getNodeService().createNode(
					person,
					CounterSignSignatureModel.ASSOC_SIGNERPUBLICKEY, 
					assocQName, 
					CounterSignSignatureModel.TYPE_PUBLICKEY);

			keyNode = keyChildRef.getChildRef();
		}

		// get a writer, store the public key
		ContentWriter writer = serviceRegistry.getContentService().
				getWriter(keyNode, ContentModel.PROP_CONTENT, true);
		PEMWriter pem = null;
		
		try 
		{
			//set the encoding and write out the key
			serviceRegistry.getNodeService().setProperty(keyNode, CounterSignSignatureModel.PROP_KEYENCODING, "PEM");
			pem = new PEMWriter(new OutputStreamWriter(writer.getContentOutputStream()));
			pem.writeObject(publicKey);
		}
		catch(IOException ioex)
		{
			logger.error("Error writing public key to PEMWriter: " + ioex);
		}
		finally
		{
			if(pem != null) try{pem.close();}catch(Exception ex) {logger.error("Error closing PEMWriter");}
		}
		
		// Ensure that the cert is readable by anybody, this is important
		// for later validation of signed content.  Can't validate without the
		// public key
		PermissionService ps = serviceRegistry.getPermissionService();
		ps.setPermission(keyNode, PermissionService.ALL_AUTHORITIES, PermissionService.READ, true);
		ps.setInheritParentPermissions(keyNode, false);
	}
	
	/**
	 * Save the Java KeyStore, associated as a child with the person that owns this keystore
	 * 
	 * @param person
	 * @param keystore
	 * @throws IOException 
	 * @throws CertificateException 
	 * @throws NoSuchAlgorithmException 
	 * @throws KeyStoreException 
	 */
	private void saveUserKeyStore(NodeRef person, KeyStore keystore, String password) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException
	{

		NodeRef keystoreNode = counterSignService.getSignatureArtifact(person, CounterSignSignatureModel.ASSOC_SIGNERKEYSTORE);

		if(keystoreNode == null)
		{
			QName assocQName = QName.createQName(
					CounterSignSignatureModel.COUNTERSIGN_SIGNATURE_MODEL_1_0_URI,
					QName.createValidLocalName(user + "-signaturekeystore"));

			ChildAssociationRef keyChildRef = serviceRegistry.getNodeService().createNode(
					person,
					CounterSignSignatureModel.ASSOC_SIGNERKEYSTORE, 
					assocQName, 
					CounterSignSignatureModel.TYPE_PKCS12);

			keystoreNode = keyChildRef.getChildRef();
		}

		// get a writer, store the user keystore
		ContentWriter writer = serviceRegistry.getContentService().
				getWriter(keystoreNode, ContentModel.PROP_CONTENT, true);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		keystore.store(baos, password.toCharArray());
		writer.putContent(new ByteArrayInputStream(baos.toByteArray()));
		
		// now set the permissions on this node so that only the person that owns it
		// can access it.  PermissionService.ALL_AUTHORITIES = GROUP_EVERYONE
		// this should leave the owner permissions intact
		PermissionService ps = serviceRegistry.getPermissionService();
		ps.clearPermission(keystoreNode, PermissionService.ALL_AUTHORITIES);
		ps.setInheritParentPermissions(keystoreNode, false);
	}
	
	/**
	 * Create a keystore for this user to be used for document signing, store it associated with the user's
	 * person node
	 * 
	 * @param person
	 * @param password
	 * 
	 * @return a Java KeyStore object suitable for document signing
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws KeyStoreException 
	 * @throws IOException 
	 * @throws CertificateException 
	 */
	private KeyStore createUserKeyStore(NodeRef person, String password) 
			throws NoSuchAlgorithmException, NoSuchProviderException, KeyStoreException, CertificateException, IOException
	{

		// get the alias from the configuration
		String alias = config.getProperty(RepositoryManagedSignatureProviderFactory.ALIAS);
		
		// initialize key generator
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(2048, random);

		// generate a keypair
		KeyPair pair = keyGen.generateKeyPair();
		PrivateKey priv = pair.getPrivate();
		PublicKey pub = pair.getPublic();
		
		Certificate[] certChain = getCertChain(pair, person);
		
		// create keystore, adding private key and cert chain
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(null, password.toCharArray());
		ks.setKeyEntry(alias, priv, password.toCharArray(), certChain);

		// save the keystore
		saveUserKeyStore(person, ks, password);

		// also save the public key separately, will need it 
		// for later validaiton activities
		saveUserPublicKey(person, pub);
		
		// return the generated keystore
		return ks;

	}
	
	/**
	 * Get a certificate chain, given a person and a keypair
	 * @param pair
	 * @param person
	 * @return
	 */
	private Certificate[] getCertChain(KeyPair pair, NodeRef person)
	{
		boolean generateTrusted = Boolean.parseBoolean(config.getProperty(RepositoryManagedSignatureProviderFactory.ENABLE_TRUSTED_CERTS));
		Certificate[] certChain;
		
		// generate the user certificate
		Certificate cert = generateCertificate(pair, person, generateTrusted);
		
		// create a trusted cert chain if enabled
		if(generateTrusted)
		{
			// get the ca cert used to sign and create cert chain
			KeyStore trustedKs = getTrustedKeyStore();
			Certificate[] caChain = getCaCertChain(trustedKs);
			certChain = new Certificate[caChain.length + 1];
			certChain[0] = cert;
			for(int i = 0; i < caChain.length; i++)
			{
				certChain[i+1] = caChain[i];
			}
		}
		else
		{
			certChain = new Certificate[1];
			certChain[0] = cert;
		}
		
		return certChain;
	}
	
	/**
	 * Generate an X509 cert for use as the keystore cert chain
	 * 
	 * @param keyPair
	 * @return
	 */
	private X509Certificate generateCertificate(KeyPair keyPair, NodeRef person, boolean trusted)
	{  
		
		X509Certificate cert = null;
		int validDuration = Integer.parseInt(config.getProperty(RepositoryManagedSignatureProviderFactory.VALID_DURATION));

		// get user's first and last name
		Map<QName, Serializable> props = serviceRegistry.getNodeService().getProperties(person);
		String firstName = String.valueOf(props.get(ContentModel.PROP_FIRSTNAME));
		String lastName = String.valueOf(props.get(ContentModel.PROP_LASTNAME));
		
		// backdate the start date by a day
		Calendar start = Calendar.getInstance();
		start.add(Calendar.DATE, -1);
		java.util.Date startDate = start.getTime();
		
		// what is the end date for this cert's validity?
		Calendar end = Calendar.getInstance();
		end.add(Calendar.DATE, validDuration);
		java.util.Date endDate = end.getTime();
		
		try
		{
			// This code works with newer versions of the BouncyCastle libraries, but not
			// the (severely outdated) version that ships with Alfresco
			/*X509v1CertificateBuilder certBuilder = new JcaX509v1CertificateBuilder(
		              new X500Principal("CN=" + firstName + " " + lastName), 
		              BigInteger.ONE, 
		              startDate, cal.getTime(), 
		              new X500Principal("CN=" + firstName + " " + lastName), 
		              keyPair.getPublic());
			
		    AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
		    AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
		    AsymmetricKeyParameter keyParam = PrivateKeyFactory.createKey(keyPair.getPrivate().getEncoded());
			ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(keyParam);
			X509CertificateHolder certHolder = certBuilder.build(sigGen);
			
			// now lets convert this thing back to a regular old java cert
			CertificateFactory cf = CertificateFactory.getInstance("X.509");  
		    InputStream certIs = new ByteArrayInputStream(certHolder.getEncoded()); 
		    cert = (X509Certificate) cf.generateCertificate(certIs); 
		    certIs.close();*/
			
			X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
			X500Principal subjectName = new X500Principal("CN=" + firstName + " " + lastName);

			certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
			certGen.setNotBefore(startDate);
			certGen.setNotAfter(endDate);
			certGen.setSubjectDN(subjectName);
			certGen.setPublicKey(keyPair.getPublic());
			certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
			
			// if we are actually generating a trusted cert, the action is a little different
			if(trusted)
			{
				KeyStore trustedKs = getTrustedKeyStore();
				
				PrivateKey caKey = getCaKey(trustedKs);
				X509Certificate caCert = getCaCert(trustedKs);
			
				// set the issuer of the generated cert to the subject of the ca cert
				X500Principal caSubject = caCert.getSubjectX500Principal();
				certGen.setIssuerDN(caSubject);
				
				//add the required extensions for the new cert
				certGen.addExtension(X509Extensions.AuthorityKeyIdentifier, false,
					new AuthorityKeyIdentifierStructure(caCert));
				certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false,
					new SubjectKeyIdentifierStructure(keyPair.getPublic()));
				
				cert = certGen.generate(caKey, "BC");
				
				//verify the cert
				cert.verify(caCert.getPublicKey());
			}
			else
			{
				certGen.setIssuerDN(subjectName);
				cert = certGen.generate(keyPair.getPrivate(), "BC");
			}
		}
		catch(CertificateException ce) 
		{
			logger.error("CertificateException creating or validating X509 certificate for user: " + ce);
			throw new AlfrescoRuntimeException(ce.getMessage());
		}
		catch(Exception ex)
		{
			logger.error("Unknown exception creating or validating X509 certificate for user : " + ex);
			ex.printStackTrace();
		}
		
		return cert;
	}
	
	/**
	 * Get the Certificate Authority private key
	 * 
	 * @return
	 */
	private PrivateKey getCaKey(KeyStore trustedKs)
	{
		PrivateKey caKey = null;
		String keyAlias = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_KEY_ALIAS);
		String keyPassword = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_KEY_PASSWORD);
		
		try
		{
			caKey = (PrivateKey)trustedKs.getKey(keyAlias, keyPassword.toCharArray());
		}
		catch(KeyStoreException kse)
		{
			throw new AlfrescoRuntimeException(kse.getMessage());
		}
		catch(UnrecoverableKeyException uke)
		{
			throw new AlfrescoRuntimeException(uke.getMessage());
		}
		catch(NoSuchAlgorithmException nsae)
		{
			throw new AlfrescoRuntimeException(nsae.getMessage());
		}
		
		return caKey;
	}
	
	/**
	 * Get the Certificate Authority public key certificate
	 * 
	 * @return
	 */
	private X509Certificate getCaCert(KeyStore trustedKs)
	{
		X509Certificate caCert = null;
		String certAlias = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_CERT_ALIAS);
		
		try
		{
			caCert = (X509Certificate)trustedKs.getCertificate(certAlias);
		}
		catch(KeyStoreException kse)
		{
			throw new AlfrescoRuntimeException(kse.getMessage());
		}
		
		return caCert;
	}
	
	/**
	 * Get the certificate chain for the CA certificate
	 * 
	 * @param trustedKs
	 * @return
	 */
	private Certificate[] getCaCertChain(KeyStore trustedKs) 
	{
		Certificate[] caCertChain = null;
		String certAlias = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_CERT_ALIAS);
		
		try
		{
			caCertChain = trustedKs.getCertificateChain(certAlias);
		}
		catch(KeyStoreException kse)
		{
			throw new AlfrescoRuntimeException(kse.getMessage());
		}
		
		return caCertChain;
	}
	
	/**
	 * Get the trusted keystore as configured in the extension properties.
	 * 
	 * @return
	 */
	private KeyStore getTrustedKeyStore() 
	{
		try 
		{
			String keystorePassword = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_KEYSTORE_PASSWORD);
			String keystorePath = config.getProperty(RepositoryManagedSignatureProviderFactory.TRUSTED_KEYSTORE_PATH);
			KeyStore keystore = KeyStore.getInstance("pkcs12");
		    FileInputStream keyStream = new FileInputStream(keystorePath);
		    keystore.load(keyStream, keystorePassword.toCharArray());
		    
			// return the keystore
			return keystore;
		}
		catch(KeyStoreException kse)
		{
			throw new AlfrescoRuntimeException(kse.getMessage());
		} 
		catch (java.security.cert.CertificateException ce) 
		{
			throw new AlfrescoRuntimeException(ce.getMessage());
		}
		catch(NoSuchAlgorithmException nsaex)
		{
			throw new AlfrescoRuntimeException(nsaex.getMessage());
		}
		catch (IOException ioex) 
		{
			throw new AlfrescoRuntimeException(ioex.getMessage());
		} 
	}
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
}
