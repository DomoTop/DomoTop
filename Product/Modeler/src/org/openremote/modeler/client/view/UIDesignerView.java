/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2009, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.modeler.client.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.event.PropertyEditEvent;
import org.openremote.modeler.client.model.AutoSaveResponse;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.proxy.UtilsProxy;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.utils.IDUtil;
import org.openremote.modeler.client.utils.PanelsAndMaxOid;
import org.openremote.modeler.client.utils.TouchPanels;
import org.openremote.modeler.client.widget.uidesigner.ProfilePanel;
import org.openremote.modeler.client.widget.uidesigner.PropertyPanel;
import org.openremote.modeler.client.widget.uidesigner.ScreenPanel;
import org.openremote.modeler.client.widget.uidesigner.TemplatePanel;
import org.openremote.modeler.client.widget.uidesigner.WidgetPanel;
import org.openremote.modeler.domain.Group;
import org.openremote.modeler.domain.GroupRef;
import org.openremote.modeler.domain.Panel;
import org.openremote.modeler.domain.ScreenPair;
import org.openremote.modeler.domain.ScreenPairRef;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
 * The Class UIDesignerView.
 */
public class UIDesignerView extends TabItem {

   /** The screen tab. */
   private ScreenPanel screenPanel = new ScreenPanel();

   /** The auto_save_interval millisecond. */
   private static final int AUTO_SAVE_INTERVAL_MS = 30000;

   private Timer timer;

   private ProfilePanel profilePanel = null;
   
   private TemplatePanel templatePanel = null;
   
   private PropertyPanel propertyPanel = null;
   
   /**
    * Instantiates a new uI designer view.
    */
   public UIDesignerView() {
      super();
      setText("UI Designer");

      AsyncServiceFactory.getUtilsRPCServiceAsync().getAccountPath(new AsyncSuccessCallback <String>() {
         public void onFailure(Throwable caught) {
            Info.display("Error", "falid to get account path.");
            super.checkTimeout(caught);
         }
         public void onSuccess(String result) {
            Cookies.setCookie(Constants.CURRETN_RESOURCE_PATH, result);
         }
         
      });
      setLayout(new BorderLayout());
      profilePanel = createWest();
      createCenter();
      createEast();
      prepareData();
      createAutoSaveTimer();
   }

   /**
    * Creates the timer.
    */
   private void createAutoSaveTimer() {
      timer = new Timer() {
         @Override
         public void run() {
            autoSaveUiDesignerLayout();
         }
      };
      timer.scheduleRepeating(AUTO_SAVE_INTERVAL_MS);
   }

