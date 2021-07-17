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

package org.crsh.cli.descriptor;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import org.crsh.cli.completers.Completer;
import org.crsh.cli.impl.Multiplicity;
import org.crsh.cli.impl.ParameterType;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.descriptor.IllegalParameterException;
import org.crsh.cli.impl.descriptor.IllegalValueTypeException;
import org.crsh.cli.type.ValueType;

public class OptionDescriptor extends ParameterDescriptor {

  private static final BitSet A = new BitSet(256);

  private static final BitSet B = new BitSet(256);

  static {
    for (char c = 'a'; c <= 'z'; c++) {
      A.set(c);
      A.set(c + 'A' - 'a');
    }
    B.or(A);
    B.set('-');
  }

  private static void checkChar(String s, int index, BitSet authorized)
      throws IllegalParameterException {
    if (!authorized.get(s.charAt(index))) {
      throw new IllegalParameterException(
          "Option name " + s + " cannot contain " + s.charAt(index) + " at position " + index);
    }
  }

  private final int arity;

  private final List<String> names;

  public OptionDescriptor(
      final ParameterType<?> type,
      List<String> names,
      final Description info,
      final boolean required,
      final boolean password,
      final boolean unquote,
      final Class<? extends Completer> completerType,
      final Annotation annotation)
      throws IllegalValueTypeException, IllegalParameterException {
    super(type, info, required, password, unquote, completerType, annotation);

    if (getMultiplicity() == Multiplicity.MULTI && getType() == ValueType.BOOLEAN) {
      throw new IllegalParameterException();
    }

    names = new ArrayList<>(names);
    for (String name : names) {
      if (name == null) {
        throw new IllegalParameterException("Option name must not be null");
      }

      int length = name.length();
      if (length == 0) {
        throw new IllegalParameterException("Option name cannot be empty");
      }

      if (!A.get(name.charAt(0))) {
        throw new IllegalParameterException(
            "Option name " + name + " cannot start with " + name.charAt(0));
      }

      checkChar(name, 0, A);
      checkChar(name, length - 1, A);

      for (int i = 1; i < length - 1; i++) {
        checkChar(name, i, B);
      }
    }

    if (getType() == ValueType.BOOLEAN) {
      arity = 0;
    } else {
      arity = 1;
    }

    this.names = Collections.unmodifiableList(names);
  }

  public int getArity() {
    return arity;
  }

  public List<String> getNames() {
    return names;
  }

  @Override
  public Object parse(List<String> values) throws SyntaxException {
    if (arity == 0) {
      if (values.size() > 0) {
        throw new SyntaxException("Too many values " + values + " for option " + names.get(0));
      }
      // It's a boolean and it is true
      return Boolean.TRUE;
    }

    if (getMultiplicity() == Multiplicity.SINGLE) {
      if (values.size() > 1) {
        throw new SyntaxException("Too many values " + values + " for option " + names.get(0));
      }

      if (values.size() == 0) {
        throw new SyntaxException("Missing option " + names.get(0) + " value");
      }

      final String value = values.get(0);
      try {
        return parse(value);
      } catch (Exception e) {
        throw new SyntaxException(
            "Could not parse value <" + value + "> for option " + names.get(0));
      }
    }

    final List<Object> v = new ArrayList<Object>(values.size());
    for (String value : values) {
      try {
        v.add(parse(value));
      } catch (Exception e) {
        throw new SyntaxException(
            "Could not parse value <" + value + "> for option " + names.get(0));
      }
    }
    return v;
  }

  /**
   * Prints the option names as an alternative of switches surrounded by a square brace, for
   * instance: "[-f --foo]"
   *
   * @param writer the writer to print to
   * @throws IOException any io exception
   */
  public void printUsage(final Appendable writer) throws IOException {
    writer.append("[");
    boolean a = false;

    for (String optionName : names) {
      if (a) {
        writer.append(" | ");
      }
      writer.append(optionName.length() == 1 ? "-" : "--").append(optionName);
      a = true;
    }
    writer.append("]");
  }

  @Override
  public String toString() {
    return "OptionDescriptor[" + names + "]";
  }
}
