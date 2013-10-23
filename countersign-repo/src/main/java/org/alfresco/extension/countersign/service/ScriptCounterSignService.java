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
package org.alfresco.extension.countersign.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.extension.countersign.signature.SignatureProvider;
import org.alfresco.extension.countersign.signature.SignatureProviderFactory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.processor.BaseProcessorExtension;
import org.alfresco.repo.web.scripts.workflow.WorkflowModelBuilder;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;

import com.itextpdf.text.pdf.PdfReader;

public class ScriptCounterSignService extends BaseProcessorExtension {

	private static final Log 			logger 						= LogFactory.getLog(ScriptCounterSignService.class);
	private ServiceRegistry				serviceRegistry;
	private SignatureProviderFactory 	signatureProviderFactory;
	private Properties 					config;
	private ArrayList<String>			counterSignWorkflowIds		= new ArrayList<String>();
	private CounterSignService			counterSignService;
	
	public static final String			signatureValidName			= "signatureValid";
	public static final String			hashValidName				= "hashValid";
	
    public String getClassName()
    {
        return "CounterSignSignatureService";
    }
    
    /**
     * Gets a signature source for the provided user.  This is JSON in the default
     * implementation, but could be a URL to an external signature image
     * 
     * @param user
     * @return
     */
    public String getSignatureSource(String user)
    {
    	return signatureProviderFactory.getSignatureProvider(user).getSignatureSource();
    }
    
