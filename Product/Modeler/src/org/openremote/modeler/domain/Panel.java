/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
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
package org.openremote.modeler.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Transient;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.domain.component.UITabbar;
import org.openremote.modeler.domain.component.UITabbarItem;
import org.openremote.modeler.touchpanel.TouchPanelCanvasDefinition;
import org.openremote.modeler.touchpanel.TouchPanelDefinition;

import flexjson.JSON;

/**
 * The Panel define the different device touch panel, such as iPhone panel, wall panel etc.
 */
public class Panel extends BusinessEntity {

   private static final long serialVersionUID = 6122936524433692761L;
   
   private static int defaultNameIndex = 1;
   private String name;
   private List<GroupRef> groupRefs = new ArrayList<GroupRef>();
   private List<UITabbarItem> tabbarItems = new ArrayList<UITabbarItem>();
   private TouchPanelDefinition touchPanelDefinition;
   
   private UITabbar tabbar = null; 
   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public List<GroupRef> getGroupRefs() {
      return groupRefs;
   }
   public void setGroupRefs(List<GroupRef> groupRefs) {
      this.groupRefs = groupRefs;
   }
   public void addGroupRef(GroupRef groupRef) {
      groupRefs.add(groupRef);
   }
   public void removeGroupRef(GroupRef groupRef) {
      groupRefs.remove(groupRef);
   }
   public TouchPanelDefinition getTouchPanelDefinition() {
      return touchPanelDefinition;
   }
   public void setTouchPanelDefinition(TouchPanelDefinition touchPanelDefinition) {
      this.touchPanelDefinition = touchPanelDefinition;
   }
   public void insertGroupRef(GroupRef before, GroupRef target) {
      int index = groupRefs.indexOf(before);
      groupRefs.add(index, target);
   }
   public List<UITabbarItem> getTabbarItems() {
      return tabbarItems;
   }
   public void setTabbarItems(List<UITabbarItem> tabbarItems) {
      this.tabbarItems = tabbarItems;
   }
   /* (non-Javadoc)
    * @see org.openremote.modeler.domain.BusinessEntity#getDisplayName()
    */
   @Transient
   public String getDisplayName() {
      TouchPanelCanvasDefinition canvas = touchPanelDefinition.getCanvas();
      return name + "(" + touchPanelDefinition.getName() + "," + canvas.getWidth() + "X" + canvas.getHeight() + ")";
   }
   
   @Transient
   public static String getNewDefaultName() {
      return "panel" + defaultNameIndex;
   }
   
   public void clearGroupRefs() {
      groupRefs.clear();
   }
   
   @Transient
   public static void increaseDefaultNameIndex() {
      defaultNameIndex++;
   }
   
   public String getType() {
      if (touchPanelDefinition != null) {
         return touchPanelDefinition.getType();
      }
      return Constants.CUSTOM_PANEL;
   }
   public UITabbar getTabbar() {
      return tabbar;
   }
   public void setTabbar(UITabbar tabbar) {
      this.tabbar = tabbar;
   }
   
   @Transient
   @JSON(include = false)
   public List<Group> getGroups() {
      List<Group> groups = new ArrayList<Group>();
      for (GroupRef groupRef : groupRefs) {
         groups.add(groupRef.getGroup());
      }
      return groups;
   }
}
