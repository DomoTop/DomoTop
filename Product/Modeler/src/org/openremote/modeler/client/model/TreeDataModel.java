/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
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
package org.openremote.modeler.client.model;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;


/**
 * The Class TreeDataModel.
 */
public class TreeDataModel extends BaseTreeModel implements Serializable {

   /** The Constant DATA. */
   private static final String DATA = "data";
   
   /** The Constant LABEL. */
   private static final String LABEL = "label";
   
   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = -3212925111586567858L;

   /**
    * Instantiates a new tree data model.
    * 
    * @param o the o
    * @param label the label
    */
   public TreeDataModel(Object o, String label) {
      set(LABEL, label);
      set(DATA, o);
   }

   /**
    * Gets the label.
    * 
    * @return the label
    */
   public String getLabel() {
      return (String) get(LABEL);
   }
   
   /**
    * Gets the display property.
    * 
    * @return the display property
    */
   public static String getDisplayProperty() {
      return LABEL;
   }
   
   /**
    * Gets the data property.
    * 
    * @return the data property
    */
   public static String getDataProperty() {
      return DATA;
   }

   /**
    * Get the model's label.
    * @return label.
    */
   public String toString() {
      return getLabel();
   }

   /**
    * Gets the data.
    * 
    * @param <X> the X
    * @return the data
    */
   @SuppressWarnings("unchecked")
   public <X> X getData() {
      return (X) get(DATA);
   }
   
  
}


