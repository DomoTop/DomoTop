package org.openremote.modeler.domain.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openremote.modeler.domain.RangeSensor;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.Slider;
import org.openremote.modeler.domain.UICommand;

import flexjson.JSON;

public class UISlider extends UIControl implements SensorOwner, ImageSourceOwner {
   
   public static final String DEFAULT_HORIZONTAL_MIN_IMAGE = "./resources/images/custom/slider/min.png";
   public static final String DEFAULT_HORIZONTAL_MINTRACK_IMAGE = "./resources/images/custom/slider/minTrack.png";
   public static final String DEFAULT_HORIZONTAL_THUMB_IMAGE = "./resources/images/custom/slider/thumb.png";
   public static final String DEFAULT_HORIZONTAL_MAXTRACK_IMAGE = "./resources/images/custom/slider/maxTrack.png";
   public static final String DEFAULT_HORIZONTAL_MAX_IMAGE = "./resources/images/custom/slider/max.png";
   
   public static final String DEFAULT_VERTICAL_MIN_IMAGE = "./resources/images/custom/slider/vmin.png";
   public static final String DEFAULT_VERTICAL_MINTRACK_IMAGE = "./resources/images/custom/slider/vminTrack.png";
   public static final String DEFAULT_VERTICAL_THUMB_IMAGE = "./resources/images/custom/slider/vthumb.png";
   public static final String DEFAULT_VERTICAL_MAXTRACK_IMAGE = "./resources/images/custom/slider/vmaxTrack.png";
   public static final String DEFAULT_VERTICAL_MAX_IMAGE = "./resources/images/custom/slider/vmax.png";
   
   private static final long serialVersionUID = 4821886776184406692L;
   
   private boolean vertical = false;
   private ImageSource thumbImage = new ImageSource("");;
   private ImageSource minImage = new ImageSource("");
   private ImageSource minTrackImage = new ImageSource("");
   private ImageSource maxImage = new ImageSource("");
   private ImageSource maxTrackImage = new ImageSource("");
   private Slider slider;
   
   public UISlider() {
   }
   
   public UISlider(long id) {
      super(id);
   }
   
   public UISlider(UISlider uiSlider) {
      this.setOid(uiSlider.getOid());
      this.vertical = uiSlider.isVertical();
      this.thumbImage = uiSlider.getThumbImage();
      this.minImage = uiSlider.getMinImage();
      this.minTrackImage = uiSlider.getMinTrackImage();
      this.maxImage = uiSlider.getMaxImage();
      this.maxTrackImage = uiSlider.getMaxTrackImage();
   }
   public boolean isVertical() {
      return vertical;
   }

   public void setVertical(boolean vertical) {
      this.vertical = vertical;
   }
  
   public ImageSource getThumbImage() {
      return thumbImage;
   }

   public void setThumbImage(ImageSource thumbImage) {
      this.thumbImage = thumbImage;
   }

   public ImageSource getMinImage() {
      return minImage;
   }

   public void setMinImage(ImageSource minImage) {
      this.minImage = minImage;
   }

   public ImageSource getMinTrackImage() {
      return minTrackImage;
   }

   public void setMinTrackImage(ImageSource minTrackImage) {
      this.minTrackImage = minTrackImage;
   }

   public ImageSource getMaxImage() {
      return maxImage;
   }

   public void setMaxImage(ImageSource maxImage) {
      this.maxImage = maxImage;
   }

   public ImageSource getMaxTrackImage() {
      return maxTrackImage;
   }

   public void setMaxTrackImage(ImageSource maxTrackImage) {
      this.maxTrackImage = maxTrackImage;
   }

   public Slider getSlider() {
      return slider;
   }

   public void setSlider(Slider slider) {
      this.slider = slider;
   }

   @Override
   public List<UICommand> getCommands() {
      List<UICommand> commands = new ArrayList<UICommand>();
      if (slider != null && slider.getSetValueCmd() != null) {
         commands.add(slider.getSetValueCmd());
      }
      return commands;
   }

