package org.openremote.controller.service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface CertificateService 
{
   /**
    * Initialize CA Path, only if the CA Path is empty 
    * then get the CA Path from the XML configuration and save it in the database
    */
   void initCaPath();
   /**
    * Check if the CA certificate already exists in the server key store
    * @return true if exists else false
    */
   boolean ifCaExists();
   /**
    * Create the CA directory structure
    * @return true if directory structure is successfully created
    */
   boolean createDirectoryStructure();
   /**
    * Create a new CA, first dropping all clients from the database, create a new key pair
    * build a new certificate and finally store it in the key store
    * (if the server key store does not exists yet it will be created)
    * 
    * @return true when successfully created else false
    */
   boolean createCa();
   /**
    * Create a new client key store only when there is not yet a client key store
    * @return true when key store is successfully created
    * or true when key store already exists else false
    */
   boolean createClientKeyStore();
   /**
    * Saves the client certificate into a client key store file
    * 
    * @param cert the X509 certificate
    * @param alias the alias of the certificate
    * @return true if success, false if unsuccessfully
    */
   boolean saveCertificateToClientKeyStore(X509Certificate cert, String alias);
   /**
    * Delete the client certificate from the client key store file
    * 
    * @param alias of the client certificate that will be removed
    * @return true if successfully removed from the key store else false
    */
   boolean deleteClientFromClientKeyStore(String alias);
   /**
    * Delete client key store file
    * 
    * @return true if success otherwise false
    */
   boolean deleteClientKeyStore();
   /**
    * Get the certificate file from the csr directory and create and returns a PKCS10CertificationRequest object
    * 
    * @param alias client alias
    * @return PKCS10CertificationRequest object
    * @throws IOException
    */
   PKCS10CertificationRequest getCertificationRequest(String alias) throws IOException; 
   /**
    * Get the private key from the CA, using the server key store file   
    * 
    * @return PrivateKey Object
    */
   PrivateKey getCaPrivateKey();
   /**
    * Sign a certificate using a PCKS10 Certification request file and the PrivateKey from the CA
    * 
    * @param inputCSR PCKS10 Certification Request file
    * @param caPrivate PrivateKey from CA
    * @return a new signed certificate
    * @throws InvalidKeyException
    * @throws NoSuchAlgorithmException
    * @throws NoSuchProviderException
    * @throws SignatureException
    * @throws IOException
    * @throws OperatorCreationException
    * @throws CertificateException
    */
   X509Certificate signCertificate(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate, String serial)
         throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException,
         IOException, OperatorCreationException, CertificateException;   
   /**
    * Parse CSR File to pin, email, device name and cn
    * 
    * @param alias String alias of the csr
    */
   void parseCSRFile(String alias);   
   /**
    * CSR getter
    * @return Pin
    */
   String getPin();
   /**
    * CSR getter
    * @return e-mail
    */
   String getEmail();
   /**
    * CSR getter
    * @return device name
    */
   String getDeviceName();
   /**
    * CSR getter
    * @return cn
    */
   String getCN();
}