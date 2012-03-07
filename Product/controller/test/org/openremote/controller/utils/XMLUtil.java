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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.InvalidReferenceException;
import org.openremote.controller.exception.NoSuchComponentException;

/**
 * This class supply some utility method for operating xml file, such as get the document for
 * panel.xml, controller.xml, get element by element id,
 *
 * @author Javen
 */
public class XMLUtil
{

  /**
   * Get a document for a user referenced panel.xml with xsd validation.
   *
   * @param xmlPath the file path for panel.xml
   * @return a builded document for panel.xml.
   */
  public static Document getPanelDocument(String xmlPath)
  {
    SAXBuilder builder = new SAXBuilder();
    Document doc = null;

    try
    {
      builder.setValidation(true);
      File xsdfile = new File(XMLUtil.class.getResource(Constants.PANEL_XSD_PATH).getPath());

      builder.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
      builder.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
      doc = builder.build(xmlPath);
    }

    catch (JDOMException e)
    {
      throw new RuntimeException(e);
    }

    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return doc;
  }


  /**
   * Get a document for a user referenced controller.xml with xsd validation.
   *
   * @param xmlPath xmlPath the file path for controller.xml
   * @return a builded document for controller.xml.
   */
  public static Document getControllerDocument(String xmlPath)
  {
    SAXBuilder builder = new SAXBuilder();
    Document doc = null;

    try
    {
      builder.setValidation(true);
      File xsdfile = new File(XMLUtil.class.getResource(Constants.CONTROLLER_XSD_PATH).getPath());

      builder.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
      builder.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
      doc = builder.build(xmlPath);
    }

    catch (JDOMException e)
    {
      throw new RuntimeException(e);
    }

    catch (IOException e)
    {
      throw new RuntimeException(e);
    }

    return doc;
  }


//  /**
//   * Build a document for a xml file referenced by user.
//   *
//   * @param xmlPath the file path for a xml file.
//   * @return a builded document for the xml file.
//   */
//  public static Document getDocument(String xmlPath)
//  {
//    SAXBuilder builder = new SAXBuilder();
//    Document doc = null;
//
//    try
//    {
//      doc = builder.build(xmlPath);
//    }
//
//    catch (JDOMException e)
//    {
//       e.printStackTrace();
//    }
//
//    catch (IOException e)
//    {
//       e.printStackTrace();
//    }
//
//    return doc;
//  }


  /**
   * Get a element by element id.
   *
   * @param doc The document for a xml file.
   * @param id a attribute for a element.  Different element must have different id.
   * @return a element which has such id.
   * @throws NoSuchComponentException if there is no such a element which not has the id.
   * @throws RuntimeException if the id is duplicated or the xml file is not a valid file.
   */
  @SuppressWarnings("unchecked")
  public static Element getElementByID(Document doc, String id)
  {
    String xpath = "//" + Constants.OPENREMOTE_NAMESPACE + ":*[@id='" + id + "']";

    try
    {
      XPath xPath = XPath.newInstance(xpath);
      xPath.addNamespace(Constants.OPENREMOTE_NAMESPACE, Constants.OPENREMOTE_WEBSITE);
      List<Element> elements = xPath.selectNodes(doc);

      if (elements.size() > 1)
      {
        throw new RuntimeException("duplicated id :" + id);
      }

      else if (elements.size() == 0)
      {
        throw new NoSuchComponentException("No such component id " + id);
      }

      return elements.get(0);
    }

    catch (JDOMException e)
    {
      throw new RuntimeException(e);
    }
  }

//  /**
//   * Validate the controller.xml
//   *
//   * @param xmlPath the file path for controller.xml
//   * @return true if valid. false if not.
//   */
//  public static boolean validateControlleXML(String xmlPath)
//  {
//    SAXBuilder builder = new SAXBuilder();
//    builder.setValidation(true);
//    File xsdfile = new File(XMLUtil.class.getResource(Constants.CONTROLLER_XSD_PATH).getPath());
//
//    builder.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
//    builder.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
//
//    try
//    {
//      builder.build(xmlPath);
//      return true;
//    }
//
//    catch (Exception e)
//    {
//      e.printStackTrace();
//      return false;
//    }
//  }


//  /**
//   * Validate the panel.xml
//   *
//   * @param xmlPath the file path for panel.xml.
//   * @return true if valid, false if not.
//   */
//  public static boolean validatePanelXML(String xmlPath)
//  {
//    SAXBuilder builder = new SAXBuilder();
//    builder.setValidation(true);
//    File xsdfile = new File(XMLUtil.class.getResource(Constants.PANEL_XSD_PATH).getPath());
//
//    builder.setProperty(Constants.SCHEMA_LANGUAGE, Constants.XML_SCHEMA);
//    builder.setProperty(Constants.SCHEMA_SOURCE, xsdfile);
//
//    try
//    {
//      builder.build(xmlPath);
//      return true;
//    }
//
//    catch (Exception e)
//    {
//      e.printStackTrace();
//      return false;
//    }
//  }


//  /**
//   * Get all the referenced element.
//   *
//   * @param doc the document for your xml file.
//   * @return a list stored all the referenced element.
//   */
//  @SuppressWarnings("unchecked")
//  public static List<Element> getRefElements(Document doc)
//  {
//    String xpath = "//" + Constants.OPENREMOTE_NAMESPACE + ":include";
//
//    try
//    {
//      XPath xPath = XPath.newInstance(xpath);
//      xPath.addNamespace(Constants.OPENREMOTE_NAMESPACE, Constants.OPENREMOTE_WEBSITE);
//      List<Element> elements = xPath.selectNodes(doc);
//      return elements;
//    }
//
//    catch (JDOMException e)
//    {
//      throw new RuntimeException(e);
//    }
//  }


//  /**
//   * Validate the xml file by validating the referencing element. <p>
//   *
//   * If you referenced a not exist element by id, or the element have the id exist but the type
//   * is not the same as the reference element's attribute <b>type</b> a InvalidReferenceExcepiton
//   * will be thrown.
//   *
//   * @param doc the document for your xml file.
//   * @throws InvalidReferenceException if the element you referenced not exist.
//   */
//  public static void validateRef(Document doc)
//  {
//    List<Element> refEles = getRefElements(doc);
//
//    if (refEles.size() > 0)
//    {
//      try
//      {
//        for (Element ele : refEles)
//        {
//          String refId = ele.getAttributeValue("ref");
//          Element refEle = getElementByID(doc, refId);
//          String includeType = ele.getAttributeValue("type");
//
//          if (!includeType.equals(refEle.getName()))
//          {
//            throw new InvalidReferenceException("No such a(an) " + includeType + " referenced by element\""
//                  + ele.getName() + "\" have an id :" + refId);
//          }
//        }
//      }
//
//      catch (NoSuchComponentException e)
//      {
//        throw new InvalidReferenceException("referenced element not exist", e);
//      }
//    }
//  }

}
