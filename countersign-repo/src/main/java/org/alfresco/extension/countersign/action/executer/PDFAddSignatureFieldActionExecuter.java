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

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.extension.countersign.model.CounterSignSignatureModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfAnnotation;
import com.itextpdf.text.pdf.PdfBorderArray;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

public class PDFAddSignatureFieldActionExecuter extends 
	AbstractSignatureActionExecuter
{
    /**
     * The logger
     */
    private static Log                    logger                   = LogFactory.getLog(PDFSignatureProviderActionExecuter.class);
    
    private JSONParser 					  parser 				   = new JSONParser();
    
    public static final String			  PARAM_POSITION		   = "position";
    public static final String			  PARAM_FIELDNAME		   = "field-name";
	
    /**
     * Add parameter definitions
     */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_FIELDNAME, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_FIELDNAME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_POSITION, DataTypeDefinition.TEXT, true, getParamDisplayLabel(PARAM_POSITION)));
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.repository.NodeRef,
     * org.alfresco.service.cmr.repository.NodeRef)
     */
    protected void executeImpl(Action ruleAction, NodeRef actionedUponNodeRef)
    {
    	
    	NodeService ns = serviceRegistry.getNodeService();
    	if (ns.exists(actionedUponNodeRef) == false)
        {
            // node doesn't exist - can't do anything
            return;
        }
    	
    	String fieldName = (String)ruleAction.getParameterValue(PARAM_FIELDNAME);
        String position = (String)ruleAction.getParameterValue(PARAM_POSITION);
        
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
        
        // get the box
        box = (JSONObject)positionObj.get("box");
        
        try 
        {
	        // open original pdf
	        ContentReader pdfReader = getReader(actionedUponNodeRef);
	        PdfReader reader = new PdfReader(pdfReader.getContentInputStream());
	        OutputStream cos = serviceRegistry.getContentService().getWriter(actionedUponNodeRef, ContentModel.PROP_CONTENT, true).getContentOutputStream();
	        
	        PdfStamper stamp = new PdfStamper(reader, cos);
	        
	        // does a field with this name already exist?
	        AcroFields allFields = stamp.getAcroFields();
	    	
	    	// if this doc is already signed, cannot add a new sig field without 
	    	if(allFields.getSignatureNames() != null && allFields.getSignatureNames().size() > 0)
	    	{
	    		throw new AlfrescoRuntimeException("This document has signatures applied, "
	    				+ "adding a new signature field would invalidate existing signatures");
	    	}

	        // cant create duplicate field names
	        if(allFields.getFieldType(fieldName) == AcroFields.FIELD_TYPE_SIGNATURE)
	        {
	        	throw new AlfrescoRuntimeException("A signature field named " + fieldName + " already exists in this document");
	        }
	        
	        // create the signature field
	        Rectangle pageRect = reader.getPageSizeWithRotation(page);
	        Rectangle sigRect = positionBlock(pageRect, box);
	        PdfFormField sigField = stamp.addSignature(fieldName, page, sigRect.getLeft(), 
	        		sigRect.getBottom(), sigRect.getRight(), sigRect.getTop());
	        
	        // style the field (no borders)
	        sigField.setBorder(new PdfBorderArray(0,0,0));
	        sigField.setBorderStyle(new PdfBorderDictionary(0, PdfBorderDictionary.STYLE_SOLID));
	        allFields.regenerateField(fieldName);
	        
	        // apply the change and close streams
	        stamp.close();
	        reader.close();
	        cos.close();
	        
	        // once the signature field has been added, apply the sig field aspect
	        if(!ns.hasAspect(actionedUponNodeRef, CounterSignSignatureModel.ASPECT_SIGNABLE))
	        {
	        	ns.addAspect(actionedUponNodeRef, CounterSignSignatureModel.ASPECT_SIGNABLE, null);
	        }
	        
	        // now update the signature fields metadata
	        Serializable currentFields = ns.getProperty(actionedUponNodeRef, CounterSignSignatureModel.PROP_SIGNATUREFIELDS);
	        ArrayList<String> fields = new ArrayList<String>();
	        
	        if(currentFields != null)
	        {
	        	fields.addAll((List<String>)currentFields);
	        }
	        
	        fields.add(fieldName);
	        ns.setProperty(actionedUponNodeRef, CounterSignSignatureModel.PROP_SIGNATUREFIELDS,  fields);
        }
        catch(IOException ioex)
        {
        	throw new AlfrescoRuntimeException(ioex.getMessage());
        }
        catch(DocumentException dex)
        {
        	throw new AlfrescoRuntimeException(dex.getMessage());
        }
    }
}
