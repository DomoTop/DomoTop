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

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.ConsoleHandler;

import org.openremote.controller.Constants;

/**
 * This is a facade for the java.util.logging API to provide additional convenience methods that
 * closely matches those used in log4j API.  <p>
 *
 * Classes in controller implementation should use this API for maximum portability and to reduce
 * direct compile-time dependencies to third party libraries. <p>
 * 
 * Logging categories should be child groups of
 * {@link org.openremote.controller.Constants#CONTROLLER_ROOT_LOG_CATEGORY}.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class Logger extends java.util.logging.Logger
{

  /**
   * Reference to the global JUL log manager.
   */
  private static LogManager logManager = LogManager.getLogManager();


  /**
   * Returns a logger instance registered with a global java.util.logging LogManager.  <p>
   *
   * This instance provides additional convenience API on top of the default
   * java.util.logging.Logger API to make it easier to switch implementations between JUL
   * and Log4j.
   *
   * @param name  A log category name. This should always be a subcategory of
   *              {@link Constants#CONTROLLER_ROOT_LOG_CATEGORY}.
   *
   * @return      A JUL Logger subclass with additional convenience API for logging.
   */
  public static Logger getLogger(String name)
  {

    // Sanity check and warn of suspicious use pattern...

    if (!name.startsWith(Constants.CONTROLLER_ROOT_LOG_CATEGORY))
    {
      java.util.logging.Logger.getLogger("").warning(
          "Log category '" + name + "' is not using parent log category " +
          Constants.CONTROLLER_ROOT_LOG_CATEGORY + ". The logging behavior not be " +
          "what was expected."
      );
    }

    Logger l = new Logger(name);

    logManager.addLogger(l);

    try
    {
      return (Logger)logManager.getLogger(name);
    }
    catch (ClassCastException e)
    {
      System.err.println(
          "\n\n" +
          "---------------------------------------------------------------------------------\n\n" +
          "  An incompatible logger was already associated " +
          "  with category '" + name + "'.\n\n" +

          "  Logging to this category is unlikely to work as intended.\n\n" +

          "---------------------------------------------------------------------------------\n\n"
      );

      // The logging is unlikely to work but return a valid reference anyway to avoid
      // NPE's and such...

      l.addHandler(new ConsoleHandler());

      return l;
    }
  }


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Private constructor accessed from {@link #getLogger(String)}
   *
   * @param name    log category name
   */
  private Logger(String name)
  {
    super(name, null /* no localization */);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Synonymous to using {@link java.util.logging.Logger#severe}.
   *
   * Further, {@link org.openremote.controller.bootstrap.Startup#redirectJULtoLog4j()} maps
   * JUL {@link java.util.logging.Level#SEVERE} to log4j <tt>ERROR</tt> priority.
   *
   * @param msg   log message
   */
  public void error(String msg)
  {
    super.severe(msg);
  }

  /**
   * Same as {@link #error} with an additional exception stack trace added to the logging record.
   *
   * @param msg         log message
   * @param throwable   exception or error associated with the log message
   */
  public void error(String msg, Throwable throwable)
  {
    super.log(Level.SEVERE, msg, throwable);
  }


  /**
   * Synonymous to using {@link java.util.logging.Logger#warning}.
   *
   * @param msg   log message
   */
  public void warn(String msg)
  {
    super.warning(msg);
  }

  /**
   * Same as {@link #warn} with an additional exception stack trace added to the logging record.
   *
   * @param msg         log message
   * @param throwable   exception or error associated with the log message
   */
  public void warn(String msg, Throwable throwable)
  {
    super.log(Level.WARNING, msg, throwable);
  }


  /**
   * Synonymous to using {@link java.util.logging.Logger#fine}.
   *
   * Further, {@link org.openremote.controller.bootstrap.Startup#redirectJULtoLog4j()} maps
   * JUL {@link java.util.logging.Level#FINE} to log4j <tt>DEBUG</tt> priority.
   *
   * @param msg   log message
   */
  public void debug(String msg)
  {
    super.fine(msg);
  }

  /**
   * Same as {@link #debug} with an additional exception stack trace added to the logging record.
   *
   * @param msg         log message
   * @param throwable   exception or error associated with the log message
   */
  public void debug(String msg, Throwable throwable)
  {
    super.log(Level.FINE, msg, throwable);
  }


}

