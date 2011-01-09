/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.crsh.cmdline;

import org.crsh.cmdline.binding.TypeBinding;
import org.crsh.cmdline.spi.Completer;

import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class OptionDescriptor<B extends TypeBinding> extends ParameterDescriptor<B> {

  /** . */
  private final int arity;

  /** . */
  private final List<String> names;

  public OptionDescriptor(
    B binding,
    Type javaType,
    List<String> names,
    InfoDescriptor info,
    boolean required,
    int arity,
    boolean password,
    Class<? extends Completer> completerType,
    Annotation annotation) throws IllegalValueTypeException, IllegalParameterException {
    super(
      binding,
      javaType,
      info,
      required,
      password,
      completerType,
      annotation);

    //
    if (arity > 1 && getMultiplicity() == Multiplicity.SINGLE) {
      throw new IllegalParameterException();
    }

    //
    if (getMultiplicity() == Multiplicity.LIST && getType() == SimpleValueType.BOOLEAN) {
      throw new IllegalParameterException();
    }

    //
    names = new ArrayList<String>(names);
    for (String name : names) {
      if (name.length() == 0) {
        throw new IllegalParameterException("Option name cannot be empty");
      }
      if (name == null) {
        throw new IllegalParameterException("Option name must not be null");
      }
      if (name.contains("-")) {
        throw new IllegalParameterException("Option name must not contain the hyphen character");
      }
    }

    //
    if (getType() == SimpleValueType.BOOLEAN && arity < 1) {
      arity = 0;
    } else {
      if (arity == -1) {
        arity = 1;
      }
    }

    //
    this.arity = arity;
    this.names = Collections.unmodifiableList(names);
  }

  public int getArity() {
    return arity;
  }

  public List<String> getNames() {
    return names;
  }

  public void printUsage(PrintWriter writer) {
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