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


import org.openremote.android.console.Constants;
import org.openremote.android.console.R;
import org.openremote.android.console.bindings.Slider;
import org.openremote.android.console.model.ListenerConstant;
import org.openremote.android.console.model.OREvent;
import org.openremote.android.console.model.OREventListener;
import org.openremote.android.console.model.ORListenerManager;
import org.openremote.android.console.model.PollingStatusParser;
import org.openremote.android.console.util.ImageUtil;
import org.openremote.android.console.util.NumberFormat;
import org.openremote.android.console.view.seekbar.ORSeekBar;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;

/**
 * This class is responsible for rendering the slider in screen with the slider data.
 * 
 * @author handy 2010-05-12
 *
 */
public class SliderView extends SensoryControlView implements ORSeekBar.OnSeekBarChangeListener {
   private static final int SEEK_BAR_PROGRESS_INIT_VALUE = 0;
   private static final int SEEK_BAR_PROGRESS_MAX = 100;
   
   private static final int SEEK_BAR_MIN_WIDTH = 29;
   private static final int SEEK_BAR_MIN_HEIGHT = 29;
   
   private static final int DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT = 26;
   private static final int DEFAULT_VERTICAL_SEEK_BAR_WIDTH = 26;
   
   private static final int SEEK_BAR_MAX_SIZE = 9;
   
   private static final int SEEK_BAR_MIN_IMAGE_WIDTH = 20;
   private static final int SEEK_BAR_MIN_IMAGE_HEIGHT = 20;
   private static final int SEEK_BAR_MAX_IMAGE_WIDTH = 20;
   private static final int SEEK_BAR_MAX_IMAGE_HEIGHT = 20;
   
   private Context context;
   private Slider slider;
   private ORSeekBar horizontalSeekBar;
   private ORSeekBar verticalSeekBar;
   private int slideToBusinessValue = 0;

   protected SliderView(Context context, Slider slider) {
      super(context);
      if (slider != null) {
         setComponent(slider);
         this.context = context;
         this.slider = slider;
         initSeekBar();
         
         // If the sensor is null, the sliderview is one-way.
         if (slider.getSensor() != null) {
            addPollingSensoryListener();
         }
      }
   }

   private void initSeekBar() {
      LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      // isVertical
      if (slider.isVertical()) {
         initVerticalSeekBar(inflater);
      } else {
         initHorizontalSeekBar(inflater);
      }
   }

