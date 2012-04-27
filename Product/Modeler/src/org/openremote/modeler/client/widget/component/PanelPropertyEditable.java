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
package org.openremote.modeler.client.widget.component;

import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.utils.PropertyEditable;
import org.openremote.modeler.client.widget.propertyform.PanelPropertyEditForm;
import org.openremote.modeler.client.widget.propertyform.PropertyForm;
import org.openremote.modeler.domain.Panel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

public class PanelPropertyEditable implements PropertyEditable {

   private static final long serialVersionUID = -1171027894552188432L;

   private Panel panel = null;
   private TreePanel<BeanModel> profileTree = null;

   public PanelPropertyEditable() {
   }

   public PanelPropertyEditable(Panel panel, TreePanel<BeanModel> profileTree) {
      this.panel = panel;
      this.profileTree = profileTree;
   }

   public void setName(String name) {
      if (panel != null && name != null && name.trim().length() > 0) {
         panel.setName(name);
         updatePanel();
      }
   }

   public String getName() {
      if (panel != null) {
         return panel.getName();
      } else {
         return "";
      }
   }

   @Override
   public PropertyForm getPropertiesForm() {
      return new PanelPropertyEditForm(this);
   }

   private void updatePanel() {
      this.profileTree.getStore().update(panel.getBeanModel());
      BeanModelDataBase.panelTable.update(panel.getBeanModel());
   }

   @Override
   public String getTitle() {
      return "Panel Property";
   }

}
