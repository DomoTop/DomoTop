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

/**
 * The Class PanelPosition.
 */
@SuppressWarnings("serial")
public class Position implements Serializable {
   
   /** The pos x. */
   private int posX;
   
   /** The pos y. */
   private int posY;
   
   /**
    * Instantiates a new position.
    */
   public Position() {
      super();
   }
   
   /**
    * Instantiates a new position.
    * 
    * @param posX the pos x
    * @param posY the pos y
    */
   public Position(int posX, int posY) {
      this.posX = posX;
      this.posY = posY;
   }
   
   /**
    * Gets the pos x.
    * 
    * @return the pos x
    */
   public int getPosX() {
      return posX;
   }
   
   /**
    * Gets the pos y.
    * 
    * @return the pos y
    */
   public int getPosY() {
      return posY;
   }
   
   /**
    * Sets the pos x.
    * 
    * @param posX the new pos x
    */
   public void setPosX(int posX) {
      this.posX = posX;
   }
   
   /**
    * Sets the pos y.
    * 
    * @param posY the new pos y
    */
   public void setPosY(int posY) {
      this.posY = posY;
   }
   
}
