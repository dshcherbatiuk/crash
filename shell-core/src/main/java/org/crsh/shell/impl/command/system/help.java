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

import java.util.Map;
import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.ShellSafety;
import org.crsh.shell.impl.command.CRaSH;
import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.Style;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.TableElement;

/** @author Julien Viet */
public class help extends BaseCommand {
  @Usage("provides basic help")
  @Command
  public void main(InvocationContext<Object> context) throws Exception {

    //
    ShellSafety shellSafety = context.getShellSafety();
    boolean safeShell = shellSafety.isSafeShell();
    boolean standAlone = shellSafety.isStandAlone();
    boolean internal = shellSafety.isInternal();
    boolean sshMode = shellSafety.isSshMode();
    boolean manAllowed = shellSafety.isAllowManCommand();

    TableElement table = new TableElement().rightCellPadding(1);
    table.row(
        new LabelElement("NAME").style(Style.style(Decoration.bold)),
        new LabelElement("DESCRIPTION"));

    CRaSH crash = (CRaSH) context.getSession().get("crash");
    java.util.ArrayList<Map.Entry<String, String>> commands = new java.util.ArrayList<>();
    crash.getCommandsSafetyCheck(shellSafety).iterator().forEachRemaining(commands::add);
    commands.sort(java.util.Comparator.comparing(Map.Entry::getKey));

    Color col =
        sshMode
            ? (safeShell ? Color.yellow : Color.red)
            : standAlone
                ? (safeShell ? Color.cyan : Color.red)
                : internal
                    ? (safeShell ? Color.green : Color.red)
                    : (safeShell ? Color.red : Color.red);
    for (Map.Entry<String, String> command : commands) {
      try {
        String desc = command.getValue();
        if (desc == null) {
          desc = "";
        }
        table.row(
            new LabelElement(command.getKey()).style(Style.style(Decoration.bold, col)),
            new LabelElement(desc));
      } catch (Exception ignore) {
        //
      }
    }

    String safeStr = safeShell ? "SAFE-" : "UNSAFE-";
    String sshStr = sshMode ? "SSH-" : "";
    String saStr = standAlone ? "Standalone-" : "";
    String intStr = internal ? "Internal-" : "";
    String manStr = manAllowed ? "ManAllowed-" : "";
    String pref = "[" + safeStr + saStr + intStr + sshStr + manStr + "Shell]: ";
    context.provide(
        new LabelElement(pref + "Try one of these commands with the -h or --help switch:\n"));

    context.provide(table);
  }
}
