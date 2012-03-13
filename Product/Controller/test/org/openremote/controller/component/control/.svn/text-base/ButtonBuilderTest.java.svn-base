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
import org.openremote.controller.component.control.button.Button;
import org.openremote.controller.component.control.button.ButtonBuilder;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.utils.SpringTestContext;
import org.openremote.controller.utils.XMLUtil;
/**
 * 
 * @author Javen
 *
 */
public class ButtonBuilderTest {
   private String controllerXMLPath = null;
   private Document doc = null;
   private ButtonBuilder builder = (ButtonBuilder) SpringTestContext.getInstance().getBean("buttonBuilder");

   @Before
   public void setUp() throws Exception {
      controllerXMLPath = this.getClass().getClassLoader().getResource(AllTests.FIXTURE_DIR + "controller.xml")
            .getFile();
      doc = XMLUtil.getControllerDocument(controllerXMLPath);
   }

   protected Element getElementByID(String id) {
      return XMLUtil.getElementByID(doc, id);
   }

   private Button getButtonByID(String buttonID, String cmdParam) {
      Element controlElement = getElementByID(buttonID);
      if (!controlElement.getName().equals("button")) {
         throw new NoSuchComponentException("button .");
      }
      return (Button) builder.build(controlElement, cmdParam);
   }

   @Test
   public void testNoSuchButton() {
      try {
         getButtonByID("10", "click");
         fail();
      } catch (Exception e) {
      };
   }

   @Test
   public void testNotNull() {
      Button btn = getButtonByID("9", "click");
      Assert.assertNotNull(btn);
   }

   @Test
   public void testGetCommand() {
      Button btn = getButtonByID("9", "click");
      Assert.assertEquals(btn.getExecutableCommands().size(), 2);

      btn = getButtonByID("9", "click");
      Assert.assertEquals(btn.getExecutableCommands().size(), 2);
      btn = getButtonByID("9", "status");
      Assert.assertEquals(btn.getExecutableCommands().size(), 0);
   }
}