   /**
    * Auto save ui designer layout json.
    */
   public void autoSaveUiDesignerLayout() {
      if (profilePanel.isInitialized()) {
         UtilsProxy.autoSaveUiDesignerLayout(getAllPanels(), IDUtil.currentID(),
               new AsyncSuccessCallback<AutoSaveResponse>() {
                  @Override
                  public void onSuccess(AutoSaveResponse result) {
                     if (result != null && result.isUpdated()) {
                        Info.display("Info", "UI designer layout saved at "
                              + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
                     }
                     Window.setStatus("Auto-Saving: UI designer layout saved at: "
                           + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
                  }

                  @Override
                  public void onFailure(Throwable caught) {
                     timer.cancel();
                     boolean timeout = super.checkTimeout(caught);
                     if (!timeout) {
                        Info.display(new InfoConfig("Error", caught.getMessage() + " "
                              + DateTimeFormat.getFormat("HH:mm:ss").format(new Date())));
                     }
                     Window.setStatus("Failed to save UI designer layout at: "
                           + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
                  }
               });
         Window.setStatus("Saving ....");
      } else {
         Window.setStatus("Auto-Saving: Unable to save UI designer because panel list has not been initialized. ");
      }
   }

   public void saveUiDesignerLayout() {
      if (templatePanel != null && templatePanel.isExpanded()) {
         templatePanel.saveTemplateUpdates();
      } else {
         if (profilePanel.isInitialized()) {
            UtilsProxy.saveUiDesignerLayout(getAllPanels(), IDUtil.currentID(),
                  new AsyncSuccessCallback<AutoSaveResponse>() {
                     @Override
                     public void onSuccess(AutoSaveResponse result) {
                        if (result != null && result.isUpdated()) {
                           Info.display("Info", "UI designer layout saved at "
                                 + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
                        }
                        Window.setStatus("UI designer layout saved at: "
                              + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
                     }
   
                     @Override
                     public void onFailure(Throwable caught) {
                        timer.cancel();
                        boolean timeout = super.checkTimeout(caught);
                        if (!timeout) {
                           Info.display(new InfoConfig("Error", caught.getMessage() + " "
                                 + DateTimeFormat.getFormat("HH:mm:ss").format(new Date())));
                        }
                        Window.setStatus("Failed to save UI designer layout at: "
                              + DateTimeFormat.getFormat("HH:mm:ss").format(new Date()));
   
                     }
   
                  });
            Window.setStatus("Saving ....");
         } else {
            Window.setStatus("Unable to save UI designer because panel list has not been initialized. ");
         }
      }
   }

   public void restore() {
      UtilsProxy.restore(new AsyncSuccessCallback<PanelsAndMaxOid>() {
         @Override
         public void onSuccess(PanelsAndMaxOid panelsAndMaxOid) {
            BeanModelDataBase.panelTable.clear();
            BeanModelDataBase.groupTable.clear();
            BeanModelDataBase.screenTable.clear();
            Collection<Panel> panels = panelsAndMaxOid.getPanels();
            long maxOid = panelsAndMaxOid.getMaxOid();

            for (Panel panel : panels) {
               BeanModelDataBase.panelTable.insert(panel.getBeanModel());
               for (GroupRef groupRef : panel.getGroupRefs()) {
                  Group group = groupRef.getGroup();
                  BeanModelDataBase.groupTable.insert(group.getBeanModel());
                  for (ScreenPairRef screenRef : group.getScreenRefs()) {
                     ScreenPair screen = screenRef.getScreen();
                     BeanModelDataBase.screenTable.insert(screen.getBeanModel());
                  }
               }
            }
            IDUtil.setCurrentID(maxOid);
            refreshPanelTree();
         }

         @Override
         public void onFailure(Throwable caught) {
            MessageBox.alert("Error", "UI designer restore failed: " + caught.getMessage(), null);
            super.checkTimeout(caught);
         }
      });
   }

   /**
    * Creates the east.
    */
   private void createEast() {
      BorderLayoutData eastLayout = new BorderLayoutData(LayoutRegion.EAST, 300);
      eastLayout.setSplit(true);
      eastLayout.setMargins(new Margins(0, 2, 0, 2));
      add(createWidgetAndPropertyContainer(), eastLayout);
   }

   /**
    * Creates the west.
    */
   private ProfilePanel createWest() {
      ContentPanel west = new ContentPanel();
      ProfilePanel result = new ProfilePanel(screenPanel);
      result.addListener(PropertyEditEvent.PropertyEditEvent, new Listener<PropertyEditEvent>() {
         public void handleEvent(PropertyEditEvent be) {
            propertyPanel.setPropertyForm(be.getPropertyEditable());
         }
         
      });
      templatePanel = new TemplatePanel(screenPanel);
      BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 200);
      westData.setSplit(true);
      west.setLayout(new AccordionLayout());
      west.setBodyBorder(false);
      west.setHeaderVisible(false);
      west.add(result);
      west.add(templatePanel);
      westData.setMargins(new Margins(0, 2, 0, 0));
      add(west, westData);
      return result;
   }

   private void refreshPanelTree() {
      if (profilePanel != null) {
         profilePanel.layout();
      }
   }

   /**
    * Creates the center.
    */
   private void createCenter() {
      BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
      centerData.setMargins(new Margins(0, 2, 0, 2));
      add(screenPanel, centerData);
   }

   /**
    * Prepare data.
    */
   private void prepareData() {
      TouchPanels.load();
   }

   /**
    * create widget and property container in the view's east.
    */
   private ContentPanel createWidgetAndPropertyContainer() {
      ContentPanel widgetAndPropertyContainer = new ContentPanel(new BorderLayout());
      widgetAndPropertyContainer.setHeaderVisible(false);
      widgetAndPropertyContainer.setBorders(false);
      widgetAndPropertyContainer.setBodyBorder(false);

      WidgetPanel widgetPanel = new WidgetPanel();
      BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH);
      northData.setSplit(true);
      northData.setMargins(new Margins(2));
      widgetPanel.setSize("100%", "50%");
      widgetAndPropertyContainer.add(widgetPanel, northData);

      propertyPanel = new PropertyPanel();
      BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
      centerData.setSplit(true);
      centerData.setMargins(new Margins(2));
      propertyPanel.setSize("100%", "50%");

      widgetAndPropertyContainer.add(propertyPanel, centerData);
      return widgetAndPropertyContainer;
   }

   List<Panel> getAllPanels() {
      List<Panel> panelList = new ArrayList<Panel>();
      for (BeanModel panelBeanModel : BeanModelDataBase.panelTable.loadAll()) {
         panelList.add((Panel) panelBeanModel.getBean());
      }
      return panelList;
   }
}
