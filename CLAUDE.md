## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Rules:
- When graphify-out/graph.json exists, run `graphify query "<question>"` before your first grep, search or source-file read for a codebase question, and put this rule in every subagent prompt that explores code. Read raw files only for lines the graph or an earlier read already located. Use `graphify path "<A>" "<B>"` for relationships and `graphify explain "<concept>"` for focused concepts. These return a scoped subgraph, usually much smaller than GRAPH_REPORT.md or raw grep output.
- If graphify-out/wiki/index.md exists, use it for broad navigation instead of raw source browsing.
- Read graphify-out/GRAPH_REPORT.md only for broad architecture review or when query/path/explain do not surface enough context.
- After modifying code, run `graphify update .` to keep the graph current (AST-only, no API cost).
- A committed graphify-out/graph.json is rewritten whole on every build, so a large diff to it in a pull request is expected. Do not flag it as churn or suggest skipping the graph rebuild or its hooks.

## Human-first Java development

- The human developer is the primary author of all project source code and manually types implementation code into the repository.
- You may explain concepts, inspect files, propose architecture, suggest code snippets in the conversation, design test cases, debug, and review code.
- By default, do not create, edit, overwrite, refactor, or patch application source files or tests. Show suggested code in the conversation for the developer to type and adapt.
- Do not modify build configuration, dependencies, or project settings unless the user explicitly approves that specific setup/change task.
- Before suggesting implementation, explain the purpose, data flow, design choices, edge cases, and important trade-offs at a level suitable for learning.
- Prefer small code suggestions over complete feature implementations. Explain the relevant fields, methods, and logic.
- For cryptographic operations, recommend established Java cryptographic APIs or vetted libraries; do not invent custom cryptographic primitives.
- Never claim that a build or test passed unless you actually ran it and observed the result. Distinguish verified facts from assumptions and recommendations.
- Preserve existing Graphify instructions and hooks. Do not replace or disable the existing Graphify configuration as part of ordinary project work.
