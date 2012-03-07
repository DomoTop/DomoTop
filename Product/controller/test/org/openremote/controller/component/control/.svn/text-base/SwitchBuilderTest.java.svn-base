/* OpenRemote, the Home of the Digital Home.
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
package org.openremote.controller.component.control;

import static org.junit.Assert.fail;
import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.suite.AllTests;
import org.openremote.controller.component.Sensory;
import org.openremote.controller.component.control.switchtoggle.Switch;
import org.openremote.controller.component.control.switchtoggle.SwitchBuilder;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.utils.SpringTestContext;
import org.openremote.controller.utils.XMLUtil;
/**
 * 
 * @author Javen
 *
 */
public class SwitchBuilderTest {
   private String controllerXMLPath = null;
   private Document doc = null;
   private SwitchBuilder builder = (SwitchBuilder) SpringTestContext.getInstance().getBean("switchBuilder");

   @Before
   public void setUp() throws Exception {
      controllerXMLPath = this.getClass().getClassLoader().getResource(AllTests.FIXTURE_DIR + "controller.xml")
            .getFile();
      doc = XMLUtil.getControllerDocument(controllerXMLPath);
   }

   protected Element getElementByID(String id) {
      return XMLUtil.getElementByID(doc, id);
   }

   private Switch getSwitchByID(String switchID, String cmdParam) {
      Element controlElement = getElementByID(switchID);
      if (!controlElement.getName().equals("switch")) {
         throw new NoSuchComponentException("switch .");
      }
      return (Switch) builder.build(controlElement, cmdParam);
   }

   @Test
   public void testNuSuchSwitch() {
      try {
         getSwitchByID("9", "on");
         fail();
      } catch (Exception e) {
      };
   }

   @Test
   public void testNoNull() {
      Assert.assertNotNull(getSwitchByID("3", "on"));
   }

   @Test
   public void testGetCommand() {
      Switch swh = getSwitchByID("4", "on");
      Assert.assertEquals(swh.getExecutableCommands().size(), 1);
      swh = getSwitchByID("4", "off");
      Assert.assertEquals(swh.getExecutableCommands().size(), 1);

      swh = getSwitchByID("4", "status");
      Assert.assertTrue(swh instanceof Sensory);
      Assert.assertTrue(((Sensory)swh).fetchSensorID() == 1004);
   }
}
