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

package org.crsh.vfs.spi;

import java.io.IOException;

public abstract class AbstractFSDriver<H> implements FSDriver<H> {

  /**
   * A simple implementation that iterates over the children to return the one specified by the
   * <code>name</code> argument. Subclasses can override this method to provide a more efficient
   * implementation.
   *
   * @param handle the directory handle
   * @param name the child name
   * @return the child or null
   * @throws IOException any io exception
   */
  @Override
  public H child(H handle, String name) throws IOException {
    if (handle == null) {
      throw new NullPointerException();
    }
    if (name == null) {
      throw new NullPointerException();
    }
    for (H child : children(handle)) {
      String childName = name(child);
      if (childName.equals(name)) {
        return child;
      }
    }
    return null;
  }
}
