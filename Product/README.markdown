DomoTop
========

Note: this product is still in development

DomoTop is TASS Project and graduation assignment for school. The assignment is to create a domotica system with existing home automation protocols and controlled via a mobile device. 

Build Requirements
-------------------

The following packages are required to make it work:
- hsqldb (can be found on this GitHub, read below)

Installing
-----------

###Controller
1. Build the Controller from source either via ant war
2. Deploy the war from the output directory into the Tomcat Web Application Manager
3. Copy the hsqldb.jar located in Controller/lib/hsqldb/ folder to ~tomcat6/lib
4. Restart Apache Tomcat 6 Server

__Or__

1. Download the War from: http://openlaptop.nl/openremote/controller.war
2. Deploy the war from the output directory into the Tomcat Web Application Manager
3. Copy the hsqldb.jar located in Controller/lib/hsqldb/ folder to ~tomcat6/lib
4. Restart Apache Tomcat 6 Server

###Android
1. You may need to go to Menu->Settings->Applications and check Unknown sources
2. Scan the QR-code below to download & install the application:

![Android APK](http://qrcode.kaywa.com/img.php?s=6&d=http%3A%2F%2Fopenlaptop.nl%2Fopenremote%2FOpenRemoteConsole.apk)

Usage
------
Go to the OpenRemote Controller - Administrator Panel

1. http://localhost:8080/controller/login
2. Login with your OpenRemote Composer Account
3. Select the Controller on your (Android) device
4. Approve the user in the User management tab (enter the pin which is shown on the device)


Uninstalling
-------------
1. Uninstall the APK
2. Undeploy the OpenRemote Controller from the Tomcat Web Application Manager
3. Remove the hsqldb.jar from the ~tomcat6/lib directory

Interesting Links
------------------