   @Override
   public String getPanelXml() {
      StringBuffer xmlContent = new StringBuffer();
      xmlContent.append("        <slider id=\"" + getOid() + "\" ");
      if (isThumbUploaded()) {
         xmlContent.append("thumbImage=\"" + thumbImage.getImageFileName() + "\" ");
      }
      if (vertical) {
         xmlContent.append("vertical=\"true\" ");
      }
      if (slider == null || slider.getSetValueCmd() == null) {
         xmlContent.append("passive=\"true\" ");
      }
      xmlContent.append(">\n");
      if(getSensor()!=null){
         xmlContent.append("<link type=\"sensor\" ref=\""+getSensor().getOid()+"\" />\n");
         if (getSensor() instanceof RangeSensor) {
            RangeSensor rangeSensor = (RangeSensor) getSensor();
            xmlContent.append("<min value=\"" + rangeSensor.getMin() + "\"");
            if (isMinImageUploaded()) {
               xmlContent.append(" image=\"" + minImage.getImageFileName() + "\"");
            }
            if (isMinTrackImageUploaded()) {
               xmlContent.append(" trackImage=\"" + minTrackImage.getImageFileName() + "\"");
            }
            xmlContent.append("/>\n");
            
            xmlContent.append("<max value=\"" + rangeSensor.getMax() + "\" ");
            if (isMaxImageUploaded()) {
               xmlContent.append("image=\"" + maxImage.getImageFileName() + "\" ");
            }
            if (isMaxTrackImageUploaded()) {
               xmlContent.append("trackImage=\"" + maxTrackImage.getImageFileName() + "\" ");
            }
            xmlContent.append("/>\n");
         }
      }
      xmlContent.append("        </slider>\n");
      return xmlContent.toString();
   }


   @Override
   public String getName() {
      return "Slider";
   }

   @Override
   public int getPreferredWidth() {
      return vertical ? 44 : 198;
   }

   @Override
   public int getPreferredHeight() {
      return vertical ? 198 : 44;
   }

   @Override
   public Sensor getSensor() {
      if(slider!= null && slider.getSliderSensorRef()!=null){
         return slider.getSliderSensorRef().getSensor();
      }
      return null;
   }

   @Override
   public void setSensor(Sensor sensor) {
      if(slider!= null && slider.getSliderSensorRef()!=null){
         slider.getSliderSensorRef().setSensor(sensor);
      }
   }

   @Override
   @JSON(include = false)
   public Collection<ImageSource> getImageSources() {
      Collection<ImageSource> imageSources = new ArrayList<ImageSource>(2);
      if (this.minImage != null && !this.minImage.isEmpty() && isMinImageUploaded()) {
         imageSources.add(minImage);
      }
      
      if (this.minTrackImage != null && ! this.minTrackImage.isEmpty() && isMinTrackImageUploaded()) {
         imageSources.add(minTrackImage);
      }
      
      if (this.thumbImage != null && ! this.thumbImage.isEmpty() && isThumbUploaded()) {
         imageSources.add(thumbImage);
      }
      
      if (this.maxTrackImage != null && ! this.maxTrackImage.isEmpty() && isMaxTrackImageUploaded()) {
         imageSources.add(maxTrackImage);
      }
      
      if (this.maxImage != null && ! this.maxImage.isEmpty() && isMaxImageUploaded()) {
         imageSources.add(maxImage);
      }
      return imageSources;
   }
   
   public boolean isMinImageUploaded() {
      if (minImage != null && !minImage.isEmpty()) {
         String imageURL = minImage.getSrc();
         return !(DEFAULT_HORIZONTAL_MIN_IMAGE.equals(imageURL) || DEFAULT_VERTICAL_MIN_IMAGE.equals(imageURL)); 
      }
      return false;
   }
   
   public boolean isMinTrackImageUploaded() {
         if (minTrackImage != null && !minTrackImage.isEmpty()) {
         String imageURL = minTrackImage.getSrc();
         return !(DEFAULT_HORIZONTAL_MINTRACK_IMAGE.equals(imageURL) || DEFAULT_VERTICAL_MINTRACK_IMAGE.equals(imageURL));
      }
      return false;
   }
   
   public boolean isThumbUploaded() {
      if (thumbImage !=null && !thumbImage.isEmpty()) {
         String imageURL = thumbImage.getSrc();
         return !(DEFAULT_HORIZONTAL_THUMB_IMAGE.equals(imageURL) || DEFAULT_VERTICAL_THUMB_IMAGE.equals(imageURL)); 
      } 
      return false;
   }
   
   public boolean isMaxTrackImageUploaded() {
      if (maxTrackImage != null && !maxTrackImage.isEmpty()) {
         String imageURL = maxTrackImage.getSrc();
         return !(DEFAULT_HORIZONTAL_MAXTRACK_IMAGE.equals(imageURL) || DEFAULT_VERTICAL_MAXTRACK_IMAGE.equals(imageURL)); 
      }
      return false;
   }
   
   public boolean isMaxImageUploaded() {
      if (maxImage != null && !maxImage.isEmpty()) {
         String imageURL = maxImage.getSrc();
         return !(DEFAULT_HORIZONTAL_MAX_IMAGE.equals(imageURL) || DEFAULT_VERTICAL_MAX_IMAGE.equals(imageURL)); 
      }
      return false;
   }
}
