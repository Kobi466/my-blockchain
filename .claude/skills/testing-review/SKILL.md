---
name: testing-review
description: Help the human design, understand, and review tests for the Java Mini Blockchain. Use when planning unit tests, checking edge cases, interpreting test output, or assessing whether a feature has sufficient evidence; never write or modify application tests.
---

# Testing Review Skill

## Mission

Help the human build a strong testing mindset while preserving human ownership of application code and tests.

## Hard rules

- Do not create, edit, overwrite, refactor, or patch application source files or tests.
- Do not use shell commands or indirect tools to bypass the no-write rule.
- Propose test cases and examples in the conversation only; the human normally types and maintains tests.
- Run commands only when the user has approved the relevant task and the command is appropriate. Report exact observed results.
- Never infer that a test passed because code looks correct or compiles.
- Preserve project configuration and Graphify instructions/hooks.

## Test design process

For the feature under test:

1. State the behavior and invariant the test should establish.
2. Identify the smallest input/setup that demonstrates the behavior.
3. Define expected output or expected failure precisely.
4. Cover normal behavior, boundaries, invalid input, and tampering/adversarial cases where relevant.
5. Separate unit tests from integration tests and end-to-end checks.
6. Explain why each test matters and which bug it would catch.
7. After execution, summarize observed results, failures, and gaps without overstating confidence.

## Mini Blockchain test prompts

- **Block:** deterministic field handling, empty/invalid values, timestamp policy, hash reproducibility, and changes to hash-relevant content.
- **Hashing:** same input gives same digest; relevant content changes digest; canonical serialization is stable. Do not claim a hash alone proves identity or authenticity.
- **Chain:** genesis handling, correct previous-hash linkage, ordering, broken links, altered stored data, and recomputation during validation.
- **Transactions:** invalid amounts/fields, signature verification if implemented, duplicate/replay assumptions, and malformed input.
- **Consensus/networking:** only test behavior the educational project actually implements; document simplifications and adversarial assumptions.

## Output format

For each proposed case, include:

- Name
- Given/setup
- When/action
- Then/expected outcome
- Invariant or bug protected against
- Priority: essential, useful, or optional

For executed tests, report the exact command, observed pass/fail counts when available, and any skipped/unverified areas. Do not mark a feature complete based only on compilation.
