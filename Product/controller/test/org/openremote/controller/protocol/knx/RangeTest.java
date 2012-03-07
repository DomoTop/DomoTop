/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.protocol.knx;

import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.protocol.knx.datatype.DataPointType;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.jdom.Element;

/**
 * Unit tests for KNX Range command.
 *
 * TODO: NYI -- See ScalingTest for a template
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class RangeTest
{


  // Test Setup -----------------------------------------------------------------------------------

  private KNXCommandBuilder builder = null;

  @Before public void setUp()
  {
    builder = new KNXCommandBuilder("127.0.0.1", 9999);
  }



  // Tests ----------------------------------------------------------------------------------------


  /**
   * Test KNX command parsing with "RANGE" as the command string.
   *
   */
  @Test public void testRangeCommand() throws InvalidGroupAddressException
  {
    GroupAddress addr = new GroupAddress("5/5/5");

    Command cmd = getCommand("RANGE", addr, 0);

    Assert.assertTrue(cmd instanceof GroupValueWrite);

    KNXCommand knx = (KNXCommand)cmd;

    Assert.assertTrue(knx.getAddress().equals(addr));
    Assert.assertTrue(knx.getDataPointType() == DataPointType.SCALING);
  }



  @Test public void testNotYetImplemented() throws InvalidGroupAddressException
  {

    Assert.fail("Not Yet Implemented (See ORCJAVA-69)");
  }



  // Helpers --------------------------------------------------------------------------------------


  private Command getCommand(String cmd, GroupAddress groupAddress, int value)
  {
    return getCommand(cmd, GroupAddress.formatToMainMiddleSub(groupAddress.asByteArray()), value);
  }


  private Command getCommand(String cmd, String groupAddress, int value)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME, "knx");
    ele.setAttribute(Command.DYNAMIC_VALUE_ATTR_NAME, Integer.toString(value));

    Element propAddr = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                          KNXCommandBuilder.KNX_XMLPROPERTY_GROUPADDRESS);
    propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                          groupAddress);

    ele.addContent(propAddr);

    Element propAddr2 = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propAddr2.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                           KNXCommandBuilder.KNX_XMLPROPERTY_COMMAND);
    propAddr2.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                           cmd);

    ele.addContent(propAddr2);

    Element propAddr3 = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propAddr3.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                           KNXCommandBuilder.KNX_XMLPROPERTY_DPT);
    propAddr3.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                           DataPointType.SCALING.getDPTID());

    ele.addContent(propAddr3);


    return builder.build(ele);
  }


}

