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
package org.alfresco.extension.countersign.model;

import org.alfresco.service.namespace.QName;

public interface CounterSignSignatureModel 
{
	// namespace
	static final String COUNTERSIGN_SIGNATURE_MODEL_1_0_URI = "http://countersign.it/model/signature/1.0";
	
	// signed aspect and associations
	static final QName ASPECT_SIGNED = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signed");
	static final QName ASSOC_SIGNATURES = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signatures");
	
	// signature type
	static final QName TYPE_SIGNATURE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signature");
	static final QName PROP_SIGNATUREDATE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signatureDate");
	static final QName PROP_REASON = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "reason");
	static final QName PROP_LOCATION = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "location");
	static final QName PROP_EXTERNALSIGNER = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "externalSigner");
	static final QName ASSOC_SIGNEDBY = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signedBy");
	static final QName PROP_SIGNATUREFIELD = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signatureField");
	static final QName PROP_SIGNATUREPAGE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signaturePage");
	static final QName PROP_SIGNATUREPOSITION = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signaturePosition");
	static final QName PROP_DOCHASH = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "docHash");
	
	// signable aspect and properties
	static final QName ASPECT_SIGNABLE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signable");
	static final QName PROP_SIGNATUREFIELDS = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signatureFields");
	
	// signer aspect and properties
	static final QName ASPECT_SIGNER = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signer");
	static final QName ASSOC_SIGNERSIGNATUREIMAGE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signerSignatureImage");
	static final QName ASSOC_SIGNERKEYSTORE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signerKeystore");
	static final QName ASSOC_SIGNERPUBLICKEY = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signerPublicKey");
	
	// pkcs12 key store type
	static final QName TYPE_PKCS12 = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "pkcs12");
	static final QName PROP_ALIASES = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "aliases");
	
	// signature image type
	static final QName TYPE_SIGNATUREIMAGE = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signatureImage");
	static final QName PROP_SIGNATUREJSON = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "signaureJson");

	// public key type
	static final QName TYPE_PUBLICKEY = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "publicKey");
	static final QName PROP_KEYENCODING = QName.createQName(COUNTERSIGN_SIGNATURE_MODEL_1_0_URI, "keyEncoding");
}
