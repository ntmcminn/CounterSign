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

import java.util.List;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CounterSignServiceImpl implements CounterSignService
{
	private ServiceRegistry 	serviceRegistry;
    private static Log			logger 					= LogFactory.getLog(CounterSignServiceImpl.class);

	/**
	 * Gets the named signature artifact for the named user, delegates to 
	 * getSignatureArtifact(NodeRef, QName).  Just a convenience method
	 */
	public NodeRef getSignatureArtifact(String person, QName assoc)
	{
		NodeRef personNode = serviceRegistry.getPersonService().getPerson(person);
		return getSignatureArtifact(personNode, assoc);
	}
	
	/**
	 * Gets the named signature artifact for the provided user.  Used for
	 * retrieving keystores, public keys and signature images.
	 * 
	 * @param person
	 * @param assoc
	 * @return
	 */
	public NodeRef getSignatureArtifact(NodeRef person, QName assoc)
    {
		NodeRef node = null;
		List<ChildAssociationRef> nodes = serviceRegistry.getNodeService()
				.getChildAssocs(
						person, 
						assoc, 
						null, 
						Integer.MAX_VALUE, 
						false);

		if(nodes != null && nodes.size() > 0)
		{
			node = nodes.get(0).getChildRef();
		}

		return node;
    }
	
	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
}
