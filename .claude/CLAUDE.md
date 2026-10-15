# graphify
- **graphify** (`.claude/skills/graphify/SKILL.md`) - any input to knowledge graph. Trigger: `/graphify`
When the user types `/graphify`, use the installed graphify skill or instructions before doing anything else.

# project skills
- **blockchain-learning** (`.claude/skills/blockchain-learning/SKILL.md`) - teach Blockchain concepts and guide the human through small, manual implementation steps. Use for learning, implementation planning, and connecting concepts to this Mini Blockchain project.
- **blockchain-reviewer** (`.claude/agents/blockchain-reviewer.md`) - read-only independent reviewer for correctness, security, edge cases, and test coverage. Use after the human has implemented a step and asks for a review.
- **testing-review** (`.claude/skills/testing-review/SKILL.md`) - design and evaluate test cases, expected outcomes, edge cases, and test evidence without writing application tests.
- **product-report** (`.claude/skills/product-report/SKILL.md`) - maintain evidence-based progress summaries, backlog status, acceptance criteria, and learning outcomes when requested.
- **notion-sync** (`.claude/skills/notion-sync/SKILL.md`) - sync Blockchain learning notes, architecture diagrams, and progress reports directly to Notion using the local zero-dependency script.
