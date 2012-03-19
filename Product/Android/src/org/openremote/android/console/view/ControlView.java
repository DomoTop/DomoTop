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

import java.io.InputStream;
import java.util.Timer;

import org.apache.http.HttpResponse;
import org.openremote.android.console.LoginDialog;
import org.openremote.android.console.bindings.ColorPicker;
import org.openremote.android.console.bindings.Component;
import org.openremote.android.console.bindings.ORButton;
import org.openremote.android.console.bindings.Slider;
import org.openremote.android.console.bindings.Switch;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.model.ControllerException;
import org.openremote.android.console.model.ViewHelper;
import org.openremote.android.console.net.ORConnectionDelegate;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.net.ORUnBlockConnection;

import android.content.Context;

/**
 * The super class of all control view, include ButtonView, SwitchView and SliderView.
 */
public class ControlView extends ComponentView implements ORConnectionDelegate {

   /** The repeat send command timer. */
   private Timer timer;
   private Context context;
   protected ControlView(Context context) {
      super(context);
      this.context = context;
   }

   public static ControlView buildWithControl(Context context, Component control) {
      ControlView controlView = null;
      if (control instanceof ORButton) {
         controlView = new ButtonView(context, (ORButton) control);
      } else if (control instanceof Switch) {
         controlView = new SwitchView(context, (Switch) control);
      } else if (control instanceof Slider) {
         controlView = new SliderView(context, (Slider) control);
      } else if (control instanceof ColorPicker) {
         controlView = new ColorPickerView(context, (ColorPicker)control);
      }
      return controlView;
   }

   /**
    * Send command request to controller by command type.
    * 
    * @param commandType the command type
    * 
    * @return true, if successful
    */
   public boolean sendCommandRequest(String commandType) {
      new ORUnBlockConnection(this.context, ORHttpMethod.POST, true, AppSettingsModel.getSecuredServer(getContext())
            + "/rest/control/" + getComponent().getComponentId() + "/" + commandType, this);
      return true;
   }

   /**
    * Cancel repeat send command.
    */
   public void cancelTimer() {
      if (timer != null) {
         timer.cancel();
      }
      timer = null;
   }

   public void setTimer(Timer timer) {
      this.timer = timer;
   }

   /**
    * Handle server error with status code.
    * If status code not equals 200, cancel the timer, and display alert with error message.
    * 
    * @param statusCode the status code
    */
   public void handleServerErrorWithStatusCode(int statusCode) {
      if (statusCode != 200) {
         cancelTimer();
         ViewHelper.showAlertViewWithTitle(getContext(), "Send Request Error", ControllerException
               .exceptionMessageOfCode(statusCode));
      }
   }

   @Override
   public void urlConnectionDidFailWithException(Exception e) {
      cancelTimer();
   }

   @Override
   public void urlConnectionDidReceiveData(InputStream data) {
      // do nothing.
   }

   @Override
   public void urlConnectionDidReceiveResponse(HttpResponse httpResponse) {
      int responseCode = httpResponse.getStatusLine().getStatusCode();
      if (responseCode != 200) {
         cancelTimer();
         if (responseCode == 401) {
            new LoginDialog(getContext());
         } else {
            handleServerErrorWithStatusCode(responseCode);
         }
      }
   }
}
