# Walter User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details


## Feature XYZ

// Feature details

## Managing places

Walter can save useful place names and addresses separately from tasks.

- Add a place: `place <name> /at <address>`
- List saved places: `places`
- Delete a place using its number from the list: `deleteplace <index>`

For example:

```text
place Alex's home /at 123 Clementi Ave 3
places
deleteplace 1
```

Place names and addresses must both be non-empty, and an add command must contain exactly one
`/at` separator. Saved places remain available when Walter is restarted.