    /**
     * Get a nodeRef string for the user's keystore node
     * 
     * @param user
     * @return
     */
    public ScriptNode getKeystoreNode(String user)
    {
    	NodeRef keystore = counterSignService.getSignatureArtifact(user, CounterSignSignatureModel.ASSOC_SIGNERKEYSTORE);
    	if(keystore != null)
    	{
    		return new ScriptNode(keystore, serviceRegistry);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    /**
     * Get a nodeRef string for the user's signature image
     * 
     * @param user
     * @return
     */
    public ScriptNode getSignatureImageNode(String user)
    {
    	NodeRef image = counterSignService.getSignatureArtifact(user, CounterSignSignatureModel.ASSOC_SIGNERSIGNATUREIMAGE);
    	if(image != null)
    	{
    		return new ScriptNode(image, serviceRegistry);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    /**
     * Get a nodeRef string for the user's signature image
     * 
     * @param user
     * @return
     */
    public ScriptNode getPublicKeyNode(String user)
    {
    	NodeRef publicKey= counterSignService.getSignatureArtifact(user, CounterSignSignatureModel.ASSOC_SIGNERPUBLICKEY);
    	if(publicKey != null)
    	{
    		return new ScriptNode(publicKey, serviceRegistry);
    	}
    	else
    	{
    		return null;
    	}
    }
    
    /**
     * Gets the signature info embedded in the signed PDF document itself
     * 
     * @param nodeRef
     */
    public void getSignatures(String nodeRef)
    {
    	
    }
    
	/**
	 * Gets the page count for a PDF document
	 * 
	 * @param nodeRef
	 * @return
	 */
	public int getPageCount(String nodeRef){
		try
		{
			ContentReader reader = serviceRegistry
					.getContentService().getReader(new NodeRef(nodeRef), ContentModel.PROP_CONTENT);
			PdfReader pdfReader = new PdfReader(reader.getContentInputStream());
			int count = pdfReader.getNumberOfPages();
			pdfReader.close();
			return count;
			
		}
		catch(IOException ioex)
		{
			return -1;
		}
	}
	
	/**
	 * Get the extension configuration (all properties)
	 * @return
	 */
	public HashMap<String, String> getConfig()
	{
		return new HashMap<String, String>((Map)config);
	}
	
	/**
	 * Get a single named properties from the CounterSign config file.  Simply 
	 * for convenience.
	 * 
	 * @param name
	 * @return
	 */
	public String getProperty(String name)
	{
		return config.getProperty(name);
	}
	
    /**
     * Returns "true" if this user has all of the required bits and pieces (keystore) to
     * apply a digital signature.
     * 
     * @param user
     */
    public boolean getSignatureAvailable(String user)
    {
    	return signatureProviderFactory.getSignatureProvider(user).signatureAvailable();
    }
    
    /**
     * Gets all of the recorded signature workflow events for this node, both for
     * active and completed workflows
     * 
     * @param nodeRef
     */
    public List<Map<String, Object>> getSignatureWorkflowHistory(String nodeRef)
    {
    	NodeService ns = serviceRegistry.getNodeService();
    	WorkflowService wfs = serviceRegistry.getWorkflowService();
    	List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    	NodeRef node = new NodeRef(nodeRef);
    	WorkflowModelBuilder modelBuilder = 
    			new WorkflowModelBuilder(serviceRegistry.getNamespaceService(), 
    									 serviceRegistry.getNodeService(), 
    									 serviceRegistry.getAuthenticationService(), 
    									 serviceRegistry.getPersonService(),
    									 serviceRegistry.getWorkflowService(), 
    									 serviceRegistry.getDictionaryService());
    	if(ns.exists(node))
    	{
    		List<WorkflowInstance> active = wfs.getWorkflowsForContent(node, true);
    		List<WorkflowInstance> inactive = wfs.getWorkflowsForContent(node, false);
    		
    		// merge the lists
    		ArrayList<WorkflowInstance> all = new ArrayList<WorkflowInstance>(active.size() + inactive.size());
    		all.addAll(active);
    		all.addAll(inactive);
    		
    		// we only need instances of the known CounterSign workflow types
    		for(WorkflowInstance instance : all)
    		{
    			// if the instance definition name is in the list, get its tasks
    			// and add them to the task list
    			if(counterSignWorkflowIds.contains(instance.getDefinition().getName()))
    			{
    				results.add(modelBuilder.buildDetailed(instance, true));
    			}
    		}
    	}
    	else
    	{
    		throw new AlfrescoRuntimeException("Node " + nodeRef + " does not exist");
    	}

    	return results;
    }
    
    /**
     * Validates a single signature, passed in as a nodeRef String
     * 
     * @param nodeRef
     * @return {signatureValid:[validity],hashValid:[validity]}
     */
    public JSONObject validateSignature(String nodeRef)
    {
    	// get the node, make sure it exists
    	NodeService ns = serviceRegistry.getNodeService();
    	ContentService cs = serviceRegistry.getContentService();
    	NodeRef sigNode = new NodeRef(nodeRef);
    	boolean signatureValid = false;
    	boolean hashValid = false;
    	
    	if(ns.exists(sigNode))
    	{
    		try
    		{
	    		// get the doc has from the time of the sig and the sig itself
	    		String docHash = String.valueOf(ns.getProperty(sigNode, CounterSignSignatureModel.PROP_DOCHASH));
	    		ContentReader sigReader = cs.getReader(sigNode, ContentModel.PROP_CONTENT);
	    		InputStream sigStream = sigReader.getContentInputStream();
	    		
	    		ByteArrayOutputStream baos = new ByteArrayOutputStream();				
	    		byte[] buffer = new byte[1024];
	    		int read = 0;
	    		while ((read = sigStream.read(buffer, 0, buffer.length)) != -1) {
	    			baos.write(buffer, 0, read);
	    		}		
	    		baos.flush();
	    		
	    		// get the signing user's public key
	    		String person = String.valueOf(ns.getProperty(sigNode, CounterSignSignatureModel.PROP_EXTERNALSIGNER));
	    		SignatureProvider prov = signatureProviderFactory.getSignatureProvider(person);	
	    		
	    		// validate the sig using the public key
	    		signatureValid = prov.validateSignature(baos.toByteArray(), docHash.getBytes());
	    		
    			// get the document associated with this sig, and compute the hash
    			NodeRef signedDoc = ns.getParentAssocs(sigNode).get(0).getParentRef();
    			ContentReader docReader = cs.getReader(signedDoc, ContentModel.PROP_CONTENT);
    			String contentHash = new String(prov.computeHash(docReader.getContentInputStream()));
    			if(docHash.equals(contentHash))
    			{
    				hashValid = true;
    			}
    			else
    			{
    				signatureValid = false;
    			}
    		}
    		catch(IOException ioex)
    		{
    			throw new AlfrescoRuntimeException("IOException reading signature: " + ioex.getMessage());
    		}
    	}
    	else
    	{
    		throw new AlfrescoRuntimeException("Node: " + nodeRef + " does not exist");
    	}
    	
		// create the JSONObject to return
		JSONObject valid = new JSONObject();
		valid.put(signatureValidName, signatureValid);
		valid.put(hashValidName,  hashValid);
		
    	return valid;
    }
    
    
    // getters, setters and other boring stuff:
    
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setSignatureProviderFactory(SignatureProviderFactory signatureProviderFactory)
	{
		this.signatureProviderFactory = signatureProviderFactory;
	}
	
	public void setExtensionConfig(Object config)
	{
		this.config = (Properties)config;
	}
	
	public void setCounterSignWorkflowIds(List ids)
	{
		this.counterSignWorkflowIds.addAll(ids);
	}   
	
	public void setCounterSignService(CounterSignService counterSignService)
	{
		this.counterSignService = counterSignService;
	}
}
