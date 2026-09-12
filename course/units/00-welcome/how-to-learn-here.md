---
summary: Habits that make you a strong programmer fast, how to use AI tools without cheating yourself, and the safety rules every 967 programmer follows around a live robot.
objectives:
  - Use predict-run-explain and the 20-minute rule when learning and debugging
  - Use AI assistants in ways that build your skill instead of replacing it
  - Follow the programmer safety rules before enabling a robot
---

## Learn like an engineer

Programming is learned by doing, not by reading. These habits separate students who become the team's go-to programmer from students who stay stuck:

1. **Type examples, do not paste them.** Your fingers learn syntax your eyes skim past.
2. **Predict, then run, then explain.** Before running code, say what you think will happen. If you are wrong, that surprise is exactly where the learning is.
3. **Read error messages from the top.** Java tells you the file, the line number, and usually what it expected. The first error often causes the rest.
4. **Change one thing at a time.** If you change five things and it works, you do not know which one fixed it.
5. **Explain it out loud.** Describing your code line by line to a teammate, or a rubber duck, finds a surprising number of bugs.
6. **Keep a lab notebook.** Write down what you tried, what happened, and the values you tuned. The team's commit history shows what happens without notes: messages like *"put values in idk what for."*

:::tip The 20-minute rule
If you have been stuck for 20 minutes without new progress, ask for help. Before you ask, write down: what you expected, what actually happened, and what you already tried. Half the time, writing that down solves it.
:::

## Using AI assistants honestly

Tools like Claude and other coding assistants can explain code, suggest fixes, and write examples. Used well, they make you learn faster. Used badly, you end up on a robot team knowing nothing when the laptop is closed and the robot won't drive.

**Good uses**
- "Explain what this line of `Drive.java` does."
- "Why does this error say *cannot find symbol*?"
- "Give me a hint for this exercise without writing the solution."
- "Quiz me on command-based vocabulary."

**Uses that cheat you**
- Pasting an exercise prompt and submitting the answer. The tests pass, but you learned nothing, and the unit test will show it.
- Deploying code you do not understand to a real robot.

:::warning You are responsible for every line you deploy
Whether a line came from a mentor, Chief Delphi, another team's GitHub, or an AI, you must understand it and test it before it runs on a robot. AI tools confidently invent methods that do not exist, especially for fast-changing libraries like REVLib and PathPlanner. Check the official documentation.
:::

Competition rules also cover software. Code written before kickoff generally has to be publicly available to be used on a competition robot. Read the current game manual's rules on pre-existing designs and code each season, or ask a mentor.

## Safety first: programmers move robots

A 74 kg robot that moves when nobody expects it is dangerous. Programmers are the people who make robots move, so these rules are ours.

:::danger Before you enable
- **Safety glasses** on for everyone near the robot.
- **Say "ENABLING" loudly** and wait until everyone's hands are clear.
- **Know your stops.** In the FRC Driver Station, **Enter** disables the robot and the **space bar** triggers an emergency stop. An e-stop requires rebooting the robot, so use it when something is actually wrong.
- **First run of new code?** Put the robot on blocks or a cart so the wheels are off the ground, or test in simulation first.
- **Keep someone at the laptop.** Never walk away from an enabled robot, not even for a moment.
:::

More rules that keep robots and people intact:

- **Mechanisms first, slowly.** Test a new mechanism at low output (a few volts) before full power, and watch it through the whole range of motion.
- **Know the hard stops.** A turret, arm, or hood driven past its limits breaks things. Our code sets soft limits and clamps, but only if the numbers are right.
- **Power down before touching.** Disable and turn off the main breaker before hands go into a mechanism. A disabled robot can still have stored energy in springs or raised arms.
- **Batteries are heavy and powerful.** Carry them with two hands, never short the terminals, and report damaged ones.
- **Only deploy code you can undo.** Commit before you deploy, so you can get back to a known-good version in seconds.

:::team In our code
`Robot.autonomousInit()` raises the drive motors' current limit from 30 A to 80 A for AUTO, and `teleopInit()` lowers it again. Settings like that change how hard the robot can push and accelerate. Programmers make safety-relevant choices even when it does not feel like it.
:::

## Be the teammate people want

Linn-Mar Robotics describes its values as encouraging teamwork and innovation and making a welcoming place where creativity thrives. On the software team that looks like:

- Explaining your code so others can maintain it after you graduate.
- Reviewing teammates' code kindly and specifically.
- Writing commit messages that describe what changed.
- Helping newer students the way someone helped you.

:::quiz
? You have been stuck on a compile error for 25 minutes. What should you do?
- Keep trying random changes until it compiles
- Delete the file and start over
+ Write down what you expected, what happened, and what you tried, then ask a mentor or teammate
- Paste the whole project into an AI tool and deploy whatever it returns
> The 20-minute rule. Writing the problem down often reveals the answer, and it makes your question quick for others to answer.

?tf In the FRC Driver Station, pressing the space bar triggers an emergency stop that requires rebooting the robot.
= true
> Space is e-stop and Enter is disable. Use Enter for routine stops and space when something is actually going wrong.

?? Which of these are good ways to use an AI assistant while learning? (Select all that apply.)
+ Asking it to explain a line of team code you do not understand
+ Asking for a hint on an exercise without the full solution
- Submitting its complete answer to an exercise without reading it
+ Asking it to quiz you on vocabulary
- Deploying its suggested code to the robot without testing it
> Use AI to understand and practice. Never let it replace your own understanding, and never deploy untested code.

? You wrote new code for the intake arm. What is the safest first test?
- Enable at full power with the robot on the floor so it behaves realistically
+ Test at low output with the robot on blocks or in simulation, watching the full range of motion
- Deploy it during a practice match so the drive team can try it
- Skip testing because the code compiled
> Compiling only means the syntax is valid. Start slow and controlled, then work up to full power.
:::
