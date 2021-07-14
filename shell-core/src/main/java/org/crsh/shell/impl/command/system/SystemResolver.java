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
package org.crsh.shell.impl.command.system;

import java.util.HashMap;
import java.util.Map;
import org.crsh.cli.descriptor.Format;
import org.crsh.cli.impl.descriptor.IntrospectionException;
import org.crsh.command.BaseCommand;
import org.crsh.command.ShellSafety;
import org.crsh.lang.impl.java.ClassShellCommand;
import org.crsh.lang.spi.CommandResolution;
import org.crsh.shell.ErrorKind;
import org.crsh.shell.impl.command.spi.Command;
import org.crsh.shell.impl.command.spi.CommandException;
import org.crsh.shell.impl.command.spi.CommandResolver;

/** @author Julien Viet */
public class SystemResolver implements CommandResolver {

  /** . */
  public static final SystemResolver UNSAFE_INSTANCE = new SystemResolver(false, true, true);

  public static final SystemResolver SAFE_INSTANCE = new SystemResolver(true, false, true);
  public static final SystemResolver SAFE_INSTANCE_WITHOUT_MAN =
      new SystemResolver(true, false, false);
  public static final SystemResolver SEMI_SAFE_INSTANCE = new SystemResolver(true, true, true);
  public static final SystemResolver SEMI_SAFE_INSTANCE_WITHOUT_MAN =
      new SystemResolver(true, true, false);

  /** . */
  private static final HashMap<String, Class<? extends BaseCommand>> unsafeCommands =
      new HashMap<>();

  private static final HashMap<String, Class<? extends BaseCommand>> safeCommands =
      new HashMap<>();
  private static final HashMap<String, Class<? extends BaseCommand>> safeCommandsWithoutMan =
      new HashMap<>();
  private static final HashMap<String, Class<? extends BaseCommand>> semiSafeCommands =
      new HashMap<>();
  private static final HashMap<String, Class<? extends BaseCommand>> semiSafeCommandsWithoutMan =
      new HashMap<>();

  /** . */
  private static final HashMap<String, String> unsafeDescriptions = new HashMap<>();

  private static final HashMap<String, String> safeDescriptions = new HashMap<>();
  private static final HashMap<String, String> safeDescriptionsWithoutMan =
      new HashMap<>();
  private static final HashMap<String, String> semiSafeDescriptions = new HashMap<>();
  private static final HashMap<String, String> semiSafeDescriptionsWithoutMan =
      new HashMap<>();

  static {
    unsafeCommands.put("help", help.class);
    unsafeCommands.put("repl", repl.class);

    unsafeDescriptions.put("help", "provides basic help (all commands).");
    unsafeDescriptions.put("repl", "list the repl or change the current repl");
    unsafeDescriptions.put("exit", "Exits.");
    unsafeDescriptions.put("bye", "Exits, same as exit.");

    // Add handlers for commands such that the user gets a suitable message if they try to run in
    // safe mode.
    UnsafeSafeModeCmdResolution.addSafeHandlers(safeCommands, false, true);
    UnsafeSafeModeCmdResolution.addSafeHandlers(safeCommandsWithoutMan, false, false);
    UnsafeSafeModeCmdResolution.addSafeHandlers(semiSafeCommands, true, true);
    UnsafeSafeModeCmdResolution.addSafeHandlers(semiSafeCommandsWithoutMan, true, false);

    safeCommands.put("help", help.class);
    safeDescriptions.put("help", "provides basic help (safe mode commands).");
    safeCommandsWithoutMan.put("help", help.class);
    safeDescriptionsWithoutMan.put("help", "provides basic help (safe mode commands).");

    semiSafeCommands.put("help", help.class);
    semiSafeDescriptions.put("help", "provides basic help (safe mode commands).");
    semiSafeDescriptions.put("exit", "Exits (currently permitted in safe mode shell).");
    semiSafeDescriptions.put(
        "bye", "Exits (currently permitted in safe mode shell), same as exit.");
    semiSafeCommandsWithoutMan.put("help", help.class);
    semiSafeDescriptionsWithoutMan.put("help", "provides basic help (safe mode commands).");
    semiSafeDescriptionsWithoutMan.put("exit", "Exits (currently permitted in safe mode shell).");
    semiSafeDescriptionsWithoutMan.put(
        "bye", "Exits (currently permitted in safe mode shell), same as exit.");
  }

  private final boolean safeInstance;
  private final boolean allowExit;
  private final boolean allowMan;

  private SystemResolver(boolean safe, boolean allowExit, boolean allowMan) {
    this.safeInstance = safe;
    this.allowExit = allowExit; // Ignored in unsafe mode as exit is allowed
    this.allowMan = allowMan; // Ignored in unsafe mode as man is allowed
  }

  @Override
  public Iterable<Map.Entry<String, String>> getDescriptions(ShellSafety shellSafety) {
    return safeInstance
        ? (allowExit
            ? (allowMan
                ? semiSafeDescriptions.entrySet()
                : semiSafeDescriptionsWithoutMan.entrySet())
            : (allowMan ? safeDescriptions.entrySet() : safeDescriptionsWithoutMan.entrySet()))
        : unsafeDescriptions.entrySet();
  }

  @Override
  public Command<?> resolveCommand(String name, ShellSafety shellSafety)
      throws CommandException, NullPointerException {
    final Class<? extends BaseCommand> systemCommand =
        safeInstance
            ? (allowExit
                ? (allowMan ? semiSafeCommands.get(name) : semiSafeCommandsWithoutMan.get(name))
                : (allowMan ? safeCommands.get(name) : safeCommandsWithoutMan.get(name)))
            : unsafeCommands.get(name);
    if (systemCommand != null) {
      return createCommand(systemCommand, shellSafety).getCommand();
    }
    return null;
  }

  private <C extends BaseCommand> CommandResolution createCommand(
      final Class<C> commandClass, ShellSafety shellSafety) throws CommandException {
    final ClassShellCommand<C> shellCommand;
    final String description;
    try {
      shellCommand = new ClassShellCommand<C>(commandClass, shellSafety);
      description = shellCommand.describe(commandClass.getSimpleName(), Format.DESCRIBE);
    } catch (IntrospectionException e) {
      throw new CommandException(
          ErrorKind.SYNTAX, "Invalid cli annotation in command " + commandClass.getSimpleName(), e);
    }
    return new CommandResolution() {
      @Override
      public String getDescription() {
        return description;
      }

      @Override
      public Command<?> getCommand() {
        return shellCommand;
      }
    };
  }
}
