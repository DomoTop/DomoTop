/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller.exception;

/**
 * Exception class to indicate errors during validating of the group 
 * of the device (using the client certificate)
 *
 *
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>
 */
@SuppressWarnings("serial")
public class InvalidGroupException extends ControlCommandException 
{

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Instantiates a new exception with no message or root cause.
   */
  public InvalidGroupException()
  {
    super();
    setErrorCode(ControlCommandException.INVALID_GROUP);
  }

  /**
   * Instantiates a new exception instance with a given message and root cause.
   *
   * @param message descriptive exception message
   * @param cause an exception that was the root cause of this error
   */
  public InvalidGroupException(String message, Throwable cause)
  {
    super(message, cause);
    setErrorCode(ControlCommandException.INVALID_GROUP);
  }

  /**
   * Instantiates a new exception instance with a given message.
   *
   * @param message descriptive exception message
   */
  public InvalidGroupException(String message)
  {
    super(message);
    setErrorCode(ControlCommandException.INVALID_GROUP);
  }
}
