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

package org.crsh.cli.impl.invocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.crsh.cli.descriptor.CommandDescriptor;
import org.crsh.cli.descriptor.OptionDescriptor;
import org.crsh.cli.impl.LiteralValue;
import org.crsh.cli.impl.SyntaxException;
import org.crsh.cli.impl.parser.Event;
import org.crsh.cli.impl.parser.Mode;
import org.crsh.cli.impl.parser.Parser;
import org.crsh.cli.impl.tokenizer.Token;
import org.crsh.cli.impl.tokenizer.Tokenizer;
import org.crsh.cli.impl.tokenizer.TokenizerImpl;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class InvocationMatcher<T> {

  private final CommandDescriptor<T> descriptor;

  private final Iterable<Token> tokens;

  public InvocationMatcher(final CommandDescriptor<T> descriptor) {
    this(descriptor, Collections.emptyList());
  }

  private InvocationMatcher(final CommandDescriptor<T> descriptor, final Iterable<Token> tokens) {
    this.descriptor = descriptor;
    this.tokens = tokens;
  }

  public InvocationMatcher<T> subordinate(final String name) throws SyntaxException {
    TokenList tokens = new TokenList(this.tokens);
    if (name != null && name.length() > 0) {
      tokens.add(new Token.Literal.Word(tokens.last(), name));
    }
    return new InvocationMatcher<>(descriptor, tokens);
  }

  public InvocationMatcher<T> option(final String optionName, List<?> optionValue)
      throws SyntaxException {
    return options(Collections.singletonMap(optionName, optionValue));
  }

  public InvocationMatcher<T> options(final Map<String, List<?>> options) throws SyntaxException {
    TokenList tokens = new TokenList(this.tokens);
    for (Map.Entry<String, List<?>> option : options.entrySet()) {
      tokens.addOption(option.getKey(), option.getValue());
    }
    return new InvocationMatcher<T>(descriptor, tokens);
  }

  public InvocationMatch<T> arguments(final List<?> arguments) throws SyntaxException {
    final TokenList tokens = new TokenList(this.tokens);
    for (Object argument : arguments) {
      tokens.add(new Token.Literal.Word(tokens.last(), argument.toString()));
    }
    return match(tokens);
  }

  public InvocationMatch<T> parse(final String s) throws SyntaxException {
    final ArrayList<Token> tokens = new ArrayList<>();
    for (Token token : this.tokens) {
      tokens.add(token);
    }

    for (Iterator<Token> i = new TokenizerImpl(s); i.hasNext(); ) {
      tokens.add(i.next());
    }
    return match(tokens);
  }

  private InvocationMatch<T> match(final Iterable<Token> tokens) {
    return match(new Tokenizer() {
      final Iterator<Token> i = tokens.iterator();

      @Override
      protected Token parse() {
        return i.hasNext() ? i.next() : null;
      }
    });
  }

  private InvocationMatch<T> match(final Tokenizer tokenizer) {
    final Parser<T> parser = new Parser<>(tokenizer, descriptor, Mode.INVOKE);
    InvocationMatch<T> current = new InvocationMatch<>(descriptor);

    while (true) {
      final Event event = parser.next();
      if (event instanceof Event.Separator) {
        //
      } else if (event instanceof Event.Stop) {
        break;
      }

      if (event instanceof Event.Option) {
        final Event.Option optionEvent = (Event.Option) event;
        final OptionDescriptor desc = optionEvent.getParameter();
        final Iterable<OptionMatch> options = current.options();
        OptionMatch option = null;

        for (OptionMatch om : options) {
          if (om.getParameter().equals(desc)) {
            final List<LiteralValue> v = new ArrayList<>(om.getValues());
            v.addAll(bilto(optionEvent.getValues()));
            final List<String> names = new ArrayList<>(om.getNames());
            names.add(optionEvent.getToken().getName());
            option = new OptionMatch(desc, names, v);
            break;
          }
        }

        if (option == null) {
          option = new OptionMatch(
              desc, optionEvent.getToken().getName(), bilto(optionEvent.getValues()));
        }
        current.option(option);
      } else if (event instanceof Event.Subordinate) {
        current = current.subordinate(((Event.Subordinate) event).getDescriptor().getName());
      } else if (event instanceof Event.Argument) {
        final Event.Argument argumentEvent = (Event.Argument) event;
        final List<Token.Literal> values = argumentEvent.getValues();
        final ArgumentMatch match;
        if (values.size() > 0) {
          match = new ArgumentMatch(
              argumentEvent.getParameter(),
              argumentEvent.getFrom(),
              argumentEvent.getTo(),
              bilto(argumentEvent.getValues()));
          if (argumentEvent.getCommand() == current.getDescriptor()) {
            current.argument(match);
          } else {
            throw new AssertionError();
          }
        }
      }
    }

    final StringBuilder rest = new StringBuilder();
    while (tokenizer.hasNext()) {
      Token token = tokenizer.next();
      rest.append(token.getRaw());
    }
    current.setRest(rest.toString());

    return current;
  }

  private List<LiteralValue> bilto(final List<? extends Token.Literal> literals) {
    final List<LiteralValue> values = new ArrayList<>(literals.size());
    for (Token.Literal literal : literals) {
      values.add(new LiteralValue(literal.getRaw(), literal.getValue()));
    }
    return values;
  }
}
