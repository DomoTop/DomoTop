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

import java.util.List;

import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.Constants;
import org.openremote.controller.suite.AllTests;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.component.control.slider.Slider;
import org.openremote.controller.component.control.slider.SliderBuilder;
import org.openremote.controller.exception.InvalidElementException;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.utils.SpringTestContext;

/**
 * Test cases for SliderBuilder.
 * 
 * @author Handy.Wang 2009-11-10
 */
public class SliderBuilderTest {
   
   private String controllerXMLPath = null;
   
   private Document doc = null;
   
   private SliderBuilder sliderBuilder = (SliderBuilder) SpringTestContext.getInstance().getBean("sliderBuilder");
   
   @Before
   public void setUp() throws Exception {
      controllerXMLPath = this.getClass().getClassLoader().getResource(AllTests.FIXTURE_DIR + "controller.xml").getFile();
      SAXBuilder builder = new SAXBuilder();
      doc = builder.build(controllerXMLPath);
   }
   
   /** Get invalid slider with control id from controller.xml. */
   @Test
   public void testGetInvalidSlider() throws JDOMException{
      try {
         getSliderByID("1");
         fail("Valid slider");
      } catch (InvalidElementException e) {
      }
   }
   
   /** Get a non-null slider and it's valid. */
   @Test
   public void testGetSliderNotNull() throws JDOMException{
      Slider slider = getSliderByID("8");
      Assert.assertNotNull(slider);
   }
   
   /** Get slider with control id from controller.xml but the control don't exsit in controller.xml.  */
   @Test
   public void testGetSliderNoSuchID() throws JDOMException{
      Slider slider  = null;
      try{
         slider = getSliderByID("13");
      }catch(Exception e){
         
      }
      Assert.assertNull(slider);
   }
   
   /** Get the slider and check whether the executable commands are null. */
   @Test
   public void testGetExecutableCommandsOfSlider() throws JDOMException {
      Slider slider = getSliderByID("8");
      Assert.assertNotNull(slider.getExecutableCommands());
      Assert.assertTrue(slider.getExecutableCommands().size() > 0);
      for (ExecutableCommand executableCommand : slider.getExecutableCommands()) {
         executableCommand.send();
      }
   }
   
   @SuppressWarnings("unchecked")
   private Element getElementByID(String id) throws JDOMException {
      String xpath = "//" + Constants.OPENREMOTE_NAMESPACE + ":*[@id='" + id + "']";
      XPath xPath = XPath.newInstance(xpath);
      xPath.addNamespace(Constants.OPENREMOTE_NAMESPACE, Constants.OPENREMOTE_WEBSITE);
      List<Element> elements = xPath.selectNodes(doc);
      if (elements.size() > 1) {
         throw new RuntimeException("duplicated id :" + id);
      } else if (elements.size() == 0) {
         throw new NoSuchComponentException();
      }
      return elements.get(0);
   }
   
   private Slider getSliderByID(String sliderID) throws JDOMException{
      Element controlElement = getElementByID(sliderID);
      return (Slider) sliderBuilder.build(controlElement, "20");
   }
}
