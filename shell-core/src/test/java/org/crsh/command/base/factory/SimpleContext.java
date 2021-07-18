/*
 * Copyright (C) 2012 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.crsh.command.base.factory;

import java.util.ArrayList;
import java.util.List;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/** @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a> */
public class SimpleContext extends EmptyContext {

  @Override
  public NamingEnumeration<Binding> listBindings(String name) throws NamingException {

    if (name.startsWith("java:global") || name == "") {
      List<Binding> l = new ArrayList<Binding>();
      l.add(new Binding("Foo", "Bar", ""));
      return new Bindings(l);
    } else {
      throw new NamingException();
    }
  }
}