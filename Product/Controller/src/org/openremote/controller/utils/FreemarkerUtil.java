package org.openremote.controller.utils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.openremote.controller.ControllerConfiguration;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerUtil {
   
   /**
    *  Process a template using FreeMarker and print the results
    * @param root HashMap with data
    * @param template the name of the template file
    * @return HTML template with the specified data
    * @throws IOException FreeMarker exception
    */
   public static String freemarkerDo(Map root, String template) throws IOException, TemplateException
   {
      Configuration cfg = new Configuration();
      // Read the XML file and process the template using FreeMarker
      ControllerConfiguration configuration = ControllerConfiguration.readXML();
      
      cfg.setDirectoryForTemplateLoading(new File(configuration.getResourcePath()));
      cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
      Template temp = cfg.getTemplate(template);
      StringWriter out = new StringWriter();
      temp.process( root, out );

      return out.getBuffer().toString();
   }
}
