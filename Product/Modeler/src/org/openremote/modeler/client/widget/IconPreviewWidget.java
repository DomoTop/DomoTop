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
package org.openremote.modeler.client.widget;

import org.openremote.modeler.client.widget.component.FlexStyleBox;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;

public class IconPreviewWidget extends LayoutContainer {

   private FlexTable btnTable = new FlexStyleBox();

   private Text center = new Text();

   private Image image = new Image();
   
   public IconPreviewWidget(int width, int height) {
      setSize(width, height);
      addStyleName("screen-btn");
      addStyleName("button-border");
      add(btnTable);
   }
   
   public void setIcon(String icon) {
      if (icon != null) {
         image.setUrl(icon);
      }
      btnTable.removeStyleName("screen-btn-cont");
      btnTable.setWidget(1, 1, image);
   }
   
   public void setText(String text) {
      center.setText(text);
      btnTable.addStyleName("screen-btn-cont");
      btnTable.setWidget(1, 1, center);
   }
}
