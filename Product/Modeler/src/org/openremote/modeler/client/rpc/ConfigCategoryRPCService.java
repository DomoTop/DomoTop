package org.openremote.modeler.client.rpc;

import java.util.Set;

import org.openremote.modeler.domain.ConfigCategory;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
@RemoteServiceRelativePath("configCategory.smvc")
public interface ConfigCategoryRPCService extends RemoteService{
   public Set<ConfigCategory> getCategories();
}
