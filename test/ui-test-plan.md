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
          "Here are the current items on your list:",
          "1. [ ] read book",
          "2. [ ] return book",
          "3. [ ] buy bread"
        ],
        [
          "Walter has marked this task as done:",
          "[X] return book"
        ],
        [
          "Here are the current items on your list:",
          "1. [ ] read book",
          "2. [X] return book",
          "3. [ ] buy bread"
        ],
        [
          "Walter has marked this task as not done yet:",
          "[ ] return book"
        ],
        [
          "Here are the current items on your list:",
          "1. [ ] read book",
          "2. [ ] return book",
          "3. [ ] buy bread"
        ],
        ["Walter: Bye. Hope to see you again soon!"]
      ]
    }
  ]
}
```
