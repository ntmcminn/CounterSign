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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.extension.countersign.signature.SignatureProviderFactory;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.json.simple.JSONObject;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

public abstract class AbstractSignatureActionExecuter extends 
	ActionExecuterAbstractBase
{

    /**
     * Predefined position options
     */
    public static final String            POSITION_CENTER          = "center";
    public static final String            POSITION_TOPLEFT         = "topleft";
    public static final String            POSITION_TOPRIGHT        = "topright";
    public static final String            POSITION_BOTTOMLEFT      = "bottomleft";
    public static final String            POSITION_BOTTOMRIGHT     = "bottomright";
    
	protected ServiceRegistry 			  serviceRegistry;
	
    public static final String            PARAM_LOCATION           = "location";
    public static final String 			  PARAM_GEOLOCATION	   	   = "geolocation";
    public static final String            PARAM_REASON             = "reason";
    public static final String            PARAM_KEY_PASSWORD       = "key-password";
    
    protected String	 				  alias					   = "countersign";

    protected SignatureProviderFactory	  signatureProviderFactory = null;

    /**
     * Load the parameter definitions common across all signature types
     *
     */
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) 
	{
        paramList.add(new ParameterDefinitionImpl(PARAM_LOCATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_LOCATION)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GEOLOCATION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_GEOLOCATION)));
        paramList.add(new ParameterDefinitionImpl(PARAM_REASON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_REASON)));
        paramList.add(new ParameterDefinitionImpl(PARAM_KEY_PASSWORD, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_KEY_PASSWORD)));
	}
	
    /**
     * Get the signature block position, using the provided JSON for the box coordinates
     * and the selected page
     * 
     * @param pageRect
     * @param box
     * @return
     */
    protected Rectangle positionBlock(Rectangle pageRect, JSONObject box)
    {
    	float startX = Integer.parseInt(String.valueOf(box.get("startX")));
    	float startY = Integer.parseInt(String.valueOf(box.get("startY")));
    	float endX = Integer.parseInt(String.valueOf(box.get("endX")));
    	float endY = Integer.parseInt(String.valueOf(box.get("endY")));
    	
    	// make sure that the ll and ur coordinates match iText's expectations
    	startY = pageRect.getHeight() - startY;
    	endY = pageRect.getHeight() - endY;
    	
    	// create the rectangle to contain the signature from the corrected coordinates
    	Rectangle r = new Rectangle(startX, startY, endX, endY);
    	
    	return r;
    }
    
    /**
     * @param actionedUponNodeRef
     * @return
     */
    protected ContentReader getReader(NodeRef nodeRef)
    {
        // First check that the node is a sub-type of content
        QName typeQName = serviceRegistry.getNodeService().getType(nodeRef);
        if (serviceRegistry.getDictionaryService().isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false)
        {
            // it is not content, so can't transform
            return null;
        }

        // Get the content reader
        ContentReader contentReader = serviceRegistry.getContentService().getReader(nodeRef, ContentModel.PROP_CONTENT);

        return contentReader;
    }
    

    /**
     * Gets the X value for centering the signature stamp
     * 
     * @param r
     * @param img
     * @return
     */
    protected float getCenterX(Rectangle r, Image img)
    {
        float x = 0;
        float pdfwidth = r.getWidth();
        float imgwidth = img.getWidth();

        x = (pdfwidth - imgwidth) / 2;

        return x;
    }


    /**
     * Gets the Y value for centering the signature stamp
     * 
     * @param r
     * @param img
     * @return
     */
    protected float getCenterY(Rectangle r, Image img)
    {
        float y = 0;
        float pdfheight = r.getHeight();
        float imgheight = img.getHeight();

        y = (pdfheight - imgheight) / 2;

        return y;
    }
    
    /**
     * Create a rectangle for the visible stamp using the selected position and block size
     * 
     * @param position
     * @param width
     * @param height
     * @return
     */
    protected Rectangle positionBlock(String position, Rectangle pageRect, int width, int height)
    {

    	float pageHeight = pageRect.getHeight();
    	float pageWidth = pageRect.getWidth();
    	
    	Rectangle r = null;
    	//Rectangle constructor(float llx, float lly, float urx, float ury)
    	if (position.equals(POSITION_BOTTOMLEFT))
    	{
    		r = new Rectangle(0, height, width, 0);
    	}
    	else if (position.equals(POSITION_BOTTOMRIGHT))
    	{
    		r = new Rectangle(pageWidth - width, height, pageWidth, 0);
    	}
    	else if (position.equals(POSITION_TOPLEFT))
    	{
    		r = new Rectangle(0, pageHeight, width, pageHeight - height);
    	}
    	else if (position.equals(POSITION_TOPRIGHT))
    	{
    		r = new Rectangle(pageWidth - width, pageHeight, pageWidth, pageHeight - height);
    	}
    	else if (position.equals(POSITION_CENTER))
    	{
    		r = new Rectangle((pageWidth / 2) - (width / 2), (pageHeight / 2) - (height / 2),
    				(pageWidth / 2) + (width / 2), (pageHeight / 2) + (height / 2));
    	}

    	return r;
    }
    
    /**
     * Creates a "signature" object and associates it with the signed doc
     * @param node
     * @param location
     * @param reason
     */
    protected NodeRef addSignatureNodeAssociation(NodeRef node, String location, String reason, 
    		String signatureField, java.util.Date sigDate, String geolocation, int page, String position)
    {
    	NodeService nodeService = serviceRegistry.getNodeService();
    	
    	String userId = AuthenticationUtil.getRunAsUser();
    	NodeRef person = serviceRegistry.getPersonService().getPerson(userId);
    	
    	// if page is -1, then this was a signature field, set position to "none"
    	if(page == -1) position = "none";
    	
    	HashMap<QName, Serializable> props = new HashMap<QName, Serializable>();
    	props.put(CounterSignSignatureModel.PROP_REASON, reason);
    	props.put(CounterSignSignatureModel.PROP_LOCATION, location);
    	props.put(CounterSignSignatureModel.PROP_SIGNATUREDATE, sigDate);
    	props.put(CounterSignSignatureModel.PROP_SIGNATUREFIELD, signatureField);
    	props.put(CounterSignSignatureModel.PROP_SIGNATUREPAGE, page);
    	props.put(CounterSignSignatureModel.PROP_SIGNATUREPOSITION, position);
    	props.put(CounterSignSignatureModel.PROP_EXTERNALSIGNER, userId);
    	
    	// check the geolocation data, if it is valid, split it out and add
    	if(geolocation.indexOf(",") != -1)
    	{
    		String[] latLong = geolocation.split(",");
    		props.put(ContentModel.PROP_LATITUDE, latLong[0]);
    		props.put(ContentModel.PROP_LONGITUDE, latLong[1]);
    	}
    	else
    	{
    		props.put(ContentModel.PROP_LATITUDE, -1);
    		props.put(ContentModel.PROP_LONGITUDE, -1);
    	}
    	
    	QName assocQName = QName.createQName(
    			CounterSignSignatureModel.COUNTERSIGN_SIGNATURE_MODEL_1_0_URI,
    			QName.createValidLocalName(userId + "-" + sigDate.getTime()));
    		
    	ChildAssociationRef sigChildRef = nodeService.createNode(
    			node, 
    			CounterSignSignatureModel.ASSOC_SIGNATURES, 
    			assocQName, 
    			CounterSignSignatureModel.TYPE_SIGNATURE, 
    			props);
    	
    	NodeRef signature = sigChildRef.getChildRef();
    	
    	// add hidden aspect to signature nodes, these should not be 
    	// shown in any document lists or other Share views
    	HashMap<QName, Serializable> aspectProps = new HashMap<QName, Serializable>();
    	aspectProps.put(ContentModel.PROP_VISIBILITY_MASK, HiddenAspect.Visibility.NotVisible.getMask());
    	nodeService.addAspect(signature, ContentModel.ASPECT_HIDDEN, aspectProps);
    
    	nodeService.createAssociation(signature, person, CounterSignSignatureModel.ASSOC_SIGNEDBY);
    	
    	return signature;
    }
    
    /**
     * Sets the signature provider used to sign documents without the need to select
     * a keystore, etc.
     * 
     * @param signatureProvider
     */
    public void setSignatureProviderFactory(SignatureProviderFactory signatureProviderFactory)
    {
    	this.signatureProviderFactory = signatureProviderFactory;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
    	this.serviceRegistry = serviceRegistry;
    }
    
    public void setAlias(String alias)
    {
    	this.alias = alias;
    }
}
