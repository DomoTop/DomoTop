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
package org.openremote.android.console.view;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.openremote.android.console.bindings.Background;
import org.openremote.android.console.bindings.LayoutContainer;
import org.openremote.android.console.bindings.Screen;
import org.openremote.android.console.model.PollingHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

public class ScreenView extends AbsoluteLayout {

   private Screen screen;
   private PollingHelper polling;
   /**
    * Instantiates a new screen view.
    * 
    * @param context
    *           the context
    * @param screen
    *           the screen
    */
   @SuppressWarnings("deprecation")
   public ScreenView(Context context, Screen screen) {
      super(context);
      this.screen = screen;
      setBackgroundColor(0);
      setTag(screen.getName());
      setId(screen.getScreenId());
      if (screen.getBackground() != null) {
         addBackground();

      }

      ArrayList<LayoutContainer> layouts = screen.getLayouts();
      for (int i = 0; i < layouts.size(); i++) {
         LayoutContainerView la = LayoutContainerView.buildWithLayoutContainer(context, layouts.get(i));
         if (la != null) {
            LayoutContainer layout = layouts.get(i);
            addView(la, new AbsoluteLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, layout.getLeft(), layout
                  .getTop()));
         }
      }
      
   }

   /**
    * @param screen
    */
   private void addBackground() {
      ImageView backgroudView = new ImageView(this.getContext());
      int left = 0;
      int top = 0;
      int screenWidth = Screen.SCREEN_WIDTH;
      int screenHeight = Screen.SCREEN_HEIGHT - Screen.SCREEN_STATUS_BAR_HEIGHT;
      try {
         Bitmap backgroudBitMap = BitmapFactory.decodeStream(this.getContext().openFileInput(screen.getBackgroundSrc()));
         backgroudView.setImageBitmap(backgroudBitMap);
         if (backgroudBitMap == null) {
            return;
         }
         int imageWidth = backgroudBitMap.getWidth();
         int imageHeight = backgroudBitMap.getHeight();
         Background background = screen.getBackground();
         if (!background.isFillScreen()) {
            if (background.isBackgroundImageAbsolutePosition()) {
               left = background.getBackgroundImageAbsolutePositionLeft();
               top = background.getBackgroundImageAbsolutePositionTop();
            } else {
               String backgroundImageRelativePosition = background.getBackgroundImageRelativePosition();
               if ("top_left".equals(backgroundImageRelativePosition)) {
               } else if ("top".equals(backgroundImageRelativePosition)) {
                  left = (screenWidth - imageWidth) / 2;
               } else if ("top_right".equals(backgroundImageRelativePosition)) {
                  left = screenWidth - imageWidth;
               } else if ("left".equals(backgroundImageRelativePosition)) {
                  top = (screenHeight - imageHeight) / 2;
               } else if ("center".equals(backgroundImageRelativePosition)) {
                  left = (screenWidth - imageWidth) / 2;
                  top = (screenHeight - imageHeight) / 2;
               } else if ("right".equals(backgroundImageRelativePosition)) {
                  left = screenWidth - imageWidth;
                  top = (screenHeight - imageHeight) / 2;
               } else if ("bottom".equals(backgroundImageRelativePosition)) {
                  left = (screenWidth - imageWidth) / 2;
                  top = screenHeight - imageHeight;
               } else if ("bottom_left".equals(backgroundImageRelativePosition)) {
                  top = screenHeight - imageHeight;
               } else if ("bottom_right".equals(backgroundImageRelativePosition)) {
                  left = screenWidth - imageWidth;
                  top = screenHeight - imageHeight;
               }
            }
         }
         addView(backgroudView, new AbsoluteLayout.LayoutParams(imageWidth, imageHeight, left, top));
      } catch (FileNotFoundException e) {
         Log.e("OpenRemote-ScreenView", "screen background file" + screen.getBackgroundSrc() + " not found.", e);
      } catch (OutOfMemoryError e) {
         Log.e("OpenRemote-OutOfMemoryError", screen.getBackgroundSrc() + ": bitmap size exceeds VM budget");
      }
   }

   /**
    * Start polling on the screen's sensor components.
    */
   public void startPolling() {
      if (!screen.getPollingComponentsIds().isEmpty()) {
         polling = new PollingHelper(screen.getPollingComponentsIds(), getContext());
      }
      if (polling != null) {
         new Thread(new Runnable() {
            public void run() {
               polling.requestCurrentStatusAndStartPolling();
            }
         }).start(); 
      }
   }
   
   public void cancelPolling() {
      if (polling != null) {
         polling.cancelPolling();
      }
   }
   
   public Screen getScreen() {
      return screen;
   }
   
}
