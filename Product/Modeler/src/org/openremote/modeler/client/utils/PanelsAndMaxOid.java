package org.openremote.modeler.client.utils;

import java.util.Collection;

import org.openremote.modeler.domain.BusinessEntity;
import org.openremote.modeler.domain.Panel;

public class PanelsAndMaxOid extends BusinessEntity{
 
   private static final long serialVersionUID = 2451009088912750552L;
   
   private Collection<Panel> panels ;
   private long maxOid;
   
   public PanelsAndMaxOid(Collection<Panel> panels,long maxOid ){
      this.panels = panels;
      this.maxOid = maxOid;
   }
   public PanelsAndMaxOid(){}
   public Collection<Panel> getPanels() {
      return panels;
   }

   public long getMaxOid() {
      return maxOid;
   }
}
