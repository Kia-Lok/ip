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
      "aim": "Verify adding Todos, listing, marking, unmarking, and exiting in one stateful session.",
      "inputs": [
        "todo read book",
        "todo return book",
        "todo buy bread",
        "list",
        "mark 2",
        "list",
        "unmark 2",
        "list",
        "bye"
      ],
      "expected_outputs": [
        [
          "Walter has added this task:",
          "[T][ ] read book",
          "Now you have 1 task in the list."
        ],
        [
          "Walter has added this task:",
          "[T][ ] return book",
          "Now you have 2 tasks in the list."
        ],
        [
          "Walter has added this task:",
          "[T][ ] buy bread",
          "Now you have 3 tasks in the list."
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][ ] read book",
          "2. [T][ ] return book",
          "3. [T][ ] buy bread"
        ],
        [
          "Walter has marked this task as done:",
          "[T][X] return book"
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][ ] read book",
          "2. [T][X] return book",
          "3. [T][ ] buy bread"
        ],
        [
          "Walter has marked this task as not done yet:",
          "[T][ ] return book"
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][ ] read book",
          "2. [T][ ] return book",
          "3. [T][ ] buy bread"
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
    },
    {
      "id": "unknown-and-blank-commands",
      "aim": "Verify blank input and command-prefix variants are rejected without terminating the session.",
      "inputs": [
        "",
        "   ",
        "blah",
        "hello there",
        "donee 1",
        "todoooo book",
        "listing",
        "byebye",
        "bye hello",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Command cannot be blank."],
        ["Command cannot be blank."],
        ["Unknown command."],
        ["Unknown command."],
        ["Unknown command."],
        ["Unknown command."],
        ["Unknown command."],
        ["Unknown command."],
        ["Unknown command."],
        ["There are currently no items on your list."],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "typed-command-errors",
      "aim": "Verify malformed Todo, Deadline, and Event commands produce specific errors and do not add tasks.",
      "inputs": [
        "todo",
        "todo    ",
        "deadline",
        "deadline return book",
        "deadline /by Sunday",
        "deadline return book /by",
        "deadline report /bypass Sunday",
        "event",
        "event meeting",
        "event /at Monday",
        "event meeting /at",
        "event meeting /atlas",
        "event meeting /from Monday",
        "event meeting /to 4pm",
        "event /from Monday /to 4pm",
        "event meeting /from /to 4pm",
        "event meeting /from Monday /to",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Todo description cannot be empty."],
        ["Todo description cannot be empty."],
        ["Deadline description cannot be empty."],
        ["Deadline requires /by."],
        ["Deadline description cannot be empty."],
        ["Deadline date/time cannot be empty."],
        ["Deadline requires /by."],
        ["Event description cannot be empty."],
        ["Event requires /at or /from and /to."],
        ["Event description cannot be empty."],
        ["Event date/time cannot be empty."],
        ["Event requires /at or /from and /to."],
        ["Event requires /to command when given /from command."],
        ["Event requires /from command when given /to command."],
        ["Event description cannot be empty."],
        ["Event start cannot be empty."],
        ["Event end cannot be empty."],
        ["There are currently no items on your list."],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "task-number-errors",
      "aim": "Verify task-number validation, whitespace handling, recovery, and state integrity.",
      "inputs": [
        "done 1",
        "todo read book",
        "done",
        "done abc",
        "done two",
        "done 1.5",
        "done !",
        "done 0",
        "done -1",
        "done 999",
        "done 1 2",
        "done 999999999999999999999999999999999999",
        "mark abc",
        "unmark 2",
        "done\t1",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Task number is out of range."],
        [
          "Walter has added this task:",
          "[T][ ] read book",
          "Now you have 1 task in the list."
        ],
        ["Task number is required."],
        ["Task number must be an integer."],
        ["Task number must be an integer."],
        ["Task number must be an integer."],
        ["Task number must be an integer."],
        ["Task number is out of range."],
        ["Task number is out of range."],
        ["Task number is out of range."],
        ["Task number must be an integer."],
        ["Task number must be an integer."],
        ["Task number must be an integer."],
        ["Task number is out of range."],
        [
          "Walter has marked this task as done:",
          "[T][X] read book"
        ],
        [
          "Here are the tasks in your list:",
          "1. [T][X] read book"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "recovery-after-errors",
      "aim": "Verify expected errors are caught centrally and later valid commands preserve correct state.",
      "inputs": [
        "todo",
        "todo read book",
        "deadline return book",
        "deadline return book /by Sunday",
        "done abc",
        "done 1",
        "blah",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Todo description cannot be empty."],
        [
          "Walter has added this task:",
          "[T][ ] read book",
          "Now you have 1 task in the list."
        ],
        ["Deadline requires /by."],
        [
          "Walter has added this task:",
          "[D][ ] return book (by: Sunday)",
          "Now you have 2 tasks in the list."
        ],
        ["Task number must be an integer."],
        [
          "Walter has marked this task as done:",
          "[T][X] read book"
        ],
        ["Unknown command."],
        [
          "Here are the tasks in your list:",
          "1. [T][X] read book",
          "2. [D][ ] return book (by: Sunday)"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "delete-lifecycle",
      "aim": "Verify deletion removes the selected polymorphic task, shifts numbering, and preserves other task states.",
      "inputs": [
        "todo read book",
        "deadline return book /by Sunday",
        "event project meeting /at Mon 2-4pm",
        "list",
        "done 2",
        "delete 1",
        "list",
        "delete 2",
        "list",
        "bye"
      ],
      "expected_outputs": [
        [
          "Walter has added this task:",
          "[T][ ] read book",
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
          "1. [T][ ] read book",
          "2. [D][ ] return book (by: Sunday)",
          "3. [E][ ] project meeting (at: Mon 2-4pm)"
        ],
        [
          "Walter has marked this task as done:",
          "[D][X] return book (by: Sunday)"
        ],
        [
          "Walter has removed this task:",
          "[T][ ] read book",
          "Now you have 2 tasks in the list."
        ],
        [
          "Here are the tasks in your list:",
          "1. [D][X] return book (by: Sunday)",
          "2. [E][ ] project meeting (at: Mon 2-4pm)"
        ],
        [
          "Walter has removed this task:",
          "[E][ ] project meeting (at: Mon 2-4pm)",
          "Now you have 1 task in the list."
        ],
        [
          "Here are the tasks in your list:",
          "1. [D][X] return book (by: Sunday)"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    },
    {
      "id": "delete-errors-preserve-state",
      "aim": "Verify malformed and out-of-range delete commands never crash or modify the task list.",
      "inputs": [
        "delete 1",
        "list",
        "todo keep me",
        "delete",
        "list",
        "delete      ",
        "list",
        "delete abc",
        "list",
        "delete two",
        "list",
        "delete 1.5",
        "list",
        "delete 0",
        "list",
        "delete -1",
        "list",
        "delete 999",
        "list",
        "delete 1 2",
        "list",
        "delete 999999999999999999999999999",
        "list",
        "deletee 1",
        "list",
        "deleted 1",
        "list",
        "deleteSomething 1",
        "list",
        "bye"
      ],
      "expected_outputs": [
        ["Task number is out of range."],
        ["There are currently no items on your list."],
        [
          "Walter has added this task:",
          "[T][ ] keep me",
          "Now you have 1 task in the list."
        ],
        ["Task number is required."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number is required."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number must be an integer."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number must be an integer."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number must be an integer."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number is out of range."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number is out of range."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number is out of range."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number must be an integer."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Task number must be an integer."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Unknown command."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Unknown command."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Unknown command."],
        ["Here are the tasks in your list:", "1. [T][ ] keep me"],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    }
  ]
}
```
