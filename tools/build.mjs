#!/usr/bin/env node
// Builds the Iron Lions Codebook into site/.  Usage:  node tools/build.mjs [--strict]
//   --strict  fail on missing lessons, broken links, or missing exercise files (use before publishing)
import { readFileSync, writeFileSync, mkdirSync, existsSync, readdirSync, statSync, copyFileSync } from 'node:fs';
import { join, dirname, resolve, basename, relative, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { renderDocument, codeBlock, stripTags } from './lib/markdown.mjs';
import { escapeHtml } from './lib/highlight.mjs';
import { renderShell } from './lib/shell.mjs';
import { makeZip } from './lib/zip.mjs';

const ROOT = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const COURSE = join(ROOT, 'course');
const SITE = join(ROOT, 'site');
const EXERCISES = join(ROOT, 'exercises');
const STRICT = process.argv.includes('--strict');

const read = (file) => readFileSync(file, 'utf8');
const write = (file, text) => {
  mkdirSync(dirname(file), { recursive: true });
  writeFileSync(file, text);
};
const safeJson = (value) =>
  JSON.stringify(value).replace(/\u2028/g, '\\u2028').replace(/\u2029/g, '\\u2029').replace(/<\/script/gi, '<\\/script');

const manifest = JSON.parse(read(join(COURSE, 'course.json')));
const REPO_DIR = resolve(ROOT, manifest.repo.localPath);
const catalog = existsSync(join(COURSE, 'exercises.json')) ? JSON.parse(read(join(COURSE, 'exercises.json'))) : [];
const exerciseById = new Map(catalog.map((e) => [e.id, e]));
const problems = [];
const warnings = [];
const problem = (msg) => (STRICT ? problems : warnings).push(msg);

// ---------- front matter ----------
function splitFrontMatter(text, where) {
  const m = /^---\r?\n([\s\S]*?)\r?\n---\r?\n?/.exec(text);
  if (!m) return { data: {}, body: text };
  const data = {};
  let listKey = null;
  for (const raw of m[1].split(/\r?\n/)) {
    if (!raw.trim()) continue;
    const item = /^\s+-\s+(.*)$/.exec(raw);
    if (item && listKey) {
      data[listKey].push(item[1].trim());
      continue;
    }
    const kv = /^([A-Za-z][\w-]*):\s*(.*)$/.exec(raw);
    if (!kv) throw new Error(`${where}: bad front matter line "${raw}"`);
    if (kv[2] === '') {
      listKey = kv[1];
      data[listKey] = [];
    } else {
      listKey = null;
      data[kv[1]] = /^\d+(\.\d+)?$/.test(kv[2]) ? Number(kv[2]) : kv[2].replace(/^"(.*)"$/, '$1');
    }
  }
  return { data, body: text.slice(m[0].length) };
}

// ---------- pages ----------
const pages = [];
for (const unit of manifest.units) {
  for (const lesson of unit.lessons) {
    pages.push({ unit, slug: lesson.slug, kind: 'lesson', title: lesson.title, minutes: lesson.minutes, file: join(COURSE, 'units', unit.slug, `${lesson.slug}.md`) });
  }
  if (unit.unitTest) {
    pages.push({ unit, slug: 'unit-test', kind: 'unit-test', title: `Unit ${unit.number} Test`, minutes: 30, file: join(COURSE, 'units', unit.slug, 'unit-test.md') });
  }
}
for (const ref of manifest.reference) {
  pages.push({ unit: null, slug: ref.slug, kind: 'reference', title: ref.title, file: join(COURSE, 'reference', `${ref.slug}.md`) });
}
const routeOf = (p) => (p.unit ? `${p.unit.slug}/${p.slug}` : `reference/${p.slug}`);
const knownRoutes = new Set(pages.map(routeOf));

// ---------- directive handlers ----------
function resolveLink(href, where) {
  if (href.startsWith('course:')) {
    const [route, anchor] = href.slice('course:'.length).split('#');
    if (!knownRoutes.has(route)) problem(`${where}: broken course link ${href}`);
    return { href: `#/${route}${anchor ? `?h=${anchor}` : ''}` };
  }
  if (href.startsWith('repo:')) {
    const [path, lines] = href.slice('repo:'.length).split('#');
    if (!existsSync(join(REPO_DIR, path))) problem(`${where}: repo link to missing file ${path}`);
    return { href: `${manifest.repo.url}/blob/${manifest.repo.sha}/${encodeURI(path)}${lines ? `#${lines}` : ''}`, cls: 'repo-link' };
  }
  if (href.startsWith('ex:')) {
    const id = href.slice(3);
    if (!exerciseById.has(id)) problem(`${where}: link to unknown exercise ${id}`);
    return { href: `#/reference/exercise-catalog?h=ex-${id}` };
  }
  return { href };
}

const repoCache = new Map();
function repoLines(file) {
  if (!repoCache.has(file)) {
    const full = join(REPO_DIR, file);
    if (!existsSync(full)) throw new Error(`source file not found in team repo: ${file}`);
    repoCache.set(file, read(full).replace(/\r\n?/g, '\n').split('\n'));
  }
  return repoCache.get(file);
}

function sourceExcerpt(args, where) {
  if (!args.file) throw new Error(`${where}: ::source needs file="..."`);
  const lines = repoLines(args.file);
  let start = 0;
  if (args.from) {
    let remaining = Number(args.occurrence || 1);
    start = lines.findIndex((l) => l.includes(args.from) && --remaining === 0);
    if (start < 0) throw new Error(`${where}: from="${args.from}" not found in ${args.file}`);
  } else if (args.start) {
    start = Number(args.start) - 1;
  }
  let end = lines.length - 1;
  if (args.to) {
    end = lines.findIndex((l, k) => k >= start && l.includes(args.to));
    if (end < 0) throw new Error(`${where}: to="${args.to}" not found after line ${start + 1} in ${args.file}`);
  } else if (args.lines) {
    end = start + Number(args.lines) - 1;
  } else if (args.end) {
    end = Number(args.end) - 1;
  }
  end = Math.min(end, lines.length - 1);
  const slice = lines.slice(start, end + 1);
  const indent = Math.min(...slice.filter((l) => l.trim()).map((l) => l.search(/\S/)));
  const code = slice.map((l) => l.slice(Math.min(indent, l.search(/\S|$/)))).join('\n');
  const lang = args.lang || (/\.java$/.test(args.file) ? 'java' : /\.(json|auto|path)$/.test(args.file) ? 'json' : /\.gradle$/.test(args.file) ? 'groovy' : '');
  return codeBlock(code, lang, { hl: args.highlight }, {
    title: args.title || basename(args.file),
    startLine: start + 1,
    numbered: true,
    sourceHref: `${manifest.repo.url}/blob/${manifest.repo.sha}/${encodeURI(args.file)}#L${start + 1}-L${end + 1}`,
    sourceLabel: `${args.file.replace(/^src\/main\/java\/frc\/robot\//, '')} · L${start + 1}–${end + 1}`,
  });
}

function diagram(args, helpers, where) {
  const file = join(COURSE, 'diagrams', `${args.name}.svg`);
  if (!existsSync(file)) {
    problem(`${where}: missing diagram ${args.name}.svg`);
    return `<p class="missing">Diagram “${escapeHtml(args.name)}” is missing.</p>`;
  }
  const caption = args.caption ? `<figcaption>${helpers.inline(args.caption)}</figcaption>` : '';
  return `<figure class="diagram${args.wide ? ' diagram-wide' : ''}">${read(file).trim()}${caption}</figure>`;
}

const exerciseUsage = new Map();
const javaFile = (base, path) => ({ path, full: join(EXERCISES, base, ...path.split('/')) });
const testClassName = (path) => path.replace(/\.java$/, '').split('/').join('.');

function exerciseCard(args, body, helpers, where, route) {
  const ex = exerciseById.get(args.id);
  if (!ex) {
    problem(`${where}: exercise ${args.id} is not in course/exercises.json`);
    return `<p class="missing">Exercise ${escapeHtml(args.id)} is missing.</p>`;
  }
  if (!exerciseUsage.has(ex.id)) exerciseUsage.set(ex.id, route);
  const [task, ...hints] = body.split(/^---hint[ \t]*$/m);
  const renderFiles = (files) =>
    files
      .map((f) => {
        if (!existsSync(f.full)) {
          problem(`${where}: exercise ${ex.id} file missing: ${relative(ROOT, f.full)}`);
          return `<p class="missing">${escapeHtml(f.path)} not found.</p>`;
        }
        return codeBlock(read(f.full), 'java', {}, { title: f.path.split('/').pop(), numbered: true });
      })
      .join('');
  const id = `ex-${ex.id}`;
  const tests = ex.test.map((t) => testClassName(t));
  const levels = ['', 'Warm-up', 'Core', 'Stretch'];
  const tab = (key, label, selected) =>
    `<button type="button" role="tab" id="${id}-tab-${key}" aria-controls="${id}-panel-${key}" aria-selected="${selected}" tabindex="${selected ? 0 : -1}">${label}</button>`;
  const panel = (key, content, selected) =>
    `<div role="tabpanel" id="${id}-panel-${key}" aria-labelledby="${id}-tab-${key}"${selected ? '' : ' hidden'}>${content}</div>`;
  return (
    `<section class="exercise" id="${id}" data-exercise="${ex.id}" aria-labelledby="${id}-title">` +
    `<div class="ex-head"><p class="ex-eyebrow">Exercise · ${levels[ex.level] || 'Core'}${ex.wpilib ? ' · uses WPILib' : ''}</p>` +
    `<h3 class="ex-title" id="${id}-title">${escapeHtml(ex.title)}</h3>` +
    `<p class="ex-run">In the exercises project run <code>./gradlew test --tests ${escapeHtml(tests.join(' --tests '))}</code></p></div>` +
    `<div class="ex-task">${helpers.md(task)}</div>` +
    (hints.length
      ? `<div class="ex-hints">${hints.map((h, i) => `<details class="hint"><summary>Hint ${i + 1}</summary><div class="details-body">${helpers.md(h)}</div></details>`).join('')}</div>`
      : '') +
    `<div class="ex-tabs"><div role="tablist" aria-label="${escapeHtml(ex.title)} files">${tab('starter', 'Starter', true)}${tab('tests', 'Tests', false)}${tab('solution', 'Solution', false)}</div>` +
    panel('starter', renderFiles(ex.starter.map((p) => javaFile('src/main/java', p))), true) +
    panel('tests', renderFiles(ex.test.map((p) => javaFile('src/test/java', p))), false) +
    panel(
      'solution',
      `<div class="ex-gate"><p>Make an honest attempt first. Reading a solution after you have struggled teaches far more than reading it before.</p>` +
        `<button type="button" class="btn btn-quiet ex-reveal">Show the solution</button></div>` +
        `<div class="ex-solution" hidden>${renderFiles(ex.solution.map((p) => javaFile('solutions/java', p)))}</div>`,
      false
    ) +
    `</div><p class="ex-done"><input type="checkbox" id="${id}-done" data-exercise-done="${ex.id}"><label for="${id}-done">My tests pass for <code>${escapeHtml(tests.map((t) => t.split('.').pop()).join(', '))}</code></label></p>` +
    `</section>`
  );
}

// ---------- render ----------
const content = {};
const indexUnits = manifest.units.map((u) => ({ slug: u.slug, number: u.number, title: u.title, blurb: u.blurb, pages: [] }));
const indexReference = [];
const search = [];

function renderPage(page, sourceText) {
  const route = routeOf(page);
  const where = relative(ROOT, page.file);
  const { data, body } = splitFrontMatter(sourceText, where);
  const ctx = {
    where,
    docKey: route,
    resolveLink: (href) => resolveLink(href, where),
    source: (args) => sourceExcerpt(args, where),
    diagram: (args, helpers) => diagram(args, helpers, where),
    exercise: (args, text, helpers) => exerciseCard(args, text, helpers, where, route),
  };
  const doc = renderDocument(body, ctx);
  const key = page.unit ? page.unit.slug : 'reference';
  content[key] ??= {};
  content[key][page.slug] = {
    html: doc.html,
    headings: doc.headings,
    summary: data.summary || '',
    objectives: data.objectives || [],
    files: data.files || [],
  };
  const entry = {
    slug: page.slug,
    kind: page.kind,
    title: page.title,
    minutes: page.minutes || data.minutes || 0,
    summary: data.summary || '',
    quizzes: doc.quizCount,
    exams: doc.examCount,
    exercises: doc.exercises,
  };
  if (page.unit) indexUnits.find((u) => u.slug === page.unit.slug).pages.push(entry);
  else indexReference.push(entry);
  const text = stripTags(doc.html.replace(/<template[\s\S]*?<\/template>/g, ' ')).replace(/\s+/g, ' ').trim();
  search.push({
    route,
    title: page.title,
    section: page.unit ? `Unit ${page.unit.number} · ${page.unit.title}` : 'Reference',
    headings: doc.headings.map((h) => [h.id, h.text]),
    text: text.slice(0, 9000),
  });
}

let missingCount = 0;
for (const page of pages) {
  if (page.slug === 'exercise-catalog') continue;
  let text;
  if (existsSync(page.file)) {
    text = read(page.file);
  } else {
    missingCount++;
    problem(`missing page ${relative(ROOT, page.file)}`);
    text = `---\nsummary: Not written yet.\n---\n:::note Coming soon\nThis page has not been written yet.\n:::\n`;
  }
  try {
    renderPage(page, text);
  } catch (err) {
    problems.push(err.message);
  }
}

const catalogPage = pages.find((p) => p.slug === 'exercise-catalog');
if (catalogPage) {
  const byUnit = new Map();
  for (const ex of catalog) {
    if (!byUnit.has(ex.unit)) byUnit.set(ex.unit, []);
    byUnit.get(ex.unit).push(ex);
  }
  let md = `---\nsummary: Every coding exercise in the course, with the tests that grade it.\n---\n`;
  md += `Every exercise lives in the **exercises project** (see [Set Up the Exercises Project](course:01-tools/exercises-project)). `;
  md += `Starters are in \`src/main/java\`, tests in \`src/test/java\`, and reference solutions in \`solutions/java\`. `;
  md += `Mentors can check every solution at once with \`./gradlew test -Psolutions\`.\n\n`;
  for (const unit of manifest.units) {
    const list = byUnit.get(unit.slug);
    if (!list) continue;
    md += `## Unit ${unit.number}: ${unit.title}\n\n| Exercise | Level | Test class | Lesson |\n|---|---|---|---|\n`;
    for (const ex of list) {
      const used = exerciseUsage.get(ex.id);
      const lesson = used ? `[Open lesson](course:${used}?h=ex-${ex.id})` : 'Not yet used';
      md += `| <span id="ex-${ex.id}"></span>${ex.title}${ex.wpilib ? ' (WPILib)' : ''} | ${['', 'Warm-up', 'Core', 'Stretch'][ex.level]} | \`${ex.test.map((t) => testClassName(t).split('.').pop()).join(', ')}\` | ${lesson} |\n`;
    }
    md += '\n';
  }
  try {
    renderPage(catalogPage, md.replace(/course:([^)?]+)\?h=/g, 'course:$1#'));
  } catch (err) {
    problems.push(err.message);
  }
}
for (const ex of catalog) if (!exerciseUsage.has(ex.id)) warnings.push(`exercise ${ex.id} is not used by any lesson`);

// ---------- write site ----------
for (const [key, data] of Object.entries(content)) {
  write(join(SITE, 'assets', 'content', `${key}.js`), `(window.__CODEBOOK_CONTENT__ = window.__CODEBOOK_CONTENT__ || {})[${JSON.stringify(key)}] = ${safeJson(data)};\n`);
}
const builtAt = new Date().toISOString();
const builtVersion = Date.now().toString(36);
const index = {
  title: manifest.title,
  subtitle: manifest.subtitle,
  team: manifest.team,
  repo: { name: manifest.repo.name, url: manifest.repo.url, sha: manifest.repo.sha },
  builtAt,
  version: builtVersion,
  units: indexUnits,
  reference: indexReference.sort((a, b) => manifest.reference.findIndex((r) => r.slug === a.slug) - manifest.reference.findIndex((r) => r.slug === b.slug)),
  exercises: catalog.map((e) => ({ id: e.id, unit: e.unit, title: e.title, level: e.level, route: exerciseUsage.get(e.id) || null })),
  homeExcerpt: sourceExcerpt(
    {
      file: 'src/main/java/frc/robot/subsystems/turret/Turret.java',
      from: 'public Translation2d considerChassisSpeeds',
      to: 'return target;',
      highlight: '5,10,13-14',
      title: 'Turret.java · considerChassisSpeeds()',
    },
    'home page'
  ),
};
write(join(SITE, 'assets', 'course-index.js'), `window.__CODEBOOK_INDEX__ = ${safeJson(index)};\n`);
write(join(SITE, 'assets', 'search-index.js'), `window.__CODEBOOK_SEARCH__ = ${safeJson(search)};\n`);

const WEB = join(ROOT, 'tools', 'web');
for (const name of readdirSync(WEB)) copyFileSync(join(WEB, name), join(SITE, 'assets', name));

// exercises project downloads (student zip excludes solutions)
function collect(dir, base = dir, skip = () => false) {
  const out = [];
  if (!existsSync(dir)) return out;
  for (const name of readdirSync(dir)) {
    const full = join(dir, name);
    const rel = relative(base, full).split(sep).join('/');
    if (skip(rel)) continue;
    if (statSync(full).isDirectory()) out.push(...collect(full, base, skip));
    else out.push({ name: rel, data: readFileSync(full) });
  }
  return out;
}
const ignored = (rel) => /^(build|bin|\.gradle|\.idea|out)(\/|$)/.test(rel);
if (existsSync(EXERCISES)) {
  const student = collect(EXERCISES, EXERCISES, (rel) => ignored(rel) || /^solutions(\/|$)/.test(rel)).map((e) => ({ ...e, name: `iron-lions-exercises/${e.name}` }));
  const mentor = collect(EXERCISES, EXERCISES, ignored).map((e) => ({ ...e, name: `iron-lions-exercises-with-solutions/${e.name}` }));
  write(join(SITE, 'assets', 'download-exercises.js'), `window.__CODEBOOK_ZIP__ = window.__CODEBOOK_ZIP__ || {}; window.__CODEBOOK_ZIP__.student = "${makeZip(student).toString('base64')}";\n`);
  write(join(SITE, 'assets', 'download-exercises-mentor.js'), `window.__CODEBOOK_ZIP__ = window.__CODEBOOK_ZIP__ || {}; window.__CODEBOOK_ZIP__.mentor = "${makeZip(mentor).toString('base64')}";\n`);
}

const shellInfo = { title: manifest.title, description: manifest.subtitle, builtAt, sha: manifest.repo.sha, version: builtVersion };
write(join(SITE, 'index.html'), renderShell({ ...shellInfo, artifact: false }));
write(join(SITE, 'artifact.html'), renderShell({ ...shellInfo, artifact: true }));

// ---------- report ----------
const lessonCount = pages.filter((p) => p.kind === 'lesson').length;
console.log(`Built ${pages.length} pages (${lessonCount} lessons, ${pages.filter((p) => p.kind === 'unit-test').length} unit tests) → ${relative(process.cwd(), SITE) || 'site'}`);
console.log(`Exercises in catalog: ${catalog.length}; missing pages: ${missingCount}`);
for (const w of warnings.slice(0, 60)) console.warn(`warning: ${w}`);
if (warnings.length > 60) console.warn(`…and ${warnings.length - 60} more warnings`);
for (const p of problems) console.error(`error: ${p}`);
if (problems.length) process.exit(1);
