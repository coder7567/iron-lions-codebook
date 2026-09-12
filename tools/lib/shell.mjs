// HTML shell for the single-page app. `artifact: true` emits a body fragment (no doctype/head/body)
// because the claude.ai Artifact host wraps the page in its own document skeleton.
import { escapeHtml } from './highlight.mjs';

const FONT_LINKS = [
  'https://fonts.googleapis.com/css2?family=Big+Shoulders+Display:wght@700;800&display=swap',
  'https://fonts.googleapis.com/css2?family=Atkinson+Hyperlegible+Next:ital,wght@0,400;0,700;1,400&display=swap',
  'https://fonts.googleapis.com/css2?family=Atkinson+Hyperlegible+Mono:wght@400;700&display=swap',
]
  .map((href) => `<link rel="stylesheet" href="${href}">`)
  .join('\n');

const ICONS = {
  menu: '<svg aria-hidden="true" viewBox="0 0 24 24" width="22" height="22"><path d="M4 6h16M4 12h16M4 18h16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  search: '<svg aria-hidden="true" viewBox="0 0 24 24" width="18" height="18"><circle cx="10.5" cy="10.5" r="6.5" fill="none" stroke="currentColor" stroke-width="2"/><path d="m15.5 15.5 5 5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  theme: '<svg aria-hidden="true" viewBox="0 0 24 24" width="20" height="20"><circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" stroke-width="2"/><path d="M12 4a8 8 0 0 1 0 16z" fill="currentColor"/></svg>',
};

function body({ title }) {
  return `<a class="skip-link" href="#main">Skip to content</a>
<header class="topbar">
  <button class="icon-btn nav-open" type="button" aria-controls="nav-dialog" aria-expanded="false" aria-label="Course menu">${ICONS.menu}</button>
  <a class="brand" href="#/" aria-label="${escapeHtml(title)} home">
    <span class="brand-mark" aria-hidden="true">967</span>
    <span class="brand-name">Iron Lions <span class="brand-strong">Codebook</span></span>
  </a>
  <button class="search-open" type="button" aria-haspopup="dialog" aria-controls="search-dialog">${ICONS.search}<span class="search-open-label">Search lessons, code, terms</span><kbd class="search-kbd">Ctrl K</kbd></button>
  <div class="topbar-end">
    <a class="progress-pill" id="progress-pill" href="#/progress">0% complete</a>
    <button class="icon-btn theme-toggle" type="button" aria-pressed="false" hidden>${ICONS.theme}<span class="visually-hidden theme-toggle-label">Switch theme</span></button>
  </div>
</header>
<div class="layout">
  <nav class="sidebar" id="sidebar" aria-label="Course"></nav>
  <main class="main" id="main" tabindex="-1">
    <div class="view" id="view"><p class="loading">Loading the Codebook…</p></div>
    <noscript><p class="loading">The Codebook needs JavaScript for navigation, quizzes, and progress tracking.</p></noscript>
  </main>
  <aside class="toc" id="toc" aria-label="On this page"></aside>
</div>
<dialog class="nav-dialog" id="nav-dialog" aria-label="Course menu" closedby="any">
  <div class="nav-dialog-head"><span class="nav-dialog-title">Course menu</span><button class="btn btn-quiet nav-close" type="button">Close</button></div>
  <div class="nav-dialog-body"></div>
</dialog>
<dialog class="search-dialog" id="search-dialog" aria-labelledby="search-label" closedby="any">
  <div class="search-head">
    ${ICONS.search}
    <label class="visually-hidden" id="search-label" for="search-input">Search the course</label>
    <input class="search-input" id="search-input" type="search" placeholder="Try “odometry”, “CAN ID”, or “Superstructure”" autocomplete="off" spellcheck="false" enterkeyhint="search">
    <button class="btn btn-quiet search-close" type="button">Close</button>
  </div>
  <p class="search-status" id="search-status" aria-live="polite"></p>
  <ol class="search-results" id="search-results"></ol>
</dialog>
<div class="toast" id="toast" role="status" aria-live="polite" hidden></div>`;
}

const scripts = (query) =>
  ['assets/course-index.js', 'assets/app.js', 'assets/quiz.js', 'assets/extras.js']
    .map((src) => `<script src="${src}${query}"></script>`)
    .join('\n');

export function renderShell({ title, description, builtAt, sha, artifact, version }) {
  const meta = `<!-- Built ${builtAt} from Rebuilt-2026@${sha.slice(0, 7)} -->`;
  // Local copies get a per-build query so browsers never mix old and new files after a rebuild.
  // Artifact files are served by exact published path, so they get no query.
  const q = artifact || !version ? '' : `?v=${encodeURIComponent(version)}`;
  const SCRIPTS = scripts(q);
  if (artifact) {
    return `<title>${escapeHtml(title)}</title>
${meta}
${FONT_LINKS}
<link rel="stylesheet" href="assets/app.css${q}">
<script>window.__CODEBOOK_ARTIFACT__ = true;</script>
${body({ title })}
${SCRIPTS}
`;
  }
  return `<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="color-scheme" content="light dark">
<title>${escapeHtml(title)}</title>
<meta name="description" content="${escapeHtml(description)}">
${meta}
<script>
try {
  var pinned = localStorage.getItem('codebook:theme');
  if (pinned === 'light' || pinned === 'dark') {
    document.documentElement.setAttribute('data-theme', pinned);
    document.querySelector('meta[name="color-scheme"]').content = pinned;
  }
} catch (e) {}
</script>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
${FONT_LINKS}
<link rel="stylesheet" href="assets/app.css${q}">
</head>
<body>
${body({ title })}
${SCRIPTS}
</body>
</html>
`;
}
