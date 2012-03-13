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

package org.openremote.controller.command;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControllerXMLNotFoundException;
import org.openremote.controller.exception.InvalidControllerXMLException;
import org.openremote.controller.utils.PathUtil;


/**
 * The controller.xml Parser.
 * 
 * @author Dan 2009-4-3
 */
public class RemoteActionXMLParser {
   
   private static Logger logger = Logger.getLogger(RemoteActionXMLParser.class.getName());
   
   private ControllerConfiguration configuration;
   
   /**
    * Query element from default controller.xml by Id.
    * 
    * @param id
    *           element id
    * @return element
    */
   public Element queryElementFromXMLById(String id) {
      return queryElementFromXML("//" + Constants.OPENREMOTE_NAMESPACE + ":*[@id='" + id + "']");
   }
   
   /**
    * Query element from a specified controller.xml by Id.
    * 
    * @param doc
    *           specified controller.xml doc
    * @param id
    *           element id
    * @return element
    */
   public Element queryElementFromXMLById(Document doc, String id) {
      return queryElementFromXML(doc, "//" + Constants.OPENREMOTE_NAMESPACE + ":*[@id='" + id + "']");
   }
   
   public Element queryElementFromXMLByName(String elementName) {
      return queryElementFromXML("//" + Constants.OPENREMOTE_NAMESPACE + ":" + elementName);
   }
   
   public List<Element> queryElementsFromXMLByName(Document doc, String elementName) {
      String xPathStr = "//" + Constants.OPENREMOTE_NAMESPACE + ":" + elementName;
      return queryElementsFromXML(doc, xPathStr);
   }
   
   public List<Element> queryElementsFromXMLByName(String elementName) {
	  String xPath = "//" + Constants.OPENREMOTE_NAMESPACE + ":" + elementName;
	  SAXBuilder sb = new SAXBuilder();
      sb.setValidation(true);
      try {
         File xsdfile = new File(URLDecoder.decode(getClass().getResource(Constants.CONTROLLER_XSD_PATH).getPath(), "UTF-8"));
         sb.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
      } catch (UnsupportedEncodingException e) {
         logger.error("The controller xsd file path unsupported encoding.", e);
      }

      sb.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
      String xmlPath = PathUtil.addSlashSuffix(configuration.getResourcePath()) + Constants.CONTROLLER_XML;
      if (!new File(xmlPath).exists()) {
         throw new ControllerXMLNotFoundException(" Make sure it's in " + configuration.getResourcePath());
      }
      try {
         Document doc = sb.build(new File(xmlPath));
         return queryElementsFromXML(doc, xPath);
      } catch (JDOMException e) {
         logger.error("JDOMException occurs when parsing controller.xml.", e);
         throw new InvalidControllerXMLException(e.getMessage() + 
               " check the version of schema or structure of controller.xml with "
               + Constants.CONTROLLER_XSD_PATH);
      } catch (IOException e) {
         String msg = " An I/O error prevents a controller.xml from being fully parsed";
         logger.error(msg, e);
         throw new ControllerXMLNotFoundException(msg);
      }
   }   
   /**
    * Basic method for query element with document context and xPath string.
    * @param doc
    * @param xPath
    * @return
    */
   @SuppressWarnings("unchecked")
   private List<Element> queryElementsFromXML(Document doc, String xPath) {
      try {
         XPath xpath = XPath.newInstance(xPath);
         xpath.addNamespace(Constants.OPENREMOTE_NAMESPACE, Constants.OPENREMOTE_WEBSITE);
         List<Element> elements = xpath.selectNodes(doc);
         if (!elements.isEmpty()) {
            return elements;
         }
      } catch (JDOMException e) {
         logger.error("JDOMException occurs when parsing controller.xml.", e);
         throw new InvalidControllerXMLException("check the version of schema or structure of controller.xml with "
               + Constants.CONTROLLER_XSD_PATH);
      }
      return null;
   }
   
   public Element queryElementFromXMLByName(Document doc, String elementName) {
      return queryElementFromXML(doc, "//" + Constants.OPENREMOTE_NAMESPACE + ":" + elementName);
   }
   
   private Element queryElementFromXML(String xPath) {
      SAXBuilder sb = new SAXBuilder();
      sb.setValidation(true);
      try {
         File xsdfile = new File(URLDecoder.decode(getClass().getResource(Constants.CONTROLLER_XSD_PATH).getPath(), "UTF-8"));
         sb.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
      } catch (UnsupportedEncodingException e) {
         logger.error("The controller xsd file path unsupported encoding.", e);
      }
      
      sb.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
      String xmlPath = PathUtil.addSlashSuffix(configuration.getResourcePath()) + Constants.CONTROLLER_XML;
      if (!new File(xmlPath).exists()) {
         throw new ControllerXMLNotFoundException(" Make sure it's in " + configuration.getResourcePath());
      }
      try {
         Document doc = sb.build(new File(xmlPath));
         return queryElementFromXML(doc, xPath);
      } catch (JDOMException e) {
         logger.error("JDOMException occurs when parsing controller.xml.", e);
         throw new InvalidControllerXMLException(e.getMessage() + 
               " check the version of schema or structure of controller.xml with "
               + Constants.CONTROLLER_XSD_PATH);
      } catch (IOException e) {
         String msg = " An I/O error prevents a controller.xml from being fully parsed";
         logger.error(msg, e);
         throw new ControllerXMLNotFoundException(msg);
      }
   }
   
   @SuppressWarnings("unchecked")
   private Element queryElementFromXML(Document doc, String xPath) {
      try {
         XPath xpath = XPath.newInstance(xPath);
         xpath.addNamespace(Constants.OPENREMOTE_NAMESPACE, Constants.OPENREMOTE_WEBSITE);
         List<Element> elements = xpath.selectNodes(doc);
         if (!elements.isEmpty()) {
            return elements.get(0);
         }
      } catch (JDOMException e) {
         logger.error("JDOMException occurs when parsing controller.xml.", e);
         throw new InvalidControllerXMLException("check the version of schema or structure of controller.xml with "
               + Constants.CONTROLLER_XSD_PATH);
      }
      return null;
   }
   
   public void setConfiguration(ControllerConfiguration configuration) {
      this.configuration = configuration;
   }

}
