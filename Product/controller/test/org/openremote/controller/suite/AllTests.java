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
package org.openremote.controller.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.Assert;
import org.openremote.controller.net.MulticastAutoDiscoveryTest;
import org.openremote.controller.utils.MacrosIrDelayUtilTest;
import org.openremote.controller.model.PanelTest;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.spring.SpringContext;

/**
 * Collects *all* unit tests. Also, the implementation contains utility methods in common across
 * various test suites.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
@RunWith(Suite.class) @SuiteClasses(
{
   AllControlBuilderTests.class,
   AllServiceTests.class,
   MacrosIrDelayUtilTest.class,
   AllCommandBuildersTests.class,
   MulticastAutoDiscoveryTest.class,
   RoundRobinTests.class,
   AllUtilTests.class,
    
   RESTTests.class,
   KNXTests.class,
   VirtualProtocolTests.class,
   PanelTest.class

})


public class AllTests
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Currently used test port for the embedded container hosting HTTP/REST implementation
   */
  public final static int WEBAPP_PORT = 8090;

  /**
   * Localhost IP address.
   */
  public final static String LOCALHOST = "127.0.0.1";

  /**
   * Path to test fixture directory.
   */
  public final static String FIXTURE_DIR = "org/openremote/controller/fixture/";

  /**
   * Path to polling related fixture files.
   */
  public final static String POLLING_FIXTURES = FIXTURE_DIR + "polling/";


  // Class Members --------------------------------------------------------------------------------


  static
  {
    try
    {
      new SpringContext();
    }
    catch (Throwable t)
    {
      System.err.println(
          "=================================================================================\n\n" +

          " Cannot initialize tests: " + t.getMessage() + "\n\n" +

          " Stack Trace: \n\n"
      );

      t.printStackTrace(System.err);
    }
  }


  /**
   * Returns a path to a fixture (resource) file used by tests. Test fixtures are stored in their
   * own (fixed) location in the test directories. This method resolves the path to a given file
   * name in the directory.
   *
   * @param name    name of the fixture file (without path)
   *
   * @return        full path to the fixture file
   *
   * @throws  AssertionError   if the file is not found
   */
  public static String getFixtureFile(String name)
  {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();

    String resource = FIXTURE_DIR + name;

    Assert.assertNotNull("Got null resource from '" + resource + "'.", cl.getResource(resource));

    return cl.getResource(resource).getFile();
  }


}
