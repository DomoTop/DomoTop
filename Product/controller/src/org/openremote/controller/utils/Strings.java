/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.utils;

/**
 * String related utilities.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class Strings
{

  /**
   * Translates a signed Java byte into an unsigned hex string.
   *
   * For example:
   *
   * <pre>{@code
   *
   *    1  -> 0x01
   *   15  -> 0x0F
   *   16  -> 0x10
   *   -1  -> 0xFF
   * -128  -> 0xC0
   *
   * }</pre>
   *
   * @param b   byte to convert
   *
   * @return    Unsigned hex representation of the byte value with a '0x' prefix. Returned values
   *            are always padded to two digits, so '1' becomes '0x01'.
   */
  public static String byteToUnsignedHexString(byte b)
  {
    int value = b;

    value &= 0xFF;
    
    return ( value < 16 ? "0x0" + Integer.toHexString(value).toUpperCase()
                        : "0x"  + Integer.toHexString(value).toUpperCase());
  }


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Only utility class methods.
   */
  private Strings()
  {}

}
