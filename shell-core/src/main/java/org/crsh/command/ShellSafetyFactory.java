package org.crsh.command;

import java.util.HashMap;

public class ShellSafetyFactory {
  private static HashMap<Long, ShellSafety> safetyByThread = new HashMap<Long, ShellSafety>();

  public static ShellSafety getCurrentThreadShellSafety() {
    long threadId = Thread.currentThread().getId();
    synchronized (safetyByThread) {
      if (safetyByThread.containsKey(threadId)) {
        return safetyByThread.get(threadId);
      }
    }

    ShellSafety ret = new ShellSafety();
    ret.setSafeShell(false);
    ret.setDefault(true);
    return ret;
  }

  public static void registerShellSafetyForThread(ShellSafety shellSafety) {
    long threadId = Thread.currentThread().getId();
    synchronized (safetyByThread) {
      safetyByThread.put(threadId, shellSafety);
    }
  }
}
