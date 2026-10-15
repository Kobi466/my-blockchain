#!/usr/bin/env node

/**
 * Notion Sync Utility for my-blockchain
 * Self-contained, zero-dependency script using native Node.js fetch (Node 18+).
 */

const fs = require('fs');
const path = require('path');

// 1. Load environment variables from .env in project root
const envPath = path.resolve(__dirname, '..', '.env');
if (fs.existsSync(envPath)) {
  const content = fs.readFileSync(envPath, 'utf8');
  content.split('\n').forEach(line => {
    const match = line.match(/^\s*([^#=\s]+)\s*=\s*(.*)$/);
    if (match) {
      process.env[match[1].trim()] = match[2].trim();
    }
  });
}

const NOTION_TOKEN = process.env.NOTION_TOKEN;
const NOTION_PAGE_ID = process.env.NOTION_PAGE_ID || '3d7b8c79-2404-80b2-a365-e920527d943b';
const NOTION_DAILY_REPORT_PAGE_ID = process.env.NOTION_DAILY_REPORT_PAGE_ID || '3d9b8c79-2404-8029-8687-e7301b0f1719';

if (!NOTION_TOKEN) {
  console.error('❌ Error: Missing NOTION_TOKEN in .env');
  process.exit(1);
}

const headers = {
  'Authorization': `Bearer ${NOTION_TOKEN}`,
  'Notion-Version': '2022-06-28',
  'Content-Type': 'application/json'
};

// 2. Notion Block Builder Helpers
function richText(text, { bold = false, italic = false, code = false, color = 'default' } = {}) {
  return {
    type: 'text',
    text: { content: String(text).slice(0, 2000) },
    annotations: { bold, italic, code, color }
  };
}

function paragraph(parts) {
  if (typeof parts === 'string') parts = [richText(parts)];
  return {
    object: 'block',
    type: 'paragraph',
    paragraph: { rich_text: parts }
  };
}

function heading1(text) {
  return {
    object: 'block',
    type: 'heading_1',
    heading_1: { rich_text: [richText(text, { bold: true })] }
  };
}

function heading2(text) {
  return {
    object: 'block',
    type: 'heading_2',
    heading_2: { rich_text: [richText(text, { bold: true })] }
  };
}

function heading3(text) {
  return {
    object: 'block',
    type: 'heading_3',
    heading_3: { rich_text: [richText(text, { bold: true })] }
  };
}

function callout(text, emoji = '💡') {
  return {
    object: 'block',
    type: 'callout',
    callout: {
      icon: { type: 'emoji', emoji },
      rich_text: [richText(text)]
    }
  };
}

function codeBlock(code, language = 'java') {
  return {
    object: 'block',
    type: 'code',
    code: {
      language,
      rich_text: [richText(code)]
    }
  };
}

function bullet(parts) {
  if (typeof parts === 'string') parts = [richText(parts)];
  return {
    object: 'block',
    type: 'bulleted_list_item',
    bulleted_list_item: { rich_text: parts }
  };
}

function divider() {
  return {
    object: 'block',
    type: 'divider',
    divider: {}
  };
}

// 3. Notion API Operations
async function testConnection() {
  try {
    const res = await fetch('https://api.notion.com/v1/users/me', { headers });
    const data = await res.json();
    if (res.ok) {
      console.log(`✅ Notion API Connected! Bot: "${data.name}" (${data.id})`);
      return true;
    } else {
      console.error('❌ Connection Failed:', data.message);
      return false;
    }
  } catch (err) {
    console.error('❌ Network error:', err.message);
    return false;
  }
}

async function appendBlocks(pageId, blocks) {
  for (let i = 0; i < blocks.length; i += 100) {
    const chunk = blocks.slice(i, i + 100);
    const res = await fetch(`https://api.notion.com/v1/blocks/${pageId}/children`, {
      method: 'PATCH',
      headers,
      body: JSON.stringify({ children: chunk })
    });
    if (!res.ok) {
      const err = await res.text();
      throw new Error(`Notion API Error (${res.status}): ${err}`);
    }
  }
}

async function createSubpage(parentPageId, title, emoji = '⛓️', initialBlocks = []) {
  const body = {
    parent: { page_id: parentPageId },
    icon: emoji ? { type: 'emoji', emoji } : undefined,
    properties: {
      title: [{ text: { content: title } }]
    },
    children: initialBlocks.slice(0, 100)
  };

  const res = await fetch('https://api.notion.com/v1/pages', {
    method: 'POST',
    headers,
    body: JSON.stringify(body)
  });

  if (!res.ok) {
    const err = await res.text();
    throw new Error(`Failed to create subpage "${title}": ${err}`);
  }

  const pageData = await res.json();
  if (initialBlocks.length > 100) {
    await appendBlocks(pageData.id, initialBlocks.slice(100));
  }

  console.log(`✅ Subpage created successfully: "${title}"`);
  console.log(`🔗 URL: ${pageData.url}`);
  return pageData;
}

// 4. CLI Execution
async function main() {
  const args = process.argv.slice(2);
  if (args.includes('--test')) {
    await testConnection();
    return;
  }

  console.log('Notion Sync CLI for my-blockchain');
  console.log('Usage:');
  console.log('  node scripts/notion-sync.js --test');
}

if (require.main === module) {
  main().catch(err => {
    console.error('Fatal:', err);
    process.exit(1);
  });
}

const MODULE_PAGES = {
  network: '3fab8c79-2404-814e-930f-e5833a43011f',
  core: '3fab8c79-2404-81a9-934f-f75f1206611e',
  consensus: '3fab8c79-2404-8161-9765-fc4e36c1f2e1',
  p2p: '3fab8c79-2404-8154-a26e-f87551092aa8',
  testing: '3fab8c79-2404-813e-b884-ee621ee63634',
  spec: '3fab8c79-2404-81dc-9bd9-d9d7c2c1306b'
};

module.exports = {
  testConnection,
  createSubpage,
  appendBlocks,
  richText,
  paragraph,
  heading1,
  heading2,
  heading3,
  callout,
  codeBlock,
  bullet,
  divider,
  NOTION_PAGE_ID,
  NOTION_DAILY_REPORT_PAGE_ID,
  MODULE_PAGES
};
  heading2,
  heading3,
  callout,
  codeBlock,
  bullet,
  divider,
  NOTION_PAGE_ID,
  NOTION_DAILY_REPORT_PAGE_ID
};
