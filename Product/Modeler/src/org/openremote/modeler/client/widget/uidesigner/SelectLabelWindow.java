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
package org.openremote.modeler.client.widget.uidesigner;

import java.util.ArrayList;
import java.util.Collection;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.domain.Absolute;
import org.openremote.modeler.domain.Cell;
import org.openremote.modeler.domain.Screen;
import org.openremote.modeler.domain.component.UIGrid;
import org.openremote.modeler.domain.component.UILabel;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SelectLabelWindow extends Dialog {


   private TreePanel<BeanModel> labelTree;
   
   private static final Icons ICON = GWT.create(Icons.class);
   private TreeStore<BeanModel> labelTreeStore = null;
   private Collection<ScreenCanvas> canvases = new ArrayList<ScreenCanvas>();
   
   public SelectLabelWindow(ScreenCanvas ... canvases) {
      for (ScreenCanvas canvas :canvases){
         this.canvases.add(canvas);
      }
      setHeading("Select Label");
      setMinHeight(260);
      setWidth(200);
      setLayout(new FitLayout());
      setModal(true);
      initLabelCollection();
      setButtons(Dialog.OKCANCEL);
      setHideOnButtonClick(true);
      addButtonListener();
      show();
   }

   private void initLabelCollection() {
      ContentPanel labelContainer = new ContentPanel();
      labelContainer.setBorders(false);
      labelContainer.setBodyBorder(false);
      labelContainer.setHeaderVisible(false);
      if (labelTree == null) {
         labelTree = buildLabelTree();
         labelContainer.add(labelTree);
      }
      labelTree.getSelectionModel().deselectAll();
      labelContainer.setScrollMode(Scroll.AUTO);
      add(labelContainer);
   }
   
   private void addButtonListener() {
      addListener(Events.BeforeHide, new Listener<WindowEvent>() {
         public void handleEvent(WindowEvent be) {
            if (be.getButtonClicked() == getButtonById("ok")) {
               BeanModel beanModel = labelTree.getSelectionModel().getSelectedItem();
               if (beanModel == null) {
                  MessageBox.alert("Error", "Please select a switch.", null);
                  be.cancelBubble();
               } else {
                  if (beanModel.getBean() instanceof UILabel) {
                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(beanModel));
                  } else {
                     MessageBox.alert("Error", "Please select a switch.", null);
                     be.cancelBubble();
                  }
               }
            }
         }
      }); 
   }
   
   private  TreePanel<BeanModel> buildLabelTree(){
      if (labelTreeStore == null) {
         labelTreeStore = new TreeStore<BeanModel>();
         for (ScreenCanvas canvas:canvases){
            labelTreeStore.add(canvas.getScreen().getBeanModel(), false);
            for (Absolute absolute : canvas.getScreen().getAbsolutes()){
               if (absolute.getUiComponent() instanceof UILabel){
                  labelTreeStore.add(canvas.getScreen().getBeanModel(), absolute.getUiComponent().getBeanModel(),false);
               }
            }
            for (UIGrid grid : canvas.getScreen().getGrids()){
               for(Cell cell : grid.getCells()){
                  if(cell.getUiComponent() instanceof UILabel){
                     labelTreeStore.add(canvas.getScreen().getBeanModel(), cell.getUiComponent().getBeanModel(),false);
                  }
               }
            }
         }
        
      }

      final TreePanel<BeanModel> tree = new TreePanel<BeanModel>(labelTreeStore);
      tree.setStateful(true);
      tree.setBorders(false);
      tree.setHeight("100%");
      tree.setDisplayProperty("displayName");
      
      tree.setIconProvider(new ModelIconProvider<BeanModel>() {
         public AbstractImagePrototype getIcon(BeanModel thisModel) {
            if (thisModel.getBean() instanceof Screen) {
               return ICON.screenIcon();
            } else if (thisModel.getBean() instanceof UILabel ) {
               return ICON.labelIcon();
            } else {
               return null;
            }
         }
      });
      return tree;
   }


}
