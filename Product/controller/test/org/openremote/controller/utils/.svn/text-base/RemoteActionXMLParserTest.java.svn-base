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
package org.openremote.controller.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.suite.AllTests;
import org.openremote.controller.command.RemoteActionXMLParser;


/**
 * The Class RemoteActionXMLParserTest.
 * 
 * @author Dan 2009-4-3
 */
public class RemoteActionXMLParserTest {
   
   private Document doc;
   
   private RemoteActionXMLParser remoteActionXMLParser = (RemoteActionXMLParser) SpringTestContext.getInstance()
         .getBean("remoteActionXMLParser");

   @Before
   public void setup() {
      String controllerXMLPath = this.getClass().getClassLoader().getResource(
            AllTests.FIXTURE_DIR + "controller.xml").getFile();
      doc = XMLUtil.getControllerDocument(controllerXMLPath);
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testqueryElementFromXMLById(){
      Element s = remoteActionXMLParser.queryElementFromXMLById(doc, "1");
     
      assertEquals("switch", s.getName());
      
      List<Element> es = s.getChildren();
      for (int i = 0; i < s.getChildren().size(); i++) {
         if (i == 0) {
            assertEquals("on", es.get(i).getName());
         } else if (i == 1) {
            assertEquals("off", es.get(i).getName());
         } else {
            assertEquals("include", es.get(i).getName());
         }
      }
      
   }
   @SuppressWarnings("unchecked")
   @Test
   public void testqueryElementFromXMLByName(){
      Element s = remoteActionXMLParser.queryElementFromXMLByName(doc, "commands");
      
      Assert.assertNotNull(s);
      assertEquals(5, s.getChildren().size());
      
      for (Element e : (List<Element>)s.getChildren()) {
         assertEquals("command", e.getName());
      }
      
   }
   @Test
   public void testqueryElementFromXMLByNameNotFound(){
      Element s = remoteActionXMLParser.queryElementFromXMLByName(doc, "xxx");
      
      Assert.assertNull(s);
      
   }
   @Test
   public void testqueryElementFromXMLByIdNotFound(){
      Element s = remoteActionXMLParser.queryElementFromXMLById(doc, "1111111111");
      
      Assert.assertNull(s);
      
   }
   
   @Test
   public void testqueryElementFromDefaultXMLByIdNotFound(){
      Assert.assertNull( remoteActionXMLParser.queryElementFromXMLById(doc, "1111111111"));
   }
   
   
   
}
