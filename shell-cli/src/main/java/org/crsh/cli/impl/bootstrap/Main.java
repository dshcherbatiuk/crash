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

package org.crsh.cli.impl.bootstrap;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.impl.Delimiter;
import org.crsh.cli.impl.descriptor.HelpDescriptor;
import org.crsh.cli.impl.invocation.InvocationMatch;
import org.crsh.cli.impl.invocation.InvocationMatcher;
import org.crsh.cli.impl.lang.CommandFactory;
import org.crsh.cli.impl.lang.Instance;
import org.crsh.cli.impl.lang.Util;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Main {

  public static void main(String[] args) throws Exception {
    final ServiceLoader<CommandProvider> loader = ServiceLoader.load(CommandProvider.class);
    final Iterator<CommandProvider> iterator = loader.iterator();

    if (iterator.hasNext()) {
      final StringBuilder line = new StringBuilder();
      for (int i = 0; i < args.length; i++) {
        if (i > 0) {
          line.append(' ');
        }
        Delimiter.EMPTY.escape(args[i], line);
      }

      final CommandProvider commandProvider = iterator.next();
      final Class<?> commandClass = commandProvider.getCommandClass();
      handle(commandClass, line.toString());
    }
  }

  private static <T> void handle(final Class<T> commandClass, final String line) throws Exception {
    final CommandDescriptor<Instance<T>> descriptor = CommandFactory.DEFAULT.create(commandClass);
    final HelpDescriptor<Instance<T>> helpDescriptor = HelpDescriptor.create(descriptor);

    final InvocationMatcher<Instance<T>> matcher = helpDescriptor.matcher();
    final InvocationMatch<Instance<T>> match = matcher.parse(line);

    final T instance = commandClass.newInstance();
    final Object o = match.invoke(Util.wrap(instance));
    if (o != null) {
      System.out.println(o);
    }
  }
}
