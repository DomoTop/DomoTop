package org.openremote.modeler.server;

import java.util.Set;

import org.openremote.modeler.client.rpc.ConfigCategoryRPCService;
import org.openremote.modeler.domain.ConfigCategory;
import org.openremote.modeler.service.ControllerConfigService;

@SuppressWarnings("serial")
public class ConfigCategoryController extends BaseGWTSpringController implements ConfigCategoryRPCService{
   private ControllerConfigService controllerConfigService = null;

   @Override
   public Set<ConfigCategory> getCategories() {
      return controllerConfigService.listAllCategory();
   }
   
   public void setControllerConfigService(ControllerConfigService controllerConfigService) {
      this.controllerConfigService = controllerConfigService;
   }

}
