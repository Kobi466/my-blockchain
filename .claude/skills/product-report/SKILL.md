---
name: product-report
description: Produce evidence-based Mini Blockchain progress reports, backlog updates, acceptance criteria, and learning summaries. Use at milestones or when the human asks for project status; do not change source or project files unless explicitly authorized.
---

# Product and Progress Reporting Skill

## Mission

Help the human manage the Mini Blockchain as both a software project and a structured learning journey.

## Hard rules

- Default to reporting in the conversation; do not create or modify project documents, source, tests, or configuration unless the human explicitly approves that specific write task.
- Never commit, push, merge, or change branches without an explicit request.
- Distinguish implemented and verified behavior from planned work, assumptions, and recommendations.
- Do not mark a feature complete just because files exist or compilation succeeds.
- Preserve all Graphify instructions, files, and hooks.

## Reporting dimensions

Track these separately:

1. **Product status:** feature, scope, acceptance criteria, blockers, and next action.
2. **Implementation evidence:** relevant files/commits and observed build/test results.
3. **Learning progress:** concepts the human can explain, concepts still unclear, and exercises completed.
4. **Quality:** review findings, unresolved risks, missing tests, and technical debt.
5. **Git state:** current branch, working tree state, and commit status when actually inspected.

## Suggested feature states

- `Planned`: not started.
- `Learning/Research`: concepts or references are being studied.
- `Designed`: responsibilities, data flow, invariants, and acceptance criteria have been agreed.
- `Implementing`: the human is manually coding.
- `Testing`: test design or execution is in progress.
- `Review`: implementation is awaiting or undergoing independent review.
- `Done`: acceptance criteria are satisfied, relevant tests were executed with observed results, review findings are addressed or explicitly accepted, and the human can explain the implementation.
- `Blocked`: progress requires a decision, dependency, or unresolved issue.

## Report format

When useful, provide:

- Summary
- Completed (with evidence)
- In progress
- Blockers and unresolved risks
- Learning outcomes and knowledge gaps
- Test/review evidence
- Next smallest actionable step

Keep reports concise and factual. Do not invent dates, results, completion percentages, or evidence. If the repository or previous progress data was not inspected, say so rather than reconstructing details from guesses.
