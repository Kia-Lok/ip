# UI Test Plan

The `test-ui` skill maintains and runs this plan. Each test case records its
aim, ordered console inputs, and the exact response expected after each input.
Expected responses omit the surrounding separator lines.

<!-- test-ui-plan -->
```json
{
  "source_directory": "src/main/java",
  "main_class": "Walter",
  "separator": "____________________________________________________________",
  "response_timeout_seconds": 5,
  "test_cases": [
    {
      "id": "task-status-lifecycle",
      "aim": "Verify adding, listing, marking, unmarking, and exiting in one stateful session.",
      "inputs": [
        "read book",
        "return book",
        "buy bread",
        "list",
        "mark 2",
        "list",
        "unmark 2",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Walter has added read book to the list."],
        ["Walter has added return book to the list."],
        ["Walter has added buy bread to the list."],
        [
          "Here are the tasks in your list:",
          "1. [ ] read book",
          "2. [ ] return book",
          "3. [ ] buy bread"
        ],
        [
          "Walter has marked this task as done:",
          "[X] return book"
        ],
        [
          "Here are the tasks in your list:",
          "1. [ ] read book",
          "2. [X] return book",
          "3. [ ] buy bread"
        ],
        [
          "Walter has marked this task as not done yet:",
          "[ ] return book"
        ],
        [
          "Here are the tasks in your list:",
          "1. [ ] read book",
          "2. [ ] return book",
          "3. [ ] buy bread"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "typed-task-lifecycle",
      "aim": "Verify Todo, Deadline, and Event creation, polymorphic listing, and the done command.",
      "inputs": [
        "todo borrow book",
        "deadline return book /by Sunday",
        "event project meeting /at Mon 2-4pm",
        "list",
        "done 2",
        "list",
        "bye"
      ],
      "expected_outputs": [
        [
          "Walter has added this task:",
          "[T][ ] borrow book",
          "Now you have 1 task in the list."
        ],
        [
          "Walter has added this task:",
          "[D][ ] return book (by: Sunday)",
          "Now you have 2 tasks in the list."
        ],
        [
          "Walter has added this task:",
          "[E][ ] project meeting (at: Mon 2-4pm)",
          "Now you have 3 tasks in the list."
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][ ] borrow book",
          "2. [D][ ] return book (by: Sunday)",
          "3. [E][ ] project meeting (at: Mon 2-4pm)"
        ],
        [
          "Walter has marked this task as done:",
          "[D][X] return book (by: Sunday)"
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][ ] borrow book",
          "2. [D][X] return book (by: Sunday)",
          "3. [E][ ] project meeting (at: Mon 2-4pm)"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "event-from-to",
      "aim": "Verify an event with /from and /to is parsed, stored, and listed without crashing.",
      "inputs": [
        "event project meeting /from Mon 2pm /to 4pm",
        "list",
        "bye"
      ],
      "expected_outputs": [
        [
          "Walter has added this task:",
          "[E][ ] project meeting (from: Mon 2pm to: 4pm)",
          "Now you have 1 task in the list."
        ],
        [
          "Here are the tasks in your list:",
          "1. [E][ ] project meeting (from: Mon 2pm to: 4pm)"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    }
  ]
}
```
