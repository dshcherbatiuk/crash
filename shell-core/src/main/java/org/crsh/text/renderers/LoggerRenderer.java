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

package org.crsh.text.renderers;

import com.google.auto.service.AutoService;
import java.util.Iterator;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.slf4j.Logger;

@AutoService(Renderer.class)
public class LoggerRenderer extends Renderer<Logger> {

  @Override
  public Class<Logger> getType() {
    return Logger.class;
  }

  @Override
  public LineRenderer renderer(Iterator<Logger> stream) {
    final TableElement table = new TableElement();

    // Header
    table.add(new RowElement()
        .style(Decoration.bold.fg(Color.black).bg(Color.white))
        .add("NAME", "LEVEL"));

    while (stream.hasNext()) {
      final Logger logger = stream.next();

      // Determine level
      String level;
      if (logger.isTraceEnabled()) {
        level = "TRACE";
      } else if (logger.isDebugEnabled()) {
        level = "DEBUG";
      } else if (logger.isInfoEnabled()) {
        level = "INFO";
      } else if (logger.isWarnEnabled()) {
        level = "WARN";
      } else if (logger.isErrorEnabled()) {
        level = "ERROR";
      } else {
        level = "UNKNOWN";
      }

      table.row(logger.getName(), level);
    }

    return table.renderer();
  }
}
