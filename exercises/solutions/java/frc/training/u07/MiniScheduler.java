package frc.training.u07;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reference solution for exercise u07-scheduler. */
public final class MiniScheduler {
  private final Map<MiniSubsystem, MiniCommand> subsystems = new LinkedHashMap<>();
  private final Set<MiniCommand> scheduled = new LinkedHashSet<>();
  private final Map<MiniSubsystem, MiniCommand> requirements = new HashMap<>();

  public void registerSubsystem(MiniSubsystem subsystem) {
    if (!subsystems.containsKey(subsystem)) {
      subsystems.put(subsystem, null);
    }
  }

  public void setDefaultCommand(MiniSubsystem subsystem, MiniCommand command) {
    if (!command.getRequirements().contains(subsystem)) {
      throw new IllegalArgumentException(
          "Default commands must require their subsystem: " + subsystem.getName());
    }
    // Replacing a LinkedHashMap value keeps the key's original position.
    subsystems.put(subsystem, command);
  }

  public void schedule(MiniCommand command) {
    if (scheduled.contains(command)) {
      return;
    }
    for (MiniSubsystem requirement : command.getRequirements()) {
      MiniCommand current = requirements.get(requirement);
      if (current != null) {
        cancel(current);
      }
    }
    command.initialize();
    scheduled.add(command);
    for (MiniSubsystem requirement : command.getRequirements()) {
      requirements.put(requirement, command);
    }
  }

  public void cancel(MiniCommand command) {
    if (!scheduled.remove(command)) {
      return;
    }
    freeRequirements(command);
    command.end(true);
  }

  public boolean isScheduled(MiniCommand command) {
    return scheduled.contains(command);
  }

  public MiniCommand requiring(MiniSubsystem subsystem) {
    return requirements.get(subsystem);
  }

  public void run() {
    for (MiniSubsystem subsystem : new ArrayList<>(subsystems.keySet())) {
      subsystem.periodic();
    }

    // Loop over a copy: execute() and end() may schedule or cancel commands while we iterate.
    List<MiniCommand> snapshot = new ArrayList<>(scheduled);
    for (MiniCommand command : snapshot) {
      if (!scheduled.contains(command)) {
        continue; // canceled earlier in this loop
      }
      command.execute();
      if (command.isFinished()) {
        scheduled.remove(command);
        freeRequirements(command);
        command.end(false);
      }
    }

    for (Map.Entry<MiniSubsystem, MiniCommand> entry : new ArrayList<>(subsystems.entrySet())) {
      MiniCommand defaultCommand = entry.getValue();
      if (defaultCommand != null && !requirements.containsKey(entry.getKey())) {
        schedule(defaultCommand);
      }
    }
  }

  private void freeRequirements(MiniCommand command) {
    for (MiniSubsystem requirement : command.getRequirements()) {
      requirements.remove(requirement, command);
    }
  }
}
