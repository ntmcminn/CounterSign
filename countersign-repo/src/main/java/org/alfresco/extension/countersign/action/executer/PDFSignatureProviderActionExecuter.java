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

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.extension.countersign.signature.SignatureProvider;
import org.alfresco.extension.countersign.signature.SignatureProviderFactory;
import org.alfresco.extension.countersign.signature.SignatureToImage;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;

public class PDFSignatureProviderActionExecuter extends
	AbstractSignatureActionExecuter {

    /**
     * The logger
     */
    private static Log                    logger                   = LogFactory.getLog(PDFSignatureProviderActionExecuter.class);
    
    private JSONParser 					  parser 				   = new JSONParser();
    
    /**
     * Constraints
     */
    public static HashMap<String, String> visibilityConstraint     = new HashMap<String, String>();
    public static HashMap<String, String> positionConstraint	   = new HashMap<String, String>();
    
    /**
     * Action constants
     */
    public static final String            NAME                     = "pdf-signature-provider-action";
    public static final String            PARAM_VISIBLE            = "visible";
    public static final String            PARAM_ALIAS              = "alias";
    public static final String			  PARAM_SIGNATURE_JSON	   = "signature-json";
    public static final String			  PARAM_POSITION		   = "position";
    public static final String			  PARAM_GRAPHIC			   = "graphic";
    
    public static final String            KEY_TYPE_PKCS12          = "pkcs12";
    public static final String			  POSITION_DELIMITER	   = "#";
    
    public static final String			  POSITION_TYPE_DRAWN	   = "drawn";
    public static final String			  POSITION_TYPE_PREDEFINED = "predefined";
    public static final String			  POSITION_TYPE_FIELD	   = "field";
            
    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
    	super.addParameterDefinitions(paramList);
    	
        paramList.add(new ParameterDefinitionImpl(PARAM_ALIAS, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_ALIAS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SIGNATURE_JSON, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_SIGNATURE_JSON)));
        paramList.add(new ParameterDefinitionImpl(PARAM_VISIBLE, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_VISIBLE)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GRAPHIC, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_GRAPHIC)));
        paramList.add(new ParameterDefinitionImpl(PARAM_POSITION, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_POSITION), false, "countersign.position"));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {

    	if (serviceRegistry.getNodeService().exists(actionedUponNodeRef) == false)
        {
            // node doesn't exist - can't do anything
            return;
        }
    	 
        String location = (String)ruleAction.getParameterValue(PARAM_LOCATION);
        String geolocation = (String)ruleAction.getParameterValue(PARAM_GEOLOCATION);
        String reason = (String)ruleAction.getParameterValue(PARAM_REASON);
        String position = (String)ruleAction.getParameterValue(PARAM_POSITION);
        String keyPassword = (String)ruleAction.getParameterValue(PARAM_KEY_PASSWORD);
        String signatureJson = (String)ruleAction.getParameterValue(PARAM_SIGNATURE_JSON);
        Boolean visible = (Boolean)ruleAction.getParameterValue(PARAM_VISIBLE);
        Boolean graphic = (Boolean)ruleAction.getParameterValue(PARAM_GRAPHIC);
        
        boolean useSignatureField = false;
        String user = AuthenticationUtil.getRunAsUser();
        String positionType = "predefined";
        String positionLoc = "center";
        JSONObject box;
        int page = -1;
        
        // parse out the position JSON
        JSONObject positionObj = null;
        
        try {
        	positionObj = (JSONObject)parser.parse(position);
        } catch (ParseException e) {
			logger.error("Could not parse position JSON from Share");
			throw new AlfrescoRuntimeException("Could not parse position JSON from Share");
		}
        
        // get the page
        page = Integer.parseInt(String.valueOf(positionObj.get("page")));
        
        // get the positioning type
        positionType = String.valueOf(positionObj.get("type"));
        
        // get the position (field or predefined)
        positionLoc = String.valueOf(positionObj.get("position"));
        
        // get the box (if required)
        box = (JSONObject)positionObj.get("box");
        
        int width = 350;
        int height = 75;
        
        File tempDir = null;

        // current date, used for both signing the PDF and creating the
        // associated signature object
        Calendar now = Calendar.getInstance();
        
        try
        {
        	// get the keystore, pk and cert chain
        	SignatureProvider signatureProvider = signatureProviderFactory.getSignatureProvider(user);
            KeyStore keystore = signatureProvider.getUserKeyStore(keyPassword);
            PrivateKey key = (PrivateKey)keystore.getKey(alias, keyPassword.toCharArray());
            Certificate[] chain = keystore.getCertificateChain(alias);
            
            // open original pdf
            ContentReader pdfReader = getReader(actionedUponNodeRef);
            PdfReader reader = new PdfReader(pdfReader.getContentInputStream());
            
            // create temp dir to store file
            File alfTempDir = TempFileProvider.getTempDir();
            tempDir = new File(alfTempDir.getPath() + File.separatorChar + actionedUponNodeRef.getId());
            tempDir.mkdir();
            File file = new File(tempDir, serviceRegistry.getFileFolderService().getFileInfo(actionedUponNodeRef).getName());
            OutputStream cos = serviceRegistry.getContentService().getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true).getContentOutputStream();
            
            PdfStamper stamp = PdfStamper.createSignature(reader, cos, '\0', file, true);
            PdfSignatureAppearance sap = stamp.getSignatureAppearance();
            sap.setCrypto(key, chain, null, PdfSignatureAppearance.SELF_SIGNED);

            // set reason for signature, location of signer, and date
            sap.setReason(reason);
            sap.setLocation(location);
            sap.setSignDate(now);
            
            // get the image for the signature
           	BufferedImage sigImage = SignatureToImage.convertJsonToImage(signatureJson, width, height);
       		// save the signature image back to the signatureProvider
       		signatureProvider.saveSignatureImage(sigImage, signatureJson);
       		
       		if(visible)
       		{	
       			//if this is a graphic sig, set the graphic here
           		if(graphic)
           		{
           			sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC);
	           		sap.setSignatureGraphic(Image.getInstance(sigImage, Color.WHITE));
           		}
           		else 
           		{
           			sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
           		}
           		
           		// either insert the sig at a defined field or at a defined position / drawn loc
	           	if(positionType.equalsIgnoreCase(POSITION_TYPE_PREDEFINED))
	           	{
	           		Rectangle pageRect = reader.getPageSizeWithRotation(page);
	           		sap.setVisibleSignature(positionBlock(positionLoc, pageRect, width, height), page, null);
	           	}
	           	else if(positionType.equalsIgnoreCase(POSITION_TYPE_DRAWN))
	           	{
	           		Rectangle pageRect = reader.getPageSizeWithRotation(page);
	           		sap.setVisibleSignature(positionBlock(pageRect, box), page, null);
	           	}
	           	else
	           	{
		           	sap.setVisibleSignature(positionLoc);
		           	useSignatureField = true;
	           	}
       		}
       		
           	// close the stamp, applying the changes to the PDF
            stamp.close();
            reader.close();
            cos.close();
            
            //delete the temp file
            file.delete();
            
            // apply the "signed" aspect
            serviceRegistry.getNodeService().addAspect(actionedUponNodeRef, CounterSignSignatureModel.ASPECT_SIGNED, new HashMap<QName, Serializable>());
            
            // create a "signature" node and associate it with the signed doc
            addSignatureNodeAssociation(actionedUponNodeRef, location, reason, 
            		useSignatureField ? positionLoc : "none", now.getTime(), geolocation, page, positionLoc);
            
        }
        catch (IOException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
        catch (ContentIOException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        }
        catch (DocumentException e)
        {
            throw new AlfrescoRuntimeException(e.getMessage(), e);
        } 
        catch (KeyStoreException e) 
        {
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		}
        catch (UnrecoverableKeyException e) 
		{
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		} 
        catch (NoSuchAlgorithmException e) 
		{
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		}
        finally
        {
            if (tempDir != null)
            {
                try
                {
                    tempDir.delete();
                }
                catch (Exception ex)
                {
                    throw new AlfrescoRuntimeException(ex.getMessage(), ex);
                }
            }
        }
    }
    
    /**
     * Scales the signature image to fit the provided signature field dimensions,
     * preserving the aspect ratio
     * 
     * @param signatureImage
     * @param width
     * @param height
     * @return
     */
    private BufferedImage scaleSignature(BufferedImage signatureImage, int width, int height)
    {
    	if(signatureImage.getHeight() > height)
    	{
	    	BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    	AffineTransform at = new AffineTransform();
	    	at.scale(2.0, 2.0);
	    	AffineTransformOp scaleOp = 
	    	   new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
	    	scaled = scaleOp.filter(signatureImage, scaled);
	    	return scaled;
    	}
    	else
    	{
    		return signatureImage;
    	}
    }

}
