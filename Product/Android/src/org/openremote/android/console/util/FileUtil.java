/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2010, OpenRemote Inc.
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
package org.openremote.android.console.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openremote.android.console.Constants;
import org.openremote.android.console.bindings.Group;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.bindings.TabBar;
import org.openremote.android.console.model.XMLEntityDataBase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;
/**
 * File I/O utility.
 * 
 * @author Tomsky Wang
 * @author Dan Cong
 *
 */
public class FileUtil {
   
   /**
    * Checks whether a file exists by file name.
    * 
    * @param context
    *           Context instance
    * @param fileName
    *           file name
    * @return true if exists.
    */
   public static boolean checkFileExists(Context context, String fileName) {
      return context.getFileStreamPath(fileName).exists();
   }

   /**
    * Parses the panel from panel.xml.
    * 
    */
   public static void parsePanelXML(Context context) {
      if (context.getFileStreamPath(Constants.PANEL_XML).exists()) {
         try {
            parsePanelXMLInputStream(context.openFileInput(Constants.PANEL_XML));
         } catch (FileNotFoundException e) {
            Log.e("OpenRemote-FileUtil", "panel.xml not found.", e);
         }
      }
   }
   
   public static void parsePanelXMLInputStream(InputStream fIn) {
      try {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document dom = builder.parse(fIn);
         Element root = dom.getDocumentElement();
         NodeList nodes = root.getChildNodes();
         int nodeLength = nodes.getLength();
         XMLEntityDataBase.globalTabBar = null;
         for (int i = 0; i < nodeLength; i++) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE && "tabbar".equals(nodes.item(i).getNodeName())) {
               XMLEntityDataBase.globalTabBar = new TabBar(nodes.item(i));
            }
         }

         XMLEntityDataBase.screens.clear();
         NodeList screenNodes = root.getElementsByTagName("screen");
         int screenNum = screenNodes.getLength();
         for (int i = 0; i < screenNum; i++) {
            Screen screen = new Screen(screenNodes.item(i));
            XMLEntityDataBase.screens.put(screen.getScreenId(), screen);
         }

         XMLEntityDataBase.groups.clear();
         NodeList groupNodes = root.getElementsByTagName("group");
         int groupNum = groupNodes.getLength();
         for (int i = 0; i < groupNum; i++) {
            Group group = new Group(groupNodes.item(i));
            XMLEntityDataBase.groups.put(group.getGroupId(), group);
         }
      } catch (ParserConfigurationException e) {
         e.printStackTrace();
      } catch (SAXException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         try {
            fIn.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   /**
    * Clear images from cache.
    * 
    */
   public static void clearImagesInCache(Context context) {
      String[] fileNames = context.fileList();
      for (int i = 0; i < fileNames.length; i++) {
         if (fileNames[i].toLowerCase().matches("^.+\\.(png|gif|jpg|bmp)$")) {
            Log.i("OpenRemote-CLEAR IMAGE", fileNames[i]);
            context.deleteFile(fileNames[i]);
         }
      }
   }
   
}
