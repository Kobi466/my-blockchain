---
name: notion-sync
description: Sync Blockchain learning notes, architecture diagrams, and progress reports directly to Notion using the local zero-dependency script.
---

# Notion Sync Skill

## Mission

Automatically record and synchronize Blockchain concepts, architectural decisions, code snippets, and learning milestones to the human's Notion workspace.

## Hard Rules

- Always read credentials safely from `.env` in the project root. Never hardcode tokens in repository source or commit `.env`.
- Do not spam the Notion page with minor edits; sync structured, polished summaries after completing a concept or milestone.
- Present the content structure to the human before writing if creating a major new subpage.

## Available Actions

1. **Verify Connection:**
   ```bash
   node scripts/notion-sync.js --test
   ```

2. **Sync Learning Note / Subpage:**
   Use the helper functions exported by `scripts/notion-sync.js` to create or append structured blocks (headings, callouts, code blocks, bullet points).

3. **Format Standards for Notes:**
   - **Header:** Concept name & Date.
   - **Problem & Mental Model:** Why this component exists and real-world analogy.
   - **Technical Invariants:** Rules that must never be broken (e.g. hash integrity, block link).
   - **Key Code Snippets:** Important Java methods implemented.
   - **Test & Evidence:** Unit test results proving correctness.