   private void initVerticalSeekBar(LayoutInflater inflater) {
      // Get the rootView(TableLayout) of seekbar. 
      // Because of minValueImage and maxValueImage, the seekbar is layouted in TableLayout.
      ViewGroup seekBarRootView = (ViewGroup) inflater.inflate(R.layout.vertical_seekbar,
            (ViewGroup) findViewById(R.id.vertical_seekbar_root_layout));
      
      // Get the seekbar instance from rootView(TableLayout).
      verticalSeekBar = (ORSeekBar) seekBarRootView.findViewById(R.id.vertical_seekbar);
      
      //Set custom track image, include minTrack and maxTrack.
      Drawable maxTrackDrawable = null;
      Drawable minTrackDrawable = null;
      boolean clipImage = false;
      if (slider.getMaxTrackImage() != null) {
         maxTrackDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMaxTrackImage().getSrc());
      }
      if (slider.getMinTrackImage() != null) {
         minTrackDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMinTrackImage().getSrc());
      }
      if (maxTrackDrawable != null || minTrackDrawable != null) {
         if (maxTrackDrawable == null) {
            maxTrackDrawable = context.getResources().getDrawable(R.drawable.vertical_seekbar_background);
         }
         if (minTrackDrawable == null) {
            minTrackDrawable = context.getResources().getDrawable(R.drawable.vertical_seekbar_progress);
         }
         int maxTrackWidth = maxTrackDrawable.getIntrinsicWidth();
         int maxTrackHeight = maxTrackDrawable.getIntrinsicHeight();
         if (maxTrackWidth > DEFAULT_VERTICAL_SEEK_BAR_WIDTH && maxTrackHeight > slider.getFrameHeight()) {
            BitmapDrawable bd = (BitmapDrawable)maxTrackDrawable;
            bd.setBounds(0, 0, DEFAULT_VERTICAL_SEEK_BAR_WIDTH, slider.getFrameHeight());
            bd.setGravity(Gravity.TOP);
            clipImage = true;
         }
         
         int minTrackWidth = minTrackDrawable.getIntrinsicWidth();
         int minTrackHeight = minTrackDrawable.getIntrinsicHeight();
         if (minTrackWidth > DEFAULT_VERTICAL_SEEK_BAR_WIDTH && minTrackHeight > slider.getFrameHeight()) {
            BitmapDrawable bd = (BitmapDrawable)minTrackDrawable;
            bd.setBounds(0, 0, DEFAULT_VERTICAL_SEEK_BAR_WIDTH, slider.getFrameHeight());
            bd.setGravity(Gravity.BOTTOM);
            clipImage = true;
         }
         
         Drawable[] lda = {
               maxTrackDrawable,
               new ClipDrawable(minTrackDrawable, 
                     Gravity.BOTTOM, 
                     ClipDrawable.VERTICAL)
         };
         LayerDrawable ld = new LayerDrawable(lda);
         ld.setId(0, android.R.id.background);
         ld.setId(1, android.R.id.progress);
         if (ld.getIntrinsicWidth() < DEFAULT_VERTICAL_SEEK_BAR_WIDTH) {
            verticalSeekBar.setMaxWidth(ld.getIntrinsicWidth());
         }
         verticalSeekBar.setProgressDrawable(ld);
      }
      if (!clipImage) {
         verticalSeekBar.setMaxWidth(SEEK_BAR_MAX_SIZE);
      }
      
      verticalSeekBar.setMax(SEEK_BAR_PROGRESS_MAX);
      verticalSeekBar.setProgress(getProgressOfBusinessValue(SEEK_BAR_PROGRESS_INIT_VALUE));
      if (slider.isPassive()) {
         verticalSeekBar.setEnabled(false);
      }

      // Set the default layoutparams for seekbar. Set the width and height for seekbar.
      // slider.getFrameHeight() means the height of vertical seekbar no minValueImage and maxValueImage
      verticalSeekBar.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_WIDTH, slider.getFrameHeight()));
      if (slider.getThumbImage() != null) {
         Drawable thumbDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getThumbImage().getSrc());
         verticalSeekBar.setThumb(thumbDrawable);
      }
      
      if (slider.getMinImage() != null) {
         Drawable minValueDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMinImage().getSrc());
         ImageView minValueImageView = (ImageView) seekBarRootView.findViewById(R.id.vertical_seekbar_minvalue_image);
         minValueImageView.setImageDrawable(minValueDrawable);
         
         // Limit the width and height of minValueImage
         minValueImageView.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_IMAGE_WIDTH,
               SEEK_BAR_MIN_IMAGE_HEIGHT));
         
         // Set the height of vertical seekbar no maxImageHeight
         verticalSeekBar.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_WIDTH, slider.getFrameHeight()
               - SEEK_BAR_MIN_IMAGE_HEIGHT));
         
      }

      if (slider.getMaxImage() != null) {
         Drawable maxValueDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMaxImage().getSrc());
         
         ImageView maxValueImageView = (ImageView) seekBarRootView.findViewById(R.id.vertical_seekbar_maxvalue_image);
         maxValueImageView.setImageDrawable(maxValueDrawable);
         
         // Limit the width and height of maxValueImage
         maxValueImageView.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MAX_IMAGE_WIDTH,
               SEEK_BAR_MAX_IMAGE_HEIGHT));
         
         if (slider.getMinImage() != null) {
            // Set the height of vertical seekbar exists minValueImage and maxValueImage
            verticalSeekBar.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_WIDTH, slider.getFrameHeight()
                  - SEEK_BAR_MIN_IMAGE_HEIGHT - SEEK_BAR_MAX_IMAGE_HEIGHT));
         } else {
            // Set the height of vertical seekbar no minImageHeight
            verticalSeekBar.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_WIDTH, slider.getFrameHeight()
                  - SEEK_BAR_MAX_IMAGE_HEIGHT));
         }
      }

      verticalSeekBar.setOnSeekBarChangeListener(this);
      this.addView(seekBarRootView);
   }

   private void initHorizontalSeekBar(LayoutInflater inflater) {
      ViewGroup seekBarRootView = (ViewGroup) inflater.inflate(R.layout.horizontal_seekbar,
            (ViewGroup) findViewById(R.id.horizontal_seekbar_root_layout));
      
      horizontalSeekBar = (ORSeekBar) seekBarRootView.findViewById(R.id.horizontal_seekbar);
      boolean clipImage = false;
      //Set custom track image, include minTrack and maxTrack.
      Drawable maxTrackDrawable = null;
      Drawable minTrackDrawable = null;
      if (slider.getMaxTrackImage() != null) {
         maxTrackDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMaxTrackImage().getSrc());
      }
      if (slider.getMinTrackImage() != null) {
         minTrackDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMinTrackImage().getSrc());
      }
      if (maxTrackDrawable != null || minTrackDrawable != null) {
         if (maxTrackDrawable == null) {
            maxTrackDrawable = context.getResources().getDrawable(R.drawable.horizontal_seekbar_background);
         }
         if (minTrackDrawable == null) {
            minTrackDrawable = context.getResources().getDrawable(R.drawable.horizontal_seekbar_progress);
         }
         int maxTrackWidth = maxTrackDrawable.getIntrinsicWidth();
         int maxTrackHeight = maxTrackDrawable.getIntrinsicHeight();
         if (maxTrackHeight > DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT && maxTrackWidth > slider.getFrameWidth()) {
            BitmapDrawable bd = (BitmapDrawable)maxTrackDrawable;
            bd.setBounds(0, 0, slider.getFrameWidth(), DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT);
            bd.setGravity(Gravity.RIGHT);
            clipImage = true;
         }
         
         int minTrackWidth = minTrackDrawable.getIntrinsicWidth();
         int minTrackHeight = minTrackDrawable.getIntrinsicHeight();
         if (minTrackHeight > DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT && minTrackWidth > slider.getFrameWidth()) {
            BitmapDrawable bd = (BitmapDrawable)minTrackDrawable;
            bd.setBounds(0, 0, slider.getFrameWidth(), DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT);
            bd.setGravity(Gravity.LEFT);
            clipImage = true;
         }
         
         Drawable[] lda = {
               maxTrackDrawable,
               new ClipDrawable(minTrackDrawable, 
                     Gravity.LEFT, 
                     ClipDrawable.HORIZONTAL)
         };
         LayerDrawable ld = new LayerDrawable(lda);
         ld.setId(0, android.R.id.background);
         ld.setId(1, android.R.id.progress);
         if (ld.getIntrinsicHeight() < DEFAULT_HORIZONTAL_SEEK_BAR_HEIGHT) {
            horizontalSeekBar.setMaxHeight(ld.getIntrinsicHeight());
         }
         horizontalSeekBar.setProgressDrawable(ld);
      }
      if (!clipImage) {
         horizontalSeekBar.setMaxHeight(SEEK_BAR_MAX_SIZE);
      }
      horizontalSeekBar.setMax(SEEK_BAR_PROGRESS_MAX);
      horizontalSeekBar.setProgress(getProgressOfBusinessValue(SEEK_BAR_PROGRESS_INIT_VALUE));
      if (slider.isPassive()) {
         horizontalSeekBar.setEnabled(false);
      }

      horizontalSeekBar.setLayoutParams(new TableRow.LayoutParams(slider.getFrameWidth(), SEEK_BAR_MIN_HEIGHT));
      if (slider.getThumbImage() != null) {
         Drawable thumbDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getThumbImage().getSrc());
         horizontalSeekBar.setThumb(thumbDrawable);
      }
      
      if (slider.getMinImage() != null) {
         Drawable minValueDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMinImage().getSrc());
         
         ImageView minValueImageView = (ImageView) seekBarRootView.findViewById(R.id.horizontal_seekbar_minvalue_image);
         minValueImageView.setImageDrawable(minValueDrawable);
         minValueImageView.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MIN_IMAGE_WIDTH,
               SEEK_BAR_MIN_IMAGE_HEIGHT));
         
         horizontalSeekBar.setLayoutParams(new TableRow.LayoutParams(slider.getFrameWidth() - SEEK_BAR_MIN_IMAGE_WIDTH,
               SEEK_BAR_MIN_HEIGHT));
      }

      if (slider.getMaxImage() != null) {
         Drawable maxValueDrawable = ImageUtil.createFromPathQuietly(Constants.FILE_FOLDER_PATH
               + slider.getMaxImage().getSrc());
         
         ImageView maxValueImageView = (ImageView) seekBarRootView.findViewById(R.id.horizontal_seekbar_maxvalue_image);
         maxValueImageView.setImageDrawable(maxValueDrawable);
         maxValueImageView.setLayoutParams(new TableRow.LayoutParams(SEEK_BAR_MAX_IMAGE_WIDTH,
               SEEK_BAR_MAX_IMAGE_HEIGHT));
         
         if (slider.getMinImage() != null) {
            horizontalSeekBar.setLayoutParams(new TableRow.LayoutParams(slider.getFrameWidth()
                  - SEEK_BAR_MIN_IMAGE_WIDTH - SEEK_BAR_MAX_IMAGE_WIDTH, SEEK_BAR_MIN_HEIGHT));
         } else {
            horizontalSeekBar.setLayoutParams(new TableRow.LayoutParams(slider.getFrameWidth()
                  - SEEK_BAR_MAX_IMAGE_WIDTH, SEEK_BAR_MIN_HEIGHT));
         }
      }

      horizontalSeekBar.setOnSeekBarChangeListener(this);
      this.addView(seekBarRootView);
   }
   
   private int getProgressOfBusinessValue(int businessValue) {
      if (slider.getMaxValue() == 0 || slider.getMaxValue() == slider.getMinValue()) {
         return 0;
      }
      double progress = SEEK_BAR_PROGRESS_MAX * ((float)(businessValue - slider.getMinValue()) / (slider.getMaxValue() - slider.getMinValue()));
      return (int) NumberFormat.format(0, progress);
   }
   
   @Override
   public void addPollingSensoryListener() {
      final Integer sensorId = ((Slider)getComponent()).getSensor().getSensorId();
      Log.i("OpenRemote-SLIDER", "sensor id is " + sensorId);
      if (sensorId > 0) {
         ORListenerManager.getInstance().addOREventListener(ListenerConstant.ListenerPollingStatusIdFormat + sensorId, new OREventListener() {
            public void handleEvent(OREvent event) {
               String value = PollingStatusParser.statusMap.get(sensorId.toString()).toLowerCase();
               int valueInt = 0;
               try {
                  valueInt = Integer.parseInt(value);
               } catch (NumberFormatException e) {
                  Log.e("OpenRemote-SLIDER", "The returned format of polling value " + value + " for slider is wrong", e);
                  return;
               }
               pollingValueUIHandler.sendEmptyMessage(valueInt);
            }
         });
      }
   }
   
   private Handler pollingValueUIHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {
         int businessValue = msg.what;
         int progress = getProgressOfBusinessValue(businessValue);
         if(slider.isVertical()) {
            verticalSeekBar.setProgress(progress);
         } else {
            horizontalSeekBar.setProgress(progress);
         }
         super.handleMessage(msg);
      }
  };
   
   @Override
   public void onProgressChanged(ORSeekBar seekBar, int progress, boolean fromUser) {
      slideToBusinessValue = (int)(((float)progress/SEEK_BAR_PROGRESS_MAX) * (slider.getMaxValue() - slider.getMinValue()) + slider.getMinValue());
   }

   @Override
   public void onStartTrackingTouch(ORSeekBar seekBar) {
   }

   @Override
   public void onStopTrackingTouch(ORSeekBar seekBar) {
      Log.i("OpenRemote-SLIDER", "Slide to business value " + slideToBusinessValue);
      sendCommandRequest(String.valueOf(slideToBusinessValue));
   }
}
