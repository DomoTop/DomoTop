package org.openremote.controller.protocol.lutron;

@SuppressWarnings("serial")
public class LutronHomeWorksDeviceException extends Exception {

  private LutronHomeWorksAddress address;
  private Class<? extends HomeWorksDevice> deviceClass;
  
  public LutronHomeWorksDeviceException(String message, LutronHomeWorksAddress address, Class<? extends HomeWorksDevice> deviceClass, Throwable cause) {
    super(message, cause);
    this.address = address;
    this.deviceClass = deviceClass;
  }

  public LutronHomeWorksAddress getAddress() {
    return address;
  }

  public Class<? extends HomeWorksDevice> getDeviceClass() {
    return deviceClass;
  }
  
}
