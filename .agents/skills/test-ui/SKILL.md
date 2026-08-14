---
name: test-ui
description: Record and run fail-fast console UI tests for this Java chatbot. Use when given commands and expected outputs to test, when asked to maintain test/ui-test-plan.md, or when asked to verify the application's terminal interaction and show a test-session transcript.
---

# Test UI

Record console test cases in `test/ui-test-plan.md`, execute them in order,
compare every command's response exactly, and show the resulting input/output
transcript.

## Maintain the test plan

1. Treat the repository root as the working directory.
2. Create or update `test/ui-test-plan.md` from the user's commands and
   expected outputs. Preserve unrelated existing cases unless the user asks to
   replace them.
3. Give every test case:
   - a unique `id`;
   - an `aim`;
   - an ordered `inputs` list;
   - a same-length `expected_outputs` list.
4. Store output from each command as an array of exact output lines. Omit the
   surrounding separator lines because the runner verifies those separately.
5. Keep project configuration and test cases in the JSON block marked
   `<!-- test-ui-plan -->`. Keep explanatory prose outside that block.

## Run the tests

1. Confirm Java 25 is active with `java -version`. On macOS, run
   `sdk use java 25.0.3.fx-zulu` if necessary.
2. From the repository root, run:

   ```bash
   python3 .agents/skills/test-ui/scripts/run_ui_tests.py test/ui-test-plan.md
   ```

   The runner compiles every Java source file into a temporary directory, so it
   does not create `.class` files in the repository.
3. Let the runner execute each test case as one live program session. It checks
   each command before sending the next command.
4. Stop immediately when the runner reports a failure. Report its expected and
   actual sections and the transcript produced up to that point. Do not run
   later cases or change application code unless the user asks.
5. After a successful run, report the complete console input/output transcript
   and the number of cases and commands that passed.

## Runner behavior

`scripts/run_ui_tests.py` uses only Python's standard library. It validates
the plan structure, compiles the project, starts a fresh Walter process for
each case, and uses the configured separator to identify one response per
command. A malformed plan, compilation error, timeout, premature process exit,
or output mismatch ends the run with a nonzero exit code.
