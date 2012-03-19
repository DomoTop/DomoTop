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

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.View;

/**
 * Image Utility class.
 * 
 * @author Dan Cong
 */
public class ImageUtil {

   private ImageUtil() {
   }
   
   /**
    * Calls native Drawable.createFromPath(pathName), but catch OutOfMemoryError and do nothing.
    * 
    * @param pathName
    *           path name
    * @return Drawable instance
    */
   public static Drawable createFromPathQuietly(String pathName) {
      Drawable d = null;
      try {
         d = Drawable.createFromPath(pathName);
      } catch (OutOfMemoryError e) {
         Log.e("OpenRemote-OutOfMemoryError", pathName + ": bitmap size exceeds VM budget");
      }
      return d;
   }
   
   public static BitmapDrawable createClipedDrawableFromPath(String pathName, int width, int height) {
      Drawable d = createFromPathQuietly(pathName);
      if (d != null) {
         BitmapDrawable bd = (BitmapDrawable)d;
         bd.setBounds(0, 0, width, height);
         bd.setGravity(Gravity.LEFT|Gravity.TOP);
         return bd;
      }
      return null;
   }
   /**
    * Calls native Activity.setContentView(int layoutResID), but catch OutOfMemoryError and do nothing.
    * 
    * @param activity
    *           an activity
    * @param layoutResID
    *           Resource ID to be inflated.
    */
   public static void setContentViewQuietly(Activity activity, int layoutResID) {
      if (activity == null) {
         return;
      }
      try {
         activity.setContentView(layoutResID);
      } catch (InflateException e) {
         Log.e("OpenRemote-OutOfMemoryError", "unable to setContentView, bitmap size exceeds VM budget");
      } catch (OutOfMemoryError e) {
         Log.e("OpenRemote-OutOfMemoryError", "unable to setContentView, bitmap size exceeds VM budget");
      }
   }
   
}
