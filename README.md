# Most Active Cookie CLI

A command-line application that analyzes a CSV cookie log file and returns the most active cookie(s) for a specific date.

---

## Overview

This application reads a CSV file of cookie usage logs and finds the cookie(s) that occurred most frequently on a user-specified date. It supports key-value flags for input and validates the file format, flag usage, and date format.

---

## Usage

Run the program using SBT:

```bash
sbt "run [OPTIONS]"
```

Example:
```bash
sbt "run -f cookie_log.csv -d 2025-06-09"
```

---
## Output

- the most active cookie for a specific day. (e.g. `AtY0laUfhglK3lC7`)
- If multiple cookies are tied for most active on the given date, all will be printed (one per line).
- ⚠️  If no cookies match the target date, an error message (NoCookiesOnDateError) is returned.
---
## Input Options
```
-h | --help        Show help message
-d                 Date in yyyy-mm-dd format (Required)
-f                 Path to CSV file (Required)
```
---
## Input Requirements

- The command-line tool **only supports key-value flags** (e.g., `-f file.csv`).

---

## CSV Format
- The CSV file **must contain a header row**:
  ```
  cookie,timestamp
  ```

- The CSV file must use **ISO 8601 timestamps with offset**, e.g.:
  ```
  AtY0laUfhglK3lC7,2025-06-10T14:19:00+00:00
  ```
  
Example
  ```csv
  cookie,timestamp
  AtY0laUfhglK3lC7,2025-06-08T14:00:00+00:00
  SAZuXPGUrfbcn5UA,2025-06-08T10:00:00+00:00
  AtY0laUfhglK3lC7,2025-06-08T16:00:00+00:00
  ```

For date `2025-06-08`, the output would be:
  ```
  AtY0laUfhglK3lC7
  ```

---

## Running Tests

To execute all unit tests:

```bash
sbt test
```

Tests cover:
- Flag parsing and validation
- CSV format validation
- Timestamp parsing
- Cookie frequency analysis
- Error cases (invalid input, missing file, malformed CSV, etc.)

---

## ⚠️ Error Handling Summary

| Error                   | Cause                                           |
|-------------------------|-------------------------------------------------| 
| `FileReadFailure`       | File could not be opened or read                |
| `EmptyCsvFileError`     | The file is empty                               |
| `MalformedCsvRowError`  | A line is missing expected columns              |
| `InvalidTimestampError` | Timestamp is not a valid ISO offset date        |
| `NoCookiesOnDateError`  | No cookies matched the specified date           |
| `InvalidFlagValue`      | Empty string, invalid date, File not found      |
| `MissingFlagValue`      | A flag is present without a corresponding value |
| `MissingFlags`          | The required flag(s) are missing                |
| `DuplicateFlag`         | The flag is present multiple times              |
| `UnrecognizedFlag`      | An Unrecognized flag is present                 |

