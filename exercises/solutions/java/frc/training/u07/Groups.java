package frc.training.u07;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

/** Reference solution for exercise u07-groups. */
public final class Groups {
  private Groups() {}

  public static MiniCommand sequence(MiniCommand... commands) {
    List<MiniCommand> members = List.of(commands);
    Set<MiniSubsystem> requirements = unionOf(members, false);
    return new MiniCommand() {
      private int index = 0;

      @Override
      public void initialize() {
        index = 0;
        if (!members.isEmpty()) {
          members.get(0).initialize();
        }
      }

      @Override
      public void execute() {
        if (index >= members.size()) {
          return;
        }
        MiniCommand current = members.get(index);
        current.execute();
        if (current.isFinished()) {
          current.end(false);
          index++;
          if (index < members.size()) {
            members.get(index).initialize();
          }
        }
      }

      @Override
      public boolean isFinished() {
        return index >= members.size();
      }

      @Override
      public void end(boolean interrupted) {
        if (interrupted && index < members.size()) {
          members.get(index).end(true);
        }
      }

      @Override
      public Set<MiniSubsystem> getRequirements() {
        return requirements;
      }
    };
  }

  public static MiniCommand parallel(MiniCommand... commands) {
    List<MiniCommand> members = List.of(commands);
    Set<MiniSubsystem> requirements = unionOf(members, true);
    return new MiniCommand() {
      private final Map<MiniCommand, Boolean> running = new LinkedHashMap<>();

      @Override
      public void initialize() {
        running.clear();
        for (MiniCommand member : members) {
          member.initialize();
          running.put(member, true);
        }
      }

      @Override
      public void execute() {
        for (Map.Entry<MiniCommand, Boolean> entry : running.entrySet()) {
          if (!entry.getValue()) {
            continue;
          }
          MiniCommand member = entry.getKey();
          member.execute();
          if (member.isFinished()) {
            member.end(false);
            entry.setValue(false);
          }
        }
      }

      @Override
      public boolean isFinished() {
        return !running.containsValue(true);
      }

      @Override
      public void end(boolean interrupted) {
        if (interrupted) {
          for (Map.Entry<MiniCommand, Boolean> entry : running.entrySet()) {
            if (entry.getValue()) {
              entry.getKey().end(true);
            }
          }
        }
      }

      @Override
      public Set<MiniSubsystem> getRequirements() {
        return requirements;
      }
    };
  }

  public static MiniCommand race(MiniCommand... commands) {
    List<MiniCommand> members = List.of(commands);
    Set<MiniSubsystem> requirements = unionOf(members, true);
    return new MiniCommand() {
      private boolean finished = false;

      @Override
      public void initialize() {
        finished = false;
        for (MiniCommand member : members) {
          member.initialize();
        }
      }

      @Override
      public void execute() {
        for (MiniCommand member : members) {
          member.execute();
          if (member.isFinished()) {
            finished = true;
          }
        }
      }

      @Override
      public boolean isFinished() {
        return finished;
      }

      @Override
      public void end(boolean interrupted) {
        for (MiniCommand member : members) {
          member.end(!member.isFinished());
        }
      }

      @Override
      public Set<MiniSubsystem> getRequirements() {
        return requirements;
      }
    };
  }

  public static MiniCommand deadline(MiniCommand deadline, MiniCommand... others) {
    List<MiniCommand> members = new ArrayList<>();
    members.add(deadline);
    members.addAll(List.of(others));
    Set<MiniSubsystem> requirements = unionOf(members, true);
    return new MiniCommand() {
      private final Map<MiniCommand, Boolean> running = new LinkedHashMap<>();
      private boolean finished = false;

      @Override
      public void initialize() {
        running.clear();
        finished = false;
        for (MiniCommand member : members) {
          member.initialize();
          running.put(member, true);
        }
      }

      @Override
      public void execute() {
        for (Map.Entry<MiniCommand, Boolean> entry : running.entrySet()) {
          if (!entry.getValue()) {
            continue;
          }
          MiniCommand member = entry.getKey();
          member.execute();
          if (member.isFinished()) {
            member.end(false);
            entry.setValue(false);
            if (member == deadline) {
              finished = true;
            }
          }
        }
      }

      @Override
      public boolean isFinished() {
        return finished;
      }

      @Override
      public void end(boolean interrupted) {
        for (Map.Entry<MiniCommand, Boolean> entry : running.entrySet()) {
          if (entry.getValue()) {
            entry.getKey().end(true);
          }
        }
      }

      @Override
      public Set<MiniSubsystem> getRequirements() {
        return requirements;
      }
    };
  }

  public static MiniCommand withTimeout(MiniCommand command, int loops) {
    return race(command, waitLoops(loops));
  }

  public static MiniCommand until(MiniCommand command, BooleanSupplier condition) {
    return race(command, waitUntil(condition));
  }

  private static MiniCommand waitLoops(int loops) {
    return new MiniCommand() {
      private int count = 0;

      @Override
      public void initialize() {
        count = 0;
      }

      @Override
      public void execute() {
        count++;
      }

      @Override
      public boolean isFinished() {
        return count >= loops;
      }
    };
  }

  private static MiniCommand waitUntil(BooleanSupplier condition) {
    return new MiniCommand() {
      @Override
      public boolean isFinished() {
        return condition.getAsBoolean();
      }
    };
  }

  private static Set<MiniSubsystem> unionOf(List<MiniCommand> members, boolean mustBeDisjoint) {
    Set<MiniSubsystem> all = new HashSet<>();
    for (MiniCommand member : members) {
      for (MiniSubsystem requirement : member.getRequirements()) {
        if (!all.add(requirement) && mustBeDisjoint) {
          throw new IllegalArgumentException(
              "Two commands in this group both require " + requirement.getName());
        }
      }
    }
    return Set.copyOf(all);
  }
}
