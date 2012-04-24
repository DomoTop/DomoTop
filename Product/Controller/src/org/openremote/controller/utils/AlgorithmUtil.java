package org.openremote.controller.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Random;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;

public class AlgorithmUtil {
   static
   {
      Security.addProvider(new BouncyCastleProvider());
   }
   
   public static String generateMD5Sum(byte[] message) {
      byte[] resultByte = null;

      try {
         final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
         messageDigest.reset();
         messageDigest.update(message);
         resultByte = messageDigest.digest();
      } catch (NoSuchAlgorithmException e) {
      }
      return new String(Hex.encodeHex(resultByte));
   }
   
   public static String generateMD5Sum(String message) {
      byte[] resultByte = null;

      try {
         final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
         messageDigest.reset();
         messageDigest.update(message.getBytes());
         resultByte = messageDigest.digest();
      } catch (NoSuchAlgorithmException e) {
      }
      return new String(Hex.encodeHex(resultByte));
   }
   
   public static String generateSHA512(byte[] message)
   {
      byte[] resultByte = null;
      MessageDigest md = null;
      
      try {
         md = MessageDigest.getInstance("SHA-512");         
         md.update(message);
         resultByte =  md.digest();
      } catch (NoSuchAlgorithmException e) {
      }
      return new String(Hex.encodeHex(resultByte));
   }

   public static String getSalt() 
   {
      int size = 32;
      byte[] bytes = new byte[size];
      new Random().nextBytes(bytes);
      return new String(Base64.encode(bytes));
   }
}
