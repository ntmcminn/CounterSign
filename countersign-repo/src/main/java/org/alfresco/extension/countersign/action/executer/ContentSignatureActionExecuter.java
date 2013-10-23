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
package org.alfresco.extension.countersign.action.executer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.extension.countersign.service.CounterSignService;
import org.alfresco.extension.countersign.signature.SignatureProvider;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContentSignatureActionExecuter extends AbstractSignatureActionExecuter {
    
    private static Log	logger 						= LogFactory.getLog(ContentSignatureActionExecuter.class);
    private CounterSignService counterSignService;
    
	@Override
	protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef) 
	{
		NodeService nodeService = serviceRegistry.getNodeService();
		ContentService contentService = serviceRegistry.getContentService();
		byte[] sigBytes;

		if (nodeService.exists(actionedUponNodeRef) == false)
        {
            return;
        }
    	 
        String location = (String)ruleAction.getParameterValue(PARAM_LOCATION);
        String geolocation = (String)ruleAction.getParameterValue(PARAM_GEOLOCATION);
        String reason = (String)ruleAction.getParameterValue(PARAM_REASON);
        String keyPassword = (String)ruleAction.getParameterValue(PARAM_KEY_PASSWORD);
        
		// get a hash of the document
        InputStream contentStream = contentService.
        		getReader(actionedUponNodeRef, ContentModel.PROP_CONTENT).getContentInputStream();
		
        try
        {
            // get the user's private key
	        String user = AuthenticationUtil.getRunAsUser();
	    	SignatureProvider signatureProvider = signatureProviderFactory.getSignatureProvider(user);
	        KeyStore keystore = signatureProvider.getUserKeyStore(keyPassword);
	        PrivateKey key = (PrivateKey)keystore.getKey(alias, keyPassword.toCharArray());
	        
	        // compute the document hash
	        byte[] hash = signatureProvider.computeHash(contentStream);
	        
			// sign the hash
			sigBytes = signatureProvider.signHash(hash, keyPassword);
			
			// create a "signature" node and associate it with the signed doc
	        NodeRef sig = addSignatureNodeAssociation(actionedUponNodeRef, location, reason, 
	        		"none", new java.util.Date(), geolocation, -1, "none");
	        
			// save the signature
			ContentWriter writer = contentService.getWriter(sig, ContentModel.PROP_CONTENT, true);
			writer.putContent(new ByteArrayInputStream(sigBytes));
			
			// also save the expected hash in the signature
			nodeService.setProperty(sig, CounterSignSignatureModel.PROP_DOCHASH, new String(hash));
        }
        catch(UnrecoverableKeyException uke)
        {
        	throw new AlfrescoRuntimeException(uke.getMessage());
        } 
        catch (KeyStoreException kse) 
        {
			throw new AlfrescoRuntimeException(kse.getMessage());
		} 
        catch (NoSuchAlgorithmException nsae) 
		{
			throw new AlfrescoRuntimeException(nsae.getMessage());
		} 
        catch (Exception e) 
        {
			throw new AlfrescoRuntimeException(e.getMessage());
		}
	}
	
	public void setCounterSignService(CounterSignService counterSignService)
	{
		this.counterSignService = counterSignService;
	}
}
