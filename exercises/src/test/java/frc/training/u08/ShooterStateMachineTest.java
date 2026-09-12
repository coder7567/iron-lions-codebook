package frc.training.u08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import frc.training.u08.ShooterStateMachine.CurrentState;
import frc.training.u08.ShooterStateMachine.Outputs;
import frc.training.u08.ShooterStateMachine.WantedState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShooterStateMachineTest {
  @Test
  @DisplayName("a new machine is IDLE and outputs nothing")
  void idle() {
    ShooterStateMachine shooter = new ShooterStateMachine();

    assertEquals(new Outputs(0.0, false), shooter.periodic(0.0));
    assertEquals(CurrentState.IDLE, shooter.getCurrentState());
  }

  @Test
  @DisplayName("SHOOT spins the flywheel up before the feeder runs")
  void spinsUpBeforeFeeding() {
    ShooterStateMachine shooter = new ShooterStateMachine();
    shooter.setWantedState(WantedState.SHOOT);

    assertEquals(new Outputs(2500.0, false), shooter.periodic(0.0));
    assertEquals(CurrentState.SPINNING_UP, shooter.getCurrentState());
  }

  @Test
  @DisplayName("READY needs the flywheel strictly less than 1000 RPM below the setpoint")
  void readyBoundary() {
    assertEquals(CurrentState.SPINNING_UP, ShooterStateMachine.updateState(WantedState.SHOOT, 1500.0));
    assertEquals(CurrentState.READY, ShooterStateMachine.updateState(WantedState.SHOOT, 1500.5));
    assertEquals(CurrentState.READY, ShooterStateMachine.updateState(WantedState.SHOOT, 3200.0));
    assertEquals(CurrentState.IDLE, ShooterStateMachine.updateState(WantedState.IDLE, 3200.0));
  }

  @Test
  @DisplayName("the feeder stops again if the flywheel slows down")
  void dropsBackWhenSpeedSags() {
    ShooterStateMachine shooter = new ShooterStateMachine();
    shooter.setWantedState(WantedState.SHOOT);

    assertEquals(new Outputs(2500.0, true), shooter.periodic(2400.0));
    assertEquals(new Outputs(2500.0, false), shooter.periodic(1200.0));
    assertEquals(CurrentState.SPINNING_UP, shooter.getCurrentState());
  }

  @Test
  @DisplayName("switching to IDLE stops everything on the next loop, even at full speed")
  void idleStopsImmediately() {
    ShooterStateMachine shooter = new ShooterStateMachine();
    shooter.setWantedState(WantedState.SHOOT);
    shooter.periodic(2500.0);

    shooter.setWantedState(WantedState.IDLE);

    assertEquals(new Outputs(0.0, false), shooter.periodic(2500.0));
  }

  @Test
  @DisplayName("every current state has outputs")
  void everyStateHasOutputs() {
    for (CurrentState state : CurrentState.values()) {
      assertNotNull(ShooterStateMachine.applyState(state), state.name());
    }
  }
}
