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

package org.crsh.text.ui;

import com.google.auto.service.AutoService;
import java.util.Iterator;
import java.util.LinkedList;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;

@AutoService(Renderer.class)
public class UIBuilderRenderer extends Renderer<UIBuilder> {

  @Override
  public Class<UIBuilder> getType() {
    return UIBuilder.class;
  }

  @Override
  public LineRenderer renderer(Iterator<UIBuilder> stream) {
    final LinkedList<LineRenderer> renderers = new LinkedList<>();
    while (stream.hasNext()) {
      for (Element element : stream.next().getElements()) {
        renderers.add(element.renderer());
      }
    }
    return LineRenderer.vertical(renderers);
  }
}
