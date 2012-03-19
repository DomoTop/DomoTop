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
package org.openremote.android.console.view.seekbar;

import android.content.Context;
import android.util.AttributeSet;



/**
 * This class is rewrite the SeekBar of Android, and for supporting vertical seek bar.
 * <p> 
 * See {@link android.widget.SeekBar}
 * </p>
 * 
 * @author handy.wang, tomsky.wang
 */
public class ORSeekBar extends ORAbsSeekBar {

   /**
    * A callback that notifies clients when the progress level has been changed. This includes changes that were
    * initiated by the user through a touch gesture or arrow key/trackball as well as changes that were initiated
    * programmatically.
    */
   public interface OnSeekBarChangeListener {

      /**
       * Notification that the progress level has changed. Clients can use the fromUser parameter to distinguish
       * user-initiated changes from those that occurred programmatically.
       * 
       * @param seekBar
       *           The SeekBar whose progress has changed
       * @param progress
       *           The current progress level. This will be in the range 0..max where max was set by
       *           {@link ProgressBar#setMax(int)}. (The default value for max is 100.)
       * @param fromUser
       *           True if the progress change was initiated by the user.
       */
      void onProgressChanged(ORSeekBar seekBar, int progress, boolean fromUser);

      /**
       * Notification that the user has started a touch gesture. Clients may want to use this to disable advancing the
       * seekbar.
       * 
       * @param seekBar
       *           The SeekBar in which the touch gesture began
       */
      void onStartTrackingTouch(ORSeekBar seekBar);

      /**
       * Notification that the user has finished a touch gesture. Clients may want to use this to re-enable advancing
       * the seekbar.
       * 
       * @param seekBar
       *           The SeekBar in which the touch gesture began
       */
      void onStopTrackingTouch(ORSeekBar seekBar);
   }

   private OnSeekBarChangeListener mOnSeekBarChangeListener;

   public ORSeekBar(Context context) {
      this(context, null);
   }

   public ORSeekBar(Context context, AttributeSet attrs) {
      this(context, attrs, android.R.attr.seekBarStyle);
   }

   public ORSeekBar(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
   }

   @Override
   void onProgressRefresh(float scale, boolean fromUser) {
      super.onProgressRefresh(scale, fromUser);

      if (mOnSeekBarChangeListener != null) {
         mOnSeekBarChangeListener.onProgressChanged(this, getProgress(), fromUser);
      }
   }

   /**
    * Sets a listener to receive notifications of changes to the SeekBar's progress level. Also provides notifications
    * of when the user starts and stops a touch gesture within the SeekBar.
    * 
    * @param l
    *           The seek bar notification listener
    * 
    * @see SeekBar.OnSeekBarChangeListener
    */
   public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
      mOnSeekBarChangeListener = l;
   }

   @Override
   void onStartTrackingTouch() {
      if (mOnSeekBarChangeListener != null) {
         mOnSeekBarChangeListener.onStartTrackingTouch(this);
      }
   }

   @Override
   void onStopTrackingTouch() {
      if (mOnSeekBarChangeListener != null) {
         mOnSeekBarChangeListener.onStopTrackingTouch(this);
      }
   }

   /**
    * Sets the max height to make the narrow track image display correctly in horizontal seek bar.
    * 
    */
   public void setMaxHeight(int maxHeight) {
      if (!vertical) {
         mMaxHeight = maxHeight;
      }
   }

   /**
    * Sets the max width to make the narrow track image display correctly in vertical seek bar.
    * 
    * @param maxWidth
    *           the new max width
    */
   public void setMaxWidth(int maxWidth) {
      if (vertical) {
         mMaxWidth = maxWidth;
      }
   }
}
