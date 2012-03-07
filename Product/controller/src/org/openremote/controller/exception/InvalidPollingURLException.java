package org.openremote.controller.exception;

@SuppressWarnings("serial")
public class InvalidPollingURLException extends ControlCommandException {

   @Override
   public int getErrorCode() {
      return ControlCommandException.INVALID_POLLING_URL;
   }

   @Override
   public void setErrorCode(int errorCode) {
      super.setErrorCode(ControlCommandException.INVALID_POLLING_URL);
   }

   public InvalidPollingURLException() {
      super();
   }

   public InvalidPollingURLException(String message, Throwable cause) {
      super(message, cause);
   }

   public InvalidPollingURLException(String message) {
      super(message);
   }

   public InvalidPollingURLException(Throwable cause) {
      super(cause);
   }
   
   
}
