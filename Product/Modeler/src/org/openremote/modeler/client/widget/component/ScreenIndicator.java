package org.openremote.modeler.client.widget.component;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;

public class ScreenIndicator extends LayoutContainer {

   public ScreenIndicator(int screenCount, int screenIndex, int width, int height) {
      setStyleAttribute("position", "absolute");
      setSize(width, height);
      setLayout(new CenterLayout());
      if (screenCount > 1) {
         LayoutContainer indicatorContainer = new LayoutContainer();
         indicatorContainer.setSize(14 * screenCount, 14);
         for (int i = 0; i < screenCount; i++) {
            LayoutContainer indicator = new LayoutContainer();
            indicator.setSize(14, 14);
            if (i != screenIndex) {
               indicator.setStyleName("indicator-default");
            } else {
               indicator.setStyleName("indicator-current");
            }
            indicatorContainer.add(indicator);
         }
         add(indicatorContainer);
      }
   }
   
   @Override
   protected void afterRender() {
      super.afterRender();
      super.el().updateZIndex(1);
   }
}
