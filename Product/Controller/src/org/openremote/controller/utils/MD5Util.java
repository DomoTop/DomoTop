package org.openremote.controller.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

public class MD5Util {
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
   
}
