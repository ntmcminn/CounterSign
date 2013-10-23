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
package org.alfresco.extension.countersign.evaluators;

import java.util.Map;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.extensibility.impl.DefaultSubComponentEvaluator;

public class CounterSignSignatureTypeEvaluator extends DefaultSubComponentEvaluator{

	private final String SIG_TYPE_PDF = "pdf";
	private final String SIG_TYPE_PARAM = "sigType";
	
	/**
     * Decides if this is a signable doc or not
     *
     * @param context
     * @param params
     * @return true if this is a signable pdf document.
     */
    @Override
    public boolean evaluate(RequestContext context, Map<String, String> params)
    {
    	String type = context.getParameter(SIG_TYPE_PARAM);
    	
    	if(type != null && type.equals(SIG_TYPE_PDF))
    	{
    		return true;
    	}
    	else
    	{
    		return false;
    	}
    }
}
