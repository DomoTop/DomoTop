package org.openremote.modeler.client.widget.propertyform;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.utils.IDUtil;
import org.openremote.modeler.client.widget.uidesigner.ComponentContainer;
import org.openremote.modeler.client.widget.uidesigner.PropertyPanel;
import org.openremote.modeler.client.widget.uidesigner.ScreenTab;
import org.openremote.modeler.client.widget.uidesigner.ScreenTabItem;
import org.openremote.modeler.domain.Screen;
import org.openremote.modeler.domain.ScreenPair;
import org.openremote.modeler.domain.ScreenPair.OrientationType;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

public class ScreenPairPropertyForm extends PropertyForm {

   private ScreenTab screenTab;
   private ScreenPair screenPair;
   public ScreenPairPropertyForm(ComponentContainer componentContainer) {
      super(componentContainer);
      setFieldWidth(130);
      this.screenTab = (ScreenTab) componentContainer.getParent().getParent();
      this.screenPair = screenTab.getScreenPair();
      createFields();
   }

   private void createFields() {
      Radio vRadio = new Radio();
      vRadio.setValueAttribute(OrientationType.PORTRAIT.toString());
      vRadio.setBoxLabel("Portrait");
      Radio hRadio = new Radio();
      hRadio.setValueAttribute(OrientationType.LANDSCAPE.toString());
      hRadio.setBoxLabel("Landscape");
      Radio vhRadio = new Radio();
      vhRadio.setValueAttribute(OrientationType.BOTH.toString());
      vhRadio.setBoxLabel("Portrait & Landscape");

      RadioGroup radioGroup = new RadioGroup("orientation");
      radioGroup.setOrientation(Orientation.VERTICAL);
      radioGroup.setFieldLabel("Orientation");
      radioGroup.add(vRadio);
      radioGroup.add(hRadio);
      radioGroup.add(vhRadio);
      add(radioGroup);
      if (OrientationType.PORTRAIT.equals(screenPair.getOrientation())) {
         vRadio.setValue(true);
      } else if (OrientationType.LANDSCAPE.equals(screenPair.getOrientation())) {
         hRadio.setValue(true);
      } else if (OrientationType.BOTH.equals(screenPair.getOrientation())) {
         vhRadio.setValue(true);
      }
      radioGroup.addListener(Events.Change, new Listener<FieldEvent>() {
         public void handleEvent(FieldEvent be) {
            Radio radio = ((RadioGroup)be.getField()).getValue();
            if (OrientationType.PORTRAIT.toString().equals(radio.getValueAttribute())) {
               screenPair.setOrientation(OrientationType.PORTRAIT);
               if (screenPair.getPortraitScreen() == null) {
                  Screen screen = new Screen();
                  screen.setOid(IDUtil.nextID());
                  screen.setName(screenPair.getName());
                  screenPair.setPortraitScreen(screen);
               }
               screenPair.getPortraitScreen().setTouchPanelDefinition(screenPair.getTouchPanelDefinition());
               if (screenTab.getItemByItemId(Constants.LANDSCAPE) != null) {
                  screenTab.getItemByItemId(Constants.LANDSCAPE).disable();
               }
               if (screenTab.getItemByItemId(Constants.PORTRAIT) == null) {
                  screenTab.insert(new ScreenTabItem(screenPair.getPortraitScreen()), 0);
               } else {
                  screenTab.getItemByItemId(Constants.PORTRAIT).enable();
               }
               screenTab.setSelection(screenTab.getItemByItemId(Constants.PORTRAIT));
            } else if (OrientationType.LANDSCAPE.toString().equals(radio.getValueAttribute())) {
               screenPair.setOrientation(OrientationType.LANDSCAPE);
               if (screenPair.getLandscapeScreen() == null) {
                  Screen screen = new Screen();
                  screen.setLandscape(true);
                  screen.setOid(IDUtil.nextID());
                  screen.setName(screenPair.getName());
                  screenPair.setLandscapeScreen(screen);
               }
               screenPair.getLandscapeScreen().setTouchPanelDefinition(screenPair.getTouchPanelDefinition().getHorizontalDefinition());
               if (screenTab.getItemByItemId(Constants.PORTRAIT) != null) {
                  screenTab.getItemByItemId(Constants.PORTRAIT).disable();
               }
               if (screenTab.getItemByItemId(Constants.LANDSCAPE) == null) {
                  screenTab.add(new ScreenTabItem(screenPair.getLandscapeScreen()));
               } else {
                  screenTab.getItemByItemId(Constants.LANDSCAPE).enable();
               }
               screenTab.setSelection(screenTab.getItemByItemId(Constants.LANDSCAPE));
            } else if (OrientationType.BOTH.toString().equals(radio.getValueAttribute())) {
               screenPair.setOrientation(OrientationType.BOTH);
               if (screenPair.getPortraitScreen() == null) {
                  Screen screen = new Screen();
                  screen.setOid(IDUtil.nextID());
                  screen.setName(screenPair.getName());
                  screenPair.setPortraitScreen(screen);
               }
               if (screenPair.getLandscapeScreen() == null) {
                  Screen screen = new Screen();
                  screen.setLandscape(true);
                  screen.setOid(IDUtil.nextID());
                  screen.setName(screenPair.getName());
                  screenPair.setLandscapeScreen(screen);
               }
               screenPair.getLandscapeScreen().setTouchPanelDefinition(screenPair.getTouchPanelDefinition().getHorizontalDefinition());
               if (screenTab.getItemByItemId(Constants.PORTRAIT) != null) {
                  screenTab.getItemByItemId(Constants.PORTRAIT).enable();
               } else {
                  screenTab.insert(new ScreenTabItem(screenPair.getPortraitScreen()), 0);
               }
               if (screenTab.getItemByItemId(Constants.LANDSCAPE) != null) {
                  screenTab.getItemByItemId(Constants.LANDSCAPE).enable();
               } else {
                  screenTab.add(new ScreenTabItem(screenPair.getLandscapeScreen()));
               }
               screenTab.setSelection(screenTab.getItemByItemId(Constants.PORTRAIT));
            }
            screenPair.clearInverseScreenIds();
         }
         
      });
   }
   
   @Override
   protected void afterRender() {
      super.afterRender();
      ((PropertyPanel)this.getParent()).setHeading("Screen pair properties");
   }
}
