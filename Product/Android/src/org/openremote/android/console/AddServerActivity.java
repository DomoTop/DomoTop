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
package org.openremote.android.console;

import java.net.URI;
import java.net.URISyntaxException;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is the add custom server screen. Mainly it configures the URL of 
 * your device (which is assumed to be the root of controller.xml.
 * 
 * @author Andrew C. Oliver <acoliver at osintegrators.com>
 */
public class AddServerActivity extends GenericActivity {
    public static final String OPEN_REMOTE_PREFS = "openRemoteConfig";

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Log.d("OpenRemote-" + this.toString(), "onCreate for configure activity");
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(320, 480));

        TextView tv = new TextView(this);
        tv.setText(R.string.enter_device_url);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextSize(18);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        EditText et = new EditText(this);
        et.setTag(Constants.URL);
        Button b = new Button(this);
        b.setText(R.string.save);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String noProtocolUrl = ((EditText) ((View) v.getParent())
                        .findViewWithTag(Constants.URL)).getText().toString();
                if (noProtocolUrl == null) {
                   return;
                }
                try {
                  String httpUrl = "http://" + noProtocolUrl;
                  new URI(httpUrl);
                  Intent intent = getIntent();
                  intent.setData(Uri.parse(noProtocolUrl));
                  setResult(Constants.RESULT_CONTROLLER_URL, intent);
                  finish();
               } catch (URISyntaxException e) {
                  Toast toast = Toast.makeText(getApplicationContext(), "URL format is not correct.", 1);
                  toast.show();
               }
            }
        });
        layout.addView(tv);
        layout.addView(et);
        layout.addView(b);
        this.setContentView(layout);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return handleMenu(item);
    }

    public boolean handleMenu(MenuItem item) {
        switch (item.getItemId()) {
        case Constants.MENU_ITEM_QUIT:
            System.exit(0);
            return true;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        populateMenu(menu);
        return true;
    }

    public void populateMenu(Menu menu) {
        menu.setQwertyMode(true);
        MenuItem quit = menu.add(0, Constants.MENU_ITEM_QUIT, 0, R.string.back);
        quit.setIcon(R.drawable.ic_menu_revert);
    }

}
