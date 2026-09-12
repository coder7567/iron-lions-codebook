package frc.training.u02;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MatchClockTest {
  @Test
  @DisplayName("autonomous is always AUTO, whatever the clock says")
  void autonomous() {
    assertEquals("AUTO", MatchClock.periodName(true, 15));
    assertEquals("AUTO", MatchClock.periodName(true, 120));
  }

  @ParameterizedTest(name = "teleop at {0} s is {1}")
  @CsvSource({
    "140, TRANSITION",
    "130.5, TRANSITION",
    "130, SHIFT 1",
    "120, SHIFT 1",
    "105, SHIFT 2",
    "80, SHIFT 3",
    "60, SHIFT 3",
    "55, SHIFT 4",
    "31, SHIFT 4",
    "30, END GAME",
    "0, END GAME",
    "-1, END GAME"
  })
  void teleopPeriods(double matchTime, String expected) {
    assertEquals(expected, MatchClock.periodName(false, matchTime));
  }

  @ParameterizedTest(name = "at {0} s the shift number is {1}")
  @CsvSource({"135, 0", "110, 1", "100, 2", "70, 3", "40, 4", "20, 0"})
  void shiftNumbers(double matchTime, int expected) {
    assertEquals(expected, MatchClock.shiftNumber(matchTime));
  }

  @ParameterizedTest(name = "at {0} s there are {1} s left in the period")
  @CsvSource({"140, 10", "120, 15", "105, 25", "81, 1", "30, 30", "12.5, 12.5", "-1, 0"})
  void secondsLeft(double matchTime, double expected) {
    assertEquals(expected, MatchClock.secondsLeftInPeriod(matchTime), 1e-9);
  }

  @ParameterizedTest(name = "warn at {0} s? {1}")
  @CsvSource({"133.5, false", "133, true", "132, true", "110, false", "108, true", "105, false", "31, true", "25, false"})
  void hubChangeWarning(double matchTime, boolean expected) {
    assertEquals(expected, MatchClock.warnHubChange(matchTime));
  }
}
