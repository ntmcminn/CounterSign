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

import java.util.Properties;

import org.alfresco.extension.countersign.service.CounterSignService;
import org.alfresco.service.ServiceRegistry;

public class RepositoryManagedSignatureProviderFactory implements SignatureProviderFactory
{

	private ServiceRegistry 	serviceRegistry;
	private CounterSignService	counterSignService;
	private Properties 			config 						= new Properties();
	
	public static final String	CONFIG_PREFIX 				= "countersign.signature.";
	public static final String 	VALID_DURATION 				= CONFIG_PREFIX + "validDuration";
	public static final String	ALIAS						= CONFIG_PREFIX + "alias";
	public static final String 	ENABLE_TRUSTED_CERTS 		= CONFIG_PREFIX + "enableTrustedCerts";
	public static final String	TRUSTED_KEYSTORE_PATH		= CONFIG_PREFIX + "trustedKeystorePath";
	public static final String	TRUSTED_KEYSTORE_PASSWORD 	= CONFIG_PREFIX + "trustedKeystorePassword";
	public static final String  TRUSTED_CERT_ALIAS			= CONFIG_PREFIX + "trustedCertAlias";
	public static final String	TRUSTED_KEY_ALIAS			= CONFIG_PREFIX + "trustedKeyAlias";
	public static final String	TRUSTED_KEY_PASSWORD		= CONFIG_PREFIX + "trustedKeyPassword";
	public static final String 	HASH_ALGORITHM				= CONFIG_PREFIX + "hashAlgorithm";
	public static final String	SIGNATURE_ALGORITHM			= CONFIG_PREFIX + "signatureAlgorithm";
	public static final String	JAVA_SIGNATURE_PROVIDER		= CONFIG_PREFIX + "java.signature.provider";
	
	public SignatureProvider getSignatureProvider(String user) {
		return new RepositoryManagedSignatureProvider(serviceRegistry, counterSignService, user, config);
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry)
	{
		this.serviceRegistry = serviceRegistry;
	}
	
	public void setValidDuration(String validDuration)
	{
		config.setProperty(VALID_DURATION, validDuration);
	}

	public void setAlias(String alias)
	{
		config.setProperty(ALIAS, alias);
	}
	
	public void setEnableTrustedCerts(String enableTrustedCerts)
	{
		config.setProperty(ENABLE_TRUSTED_CERTS, enableTrustedCerts);
	}
	
	public void setTrustedKeystorePath(String trustedKeystorePath)
	{
		config.setProperty(TRUSTED_KEYSTORE_PATH, trustedKeystorePath);
	}
	
	public void setTrustedKeystorePassword(String trustedKeystorePassword)
	{
		config.setProperty(TRUSTED_KEYSTORE_PASSWORD, trustedKeystorePassword);
	}
	
	public void setTrustedCertAlias(String trustedCertAlias)
	{
		config.setProperty(TRUSTED_CERT_ALIAS, trustedCertAlias);
	}
	
	public void setTrustedKeyAlias(String trustedKeyAlias)
	{
		config.setProperty(TRUSTED_KEY_ALIAS, trustedKeyAlias);
	}
	
	public void setTrustedKeyPassword(String trustedKeyPassword)
	{
		config.setProperty(TRUSTED_KEY_PASSWORD, trustedKeyPassword);
	}
	
	public void setCounterSignService(CounterSignService counterSignService)
	{
		this.counterSignService = counterSignService;
	}
	
	public void setHashAlgorithm(String hashAlgorithm)
	{
		config.setProperty(HASH_ALGORITHM, hashAlgorithm);
	}
	
	public void setSignatureAlgorithm(String signatureAlgorithm)
	{
		config.setProperty(SIGNATURE_ALGORITHM, signatureAlgorithm);
	}
	
	public void setJavaSignatureProvider(String javaSignatureProvider)
	{
		config.setProperty(JAVA_SIGNATURE_PROVIDER, javaSignatureProvider);
	}
}
