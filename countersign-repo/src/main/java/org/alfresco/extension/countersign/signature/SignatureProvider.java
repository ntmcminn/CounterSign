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
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface SignatureProvider {

	/**
	 * Gets the private key to be used
	 * 
	 * @return
	 */
	public KeyStore getUserKeyStore(String storePassword);
	
	/**
	 * Gets a BufferedImage representation of the signature
	 * 
	 * @return
	 */
	public BufferedImage getSignatureImage();
	
	/**
	 * Gets the source for the signature.  This could be JSON, for the built in
	 * sig pad, or it could be a URL, depending on implementation
	 * 
	 * @return
	 */
	public String getSignatureSource();
	
	/**
	 * Save the signature image provided by the user
	 * 
	 * @param image
	 */
	public void saveSignatureImage(BufferedImage image, String source);
	
	/**
	 * Checks to see if a signature is available
	 * 
	 * @return
	 */
	public boolean signatureAvailable();

	/**
	 * Gets a user's public Key
	 * 
	 * @return
	 */
	public PublicKey getPublicKey();
	
	/**
	 * Validate a signature and hash, using the user's CURRENT public key.  If a user invalidates
	 * their key by resetting it, this validation will fail.
	 * 
	 * @param sig
	 * @param hash
	 * @return
	 */
	public boolean validateSignature(byte[] sig, byte[] hash);
	
	/**
	 * Compute a hash using the configured algorithm.  Signatures that are later validated will be
	 * validated expecting the same algorithm.
	 * 
	 * @param contentStream
	 * @return
	 */
	public byte[] computeHash(InputStream contentStream);
	
	/**
	 * Sign a hash, using the user's private key
	 * @param hash
	 * @param key
	 * @return
	 */
	public byte[] signHash(byte[] hash, String storePassword) throws Exception ;
}
