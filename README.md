# This project is no longer being maintained. It does not fully work with the latest versions of Alfresco Share, nor does it currenly work with the Alfresco App Dev Framework (ADF). If somebody wants to take over the project, feel free.

CounterSign
===========

CounterSign is a digital / electronic signature solution for Alfresco.  It aims to be a self-contained set of extensions to the Alfresco environment that provides a simple signature environment for the end user.

Compliance:

While no software can claim to be fully FDA CFR21 Part 11 compliant (as the regulation is as much about process as it is software), the generation of signatures that are compliant with the regulation is one of the key design goals of this project.  The current release generates signatures that I believe meet all of the requirements of CFR21 Part 11, and I will make my best effort to ensure that this is maintained throughout future releases.  Please note that compliance with regulations is ultimately the responsibility of the user, and compliance must be validated.  No warranty or guarantee of compliance is implied or offered.

A.  BUILDING AND DEBUGGING COUNTERSIGN

Some of the Maven operations (such as running the project) require some additional
JVM settings:

export MAVEN_OPTS="-Xms256m -Xmx2048m -XX:MaxPermSize=512m -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"

Running this project:

mvn install -Prun

Building this project:

mvn clean package


B.  CREATING A ROOT SIGNING CERT, KEYS AND STORES

To generate a root CA key and cert:

1. Generate an RSA key for the root CA and store it in a file:

openssl genrsa -out ca.key 4096

2.  Create a self-signed root CA certificate

openssl req -new -x509 -days 3650 -key ca.key -out ca.crt

3.  Create a p12 file containing the key and certificate

openssl pkcs12 -export -in ca.crt -inkey ca.key -name countersign -out ca.p12

4.  Make note of the keystore password and alias (-name value), CounterSign will need this to access the key / cert


C.  KNOWN LIMITATIONS:

1.  Document previews generated by PDF2SWF will show "Signature not Verified" messages on all signatures.
This is a limitation of SWFTools.  If CounterSign is set up properly and the right signing
certificates are provided and trusted by Adobe Reader, the signatures should be properly verified
when viewed in Reader.


D.  LICENSING

CounterSign for Alfresco is an open source project.  The project code itself is released under the 
AGPL License.  The following components are used in CounterSign, and retain their own licenses,
as noted in the source code

iText - Affero GPL 3 (http://itextpdf.com/terms-of-use/agpl.php)

jQuery - MIT License (https://jquery.org/license/)

LeafletJS - custom open source license (https://github.com/Leaflet/Leaflet/blob/master/LICENSE)

JQuery SignaturePad - BSD (http://thomasjbradley.ca/lab/signature-pad/#license)

TimelineJS - Mozilla Public License 2.0 (https://github.com/VeriteCo/TimelineJS#license)

