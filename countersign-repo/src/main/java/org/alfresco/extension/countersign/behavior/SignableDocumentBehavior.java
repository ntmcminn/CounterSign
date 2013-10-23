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
package org.alfresco.extension.countersign.behavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.AcroFields.Item;
import com.itextpdf.text.pdf.PdfReader;

public class SignableDocumentBehavior implements NodeServicePolicies.OnAddAspectPolicy
{

    private static Log          logger          	= LogFactory.getLog(SignableDocumentBehavior.class);
    private ServiceRegistry 	serviceRegistry;
    private PolicyComponent 	policyComponent;
    private Behaviour 			onAddAspect;
    
    /**
     * Initialize this behaviour component, binding class behaviour.
     */
    public void init() {
    	
        this.onAddAspect = new JavaBehaviour(this, "onAddAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT);
        
        // when the aspect is initially added, extract the unused sig fields
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnAddAspectPolicy.QNAME,
                CounterSignSignatureModel.ASPECT_SIGNABLE,
                this.onAddAspect);
    }
	
	/**
	 * When the "signable" aspect is applied, extract the signature fields and add them
	 * to the multivalue property
	 */
	public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName) 
	{
		try 
		{
			// when the aspect is added, extract the signature fields from the PDF
			ArrayList<String> signatureFields = new ArrayList<String>();
			ContentReader pdfReader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);
	        PdfReader reader = new PdfReader(pdfReader.getContentInputStream());
	        AcroFields form = reader.getAcroFields();
	        Map<String, Item> fields = form.getFields();
	        Iterator<String> it = fields.keySet().iterator();
	        while(it.hasNext())
	        {
	        	String fieldName = it.next();
	        	if(form.getFieldType(fieldName) == AcroFields.FIELD_TYPE_SIGNATURE)
	        	{
	        		// add this signature field to the list of available fields
	        		signatureFields.add(fieldName);
	        	}
	        }
    		serviceRegistry.getNodeService().setProperty(nodeRef, CounterSignSignatureModel.PROP_SIGNATUREFIELDS, signatureFields);

		}
		catch(IOException ex)
		{
			logger.error("Error extracting PDF signature fields from document: " + ex);
		}
	}
	
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }

	public void setPolicyComponent(PolicyComponent policyComponent) 
	{
		this.policyComponent = policyComponent;
	}


}
