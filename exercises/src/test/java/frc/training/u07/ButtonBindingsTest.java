package frc.training.u07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ButtonBindingsTest {
  private final List<String> log = new ArrayList<>();
  private boolean pressed;
  private int loop;

  /** Logs schedule and cancel calls with the loop number they happened on. */
  private final class FakeTarget implements ButtonBindings.Target {
    private final String name;
    private boolean scheduled;

    FakeTarget(String name) {
      this.name = name;
    }

    @Override
    public void schedule() {
      scheduled = true;
      log.add(name + ".schedule@" + loop);
    }

    @Override
    public void cancel() {
      scheduled = false;
      log.add(name + ".cancel@" + loop);
    }

    @Override
    public boolean isScheduled() {
      return scheduled;
    }

    void finishOnItsOwn() {
      scheduled = false;
    }
  }

  /** Plays one button value per loop, polling once per loop. Loop numbers start at 1. */
  private void play(ButtonBindings bindings, boolean... values) {
    for (boolean value : values) {
      loop++;
      pressed = value;
      bindings.poll();
    }
  }

  @BeforeEach
  void reset() {
    log.clear();
    pressed = false;
    loop = 0;
  }

  @Test
  @DisplayName("onTrue schedules once per press, on the loop the button goes down")
  void onTrue() {
    FakeTarget shoot = new FakeTarget("shoot");
    ButtonBindings rightTrigger = new ButtonBindings(() -> pressed).onTrue(shoot);

    play(rightTrigger, false, true, true, true, false, true);

    assertEquals(List.of("shoot.schedule@2", "shoot.schedule@6"), log);
  }

  @Test
  @DisplayName("a button already held when the binding is created does not count as a press")
  void heldAtStartup() {
    pressed = true;
    FakeTarget shoot = new FakeTarget("shoot");
    ButtonBindings rightTrigger = new ButtonBindings(() -> pressed).onTrue(shoot);

    play(rightTrigger, true, true, false, true);

    assertEquals(List.of("shoot.schedule@4"), log);
  }

  @Test
  @DisplayName("onFalse schedules on the loop the condition becomes false")
  void onFalse() {
    FakeTarget stopRumble = new FakeTarget("stopRumble");
    ButtonBindings jammed = new ButtonBindings(() -> pressed).onFalse(stopRumble);

    play(jammed, false, true, true, false, false, true, false);

    assertEquals(List.of("stopRumble.schedule@4", "stopRumble.schedule@7"), log);
  }

  @Test
  @DisplayName("whileTrue schedules on press and cancels on release")
  void whileTrue() {
    FakeTarget eject = new FakeTarget("eject");
    ButtonBindings leftBumper = new ButtonBindings(() -> pressed).whileTrue(eject);

    play(leftBumper, true, true, false, false, true, false);

    assertEquals(List.of("eject.schedule@1", "eject.cancel@3", "eject.schedule@5", "eject.cancel@6"), log);
  }

  @Test
  @DisplayName("toggleOnTrue alternates schedule and cancel, and restarts a command that already finished")
  void toggleOnTrue() {
    FakeTarget intake = new FakeTarget("intake");
    ButtonBindings a = new ButtonBindings(() -> pressed).toggleOnTrue(intake);

    play(a, true, false, true, false, true);
    assertEquals(List.of("intake.schedule@1", "intake.cancel@3", "intake.schedule@5"), log);

    intake.finishOnItsOwn();
    play(a, false, true);
    assertEquals("intake.schedule@7", log.get(log.size() - 1));
  }

  @Test
  @DisplayName("bindings chain, like the rumble trigger's onTrue(...).onFalse(...)")
  void rumblePattern() {
    FakeTarget rumbleOn = new FakeTarget("rumbleOn");
    FakeTarget rumbleOff = new FakeTarget("rumbleOff");
    ButtonBindings rumble = new ButtonBindings(() -> pressed).onTrue(rumbleOn).onFalse(rumbleOff);

    play(rumble, true, true, false);

    assertEquals(List.of("rumbleOn.schedule@1", "rumbleOff.schedule@3"), log);
  }

  @Test
  @DisplayName("several bindings on one condition run in the order they were added")
  void bindingOrder() {
    FakeTarget first = new FakeTarget("first");
    FakeTarget second = new FakeTarget("second");
    ButtonBindings button = new ButtonBindings(() -> pressed).whileTrue(first).onTrue(second);

    play(button, true, false);

    assertEquals(List.of("first.schedule@1", "second.schedule@1", "first.cancel@2"), log);
  }

  @Test
  @DisplayName("a press that starts and ends between two polls is never seen")
  void tapBetweenLoops() {
    FakeTarget shoot = new FakeTarget("shoot");
    ButtonBindings rightTrigger = new ButtonBindings(() -> pressed).onTrue(shoot);

    pressed = true;
    pressed = false;
    rightTrigger.poll();

    assertTrue(log.isEmpty());
  }
}
