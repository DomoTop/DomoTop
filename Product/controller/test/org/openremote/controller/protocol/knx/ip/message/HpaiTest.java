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
package org.openremote.controller.protocol.knx.ip.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.knx.ip.message.Hpai;

public class HpaiTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testHpai() throws IOException {
    Hpai h1 = new Hpai(new InetSocketAddress(InetAddress.getByName("255.128.127.1"), 2555));
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    h1.write(os);
    byte[] r = os.toByteArray();
    Assert.assertArrayEquals(new byte[] { 0x08, 0x01, (byte) 255, (byte) 128, 127, 1, 0x09, (byte) 0xFB }, r);
    byte[] i = new byte[] { 0x08, 0x01, (byte) 255, (byte) 128, 127, 1, 0x09, (byte) 0xFB };
    ByteArrayInputStream is = new ByteArrayInputStream(i);
    Hpai h2 = new Hpai(is);
    Assert.assertEquals(new InetSocketAddress(InetAddress.getByName("255.128.127.1"), 2555), h2.getAddress());
  }
}
