/* Search, "Ask a mentor" (claude.ai Artifact only), downloads, and progress import/export. */
(function () {
  'use strict';
  const C = window.Codebook;
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));

  function loadScript(src, ready) {
    if (ready()) return Promise.resolve();
    return new Promise((resolve, reject) => {
      const s = document.createElement('script');
      s.src = src;
      s.onload = () => (ready() ? resolve() : reject(new Error(`${src} loaded but was empty`)));
      s.onerror = () => reject(new Error(`Could not load ${src}`));
      document.head.appendChild(s);
    });
  }

  // ---------- search ----------
  const dialog = $('#search-dialog');
  const input = $('#search-input');
  const results = $('#search-results');
  const status = $('#search-status');
  const openers = $$('.search-open');
  let docs = null;

  async function openSearch() {
    if (!dialog.open) dialog.showModal();
    input.focus();
    input.select();
    if (!docs) {
      status.textContent = 'Loading search index…';
      try {
        await loadScript(`assets/search-index.js${C.versionQuery}`, () => Array.isArray(window.__CODEBOOK_SEARCH__));
        docs = window.__CODEBOOK_SEARCH__.map((d) => ({
          ...d,
          lcTitle: d.title.toLowerCase(),
          lcText: d.text.toLowerCase(),
          lcHeadings: d.headings.map(([id, text]) => [id, text.toLowerCase(), text]),
        }));
        status.textContent = '';
      } catch (err) {
        status.textContent = err.message;
        return;
      }
    }
    runSearch();
  }

  const tokenize = (q) => q.toLowerCase().split(/[^a-z0-9_.]+/).map((t) => t.replace(/^\.+|\.+$/g, '')).filter((t) => t.length >= 2);

  function runSearch() {
    if (!docs) return;
    const query = input.value.trim();
    const tokens = tokenize(query);
    results.innerHTML = '';
    if (tokens.length === 0) {
      status.textContent = query ? 'Type at least two letters.' : 'Search every lesson, reference page, and code excerpt.';
      return;
    }
    const phrase = query.toLowerCase();
    const scored = [];
    for (const doc of docs) {
      let score = 0;
      let heading = null;
      let ok = true;
      for (const token of tokens) {
        const inTitle = doc.lcTitle.includes(token);
        const h = doc.lcHeadings.find(([, lc]) => lc.includes(token));
        let count = 0;
        for (let at = doc.lcText.indexOf(token); at !== -1 && count < 6; at = doc.lcText.indexOf(token, at + token.length)) count++;
        if (!inTitle && !h && !count) {
          ok = false;
          break;
        }
        score += (inTitle ? 12 : 0) + (h ? 6 : 0) + count;
        if (h && !heading) heading = h;
      }
      if (!ok) continue;
      if (doc.lcTitle.includes(phrase)) score += 15;
      else if (doc.lcText.includes(phrase)) score += 4;
      scored.push({ doc, score, heading });
    }
    scored.sort((a, b) => b.score - a.score);
    const top = scored.slice(0, 25);
    status.textContent = top.length ? `${scored.length} result${scored.length === 1 ? '' : 's'}` : `No results for “${query}”. Try a shorter or different word.`;
    const highlight = (text) => {
      let html = C.esc(text);
      for (const token of tokens) {
        const safe = token.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
        html = html.replace(new RegExp(`(${safe})`, 'gi'), '<mark>$1</mark>');
      }
      return html;
    };
    results.innerHTML = top
      .map(({ doc, heading }) => {
        const at = doc.lcText.indexOf(tokens[0]);
        const start = Math.max(0, at - 70);
        const snippet = at >= 0 ? `${start > 0 ? '…' : ''}${doc.text.slice(start, start + 180)}…` : '';
        const href = `#/${doc.route}${heading ? `?h=${encodeURIComponent(heading[0])}` : ''}`;
        const where = heading ? `${doc.section} · ${heading[2]}` : doc.section;
        return (
          `<li><a class="search-hit" href="${href}"><span class="search-hit-section">${C.esc(where)}</span>` +
          `<span class="search-hit-title">${highlight(doc.title)}</span>${snippet ? `<span class="search-hit-snippet">${highlight(snippet)}</span>` : ''}</a></li>`
        );
      })
      .join('');
  }

  let searchTimer = null;
  input.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(runSearch, 90);
  });
  input.addEventListener('keydown', (event) => {
    if (event.key === 'ArrowDown') {
      const first = $('.search-hit', results);
      if (first) {
        event.preventDefault();
        first.focus();
      }
    } else if (event.key === 'Enter' && !event.isComposing) {
      const first = $('.search-hit', results);
      if (first) {
        event.preventDefault();
        first.click();
      }
    }
  });
  results.addEventListener('keydown', (event) => {
    const hits = $$('.search-hit', results);
    const i = hits.indexOf(document.activeElement);
    if (i < 0) return;
    if (event.key === 'ArrowDown' && hits[i + 1]) {
      event.preventDefault();
      hits[i + 1].focus();
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      (hits[i - 1] || input).focus();
    }
  });
  results.addEventListener('click', (event) => {
    if (event.target.closest('.search-hit')) dialog.close();
  });
  $('.search-close', dialog).addEventListener('click', () => dialog.close());
  openers.forEach((btn) => btn.addEventListener('click', openSearch));
  document.addEventListener('keydown', (event) => {
    const typing = event.target.closest('input, textarea, select, [contenteditable="true"]');
    if ((event.key === 'k' || event.key === 'K') && (event.ctrlKey || event.metaKey)) {
      event.preventDefault();
      openSearch();
    } else if (event.key === '/' && !typing && !event.ctrlKey && !event.metaKey && !event.altKey) {
      event.preventDefault();
      openSearch();
    }
  });
  if (/Mac|iPhone|iPad/.test(navigator.platform || '')) $$('.search-kbd').forEach((k) => (k.textContent = '⌘ K'));

  // ---------- downloads (Blob link locally; downloads capability inside a claude.ai Artifact) ----------
  async function saveFile(filename, data, type) {
    if (C.isArtifact) {
      const downloads = await C.use('downloads');
      if (!downloads) {
        C.toast('Downloads are not available in this view.');
        return false;
      }
      try {
        await downloads.save({ filename, data: typeof data === 'string' ? data : new Blob([data], { type }) });
        C.toast(`Saved ${filename}`);
        return true;
      } catch (err) {
        const code = err && err.code;
        if (code === 'declined') C.toast('Download canceled.');
        else if (code === 'rate_limited') C.toast('A download prompt is already open. Finish it first.');
        else C.toast(`This view cannot save ${filename.split('.').pop()} files. Ask a mentor for a copy.`);
        return false;
      }
    }
    const url = URL.createObjectURL(new Blob([data], { type }));
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    setTimeout(() => URL.revokeObjectURL(url), 5000);
    C.toast(`Downloading ${filename}`);
    return true;
  }

  async function downloadZip(kind) {
    const src = (kind === 'mentor' ? 'assets/download-exercises-mentor.js' : 'assets/download-exercises.js') + C.versionQuery;
    try {
      C.toast('Preparing the exercises project…');
      await loadScript(src, () => Boolean(window.__CODEBOOK_ZIP__ && window.__CODEBOOK_ZIP__[kind]));
      const bytes = Uint8Array.from(atob(window.__CODEBOOK_ZIP__[kind]), (c) => c.charCodeAt(0));
      await saveFile(kind === 'mentor' ? 'iron-lions-exercises-with-solutions.zip' : 'iron-lions-exercises.zip', bytes, 'application/zip');
    } catch (err) {
      C.toast(err.message);
    }
  }

  C.hooks.progressPage.push((view) => {
    const actions = $('#progress-actions', view);
    let resetArmed = null;
    view.addEventListener('click', (event) => {
      const btn = event.target.closest('button[data-action]');
      if (!btn) return;
      const action = btn.dataset.action;
      if (action === 'export') {
        const stamp = new Date().toISOString().slice(0, 10);
        saveFile(`iron-lions-codebook-progress-${stamp}.json`, JSON.stringify(C.progress.all(), null, 2), 'application/json');
      } else if (action === 'reset') {
        if (resetArmed) {
          clearTimeout(resetArmed);
          C.progress.reset();
          C.toast('All progress erased.');
          setTimeout(() => location.reload(), 600);
        } else {
          btn.textContent = 'Click again to erase everything';
          resetArmed = setTimeout(() => {
            btn.textContent = 'Reset all progress';
            resetArmed = null;
          }, 5000);
        }
      } else if (action === 'zip-student') {
        downloadZip('student');
      } else if (action === 'zip-mentor') {
        downloadZip('mentor');
      }
    });
    const file = $('#import-file', actions);
    file.addEventListener('change', async () => {
      const chosen = file.files && file.files[0];
      if (!chosen) return;
      try {
        const data = JSON.parse(await chosen.text());
        if (!data || data.v !== 1 || typeof data.lessons !== 'object') throw new Error('not a Codebook progress file');
        C.progress.replace(data);
        C.toast('Progress imported.');
        setTimeout(() => location.reload(), 600);
      } catch (err) {
        C.toast(`Import failed: ${err.message}.`);
      }
    });
  });

  // ---------- Ask a mentor (only when the claude.ai viewer grants the sample capability) ----------
  const samplePromise = C.isArtifact ? C.use('sample') : Promise.resolve(null);
  let mentorDisabled = false;

  const RULES = [
    'You are a patient programming mentor for FIRST Robotics Competition Team 967, the Iron Lions, from Linn-Mar High School in Marion, Iowa.',
    'The student is working through the "Iron Lions Codebook" course. Their robot code is the Rebuilt-2026 repository: Java 17, WPILib 2026 command-based,',
    'REVLib 2026 (SparkMax/SparkFlex), AdvantageKit 26, PathPlannerLib 2026, PhotonLib 2026, a NavX gyro, swerve drive, a turret shooter, and an intake.',
    'Rules: explain at a high-school level and use the lesson text below as your main source. Keep answers under about 250 words unless asked for more.',
    'If the question is about a graded exercise, give hints, questions, and small examples, never a complete solution.',
    'Never invent CAN IDs, constants, or API methods. If you are not sure an API exists in the 2026 libraries, say so and point to the official docs.',
    'For anything that could damage the robot or hurt someone, tell the student to check with a mentor first.',
  ].join(' ');

  function lessonText(view) {
    const body = $('.page-body', view);
    if (!body) return '';
    const clone = body.cloneNode(true);
    $$('template, .mentor, .quiz, .ex-solution', clone).forEach((el) => el.remove());
    return clone.textContent.replace(/\s+\n/g, '\n').replace(/[ \t]+/g, ' ').trim().slice(0, 14000);
  }

  C.hooks.page.push(async ({ view, entry, route }) => {
    if (!C.isArtifact || mentorDisabled || entry.page.kind === 'unit-test') return;
    const sample = await samplePromise;
    if (!sample || mentorDisabled || C.currentRoute() !== route) return;
    const body = $('.page-body', view);
    if (!body || $('.mentor', view)) return;

    const panel = document.createElement('section');
    panel.className = 'mentor';
    panel.setAttribute('aria-labelledby', 'mentor-title');
    panel.innerHTML =
      '<div class="mentor-head"><h2 id="mentor-title">Ask a mentor</h2>' +
      '<p>Claude answers using this page as context, on your own Claude account. Double-check anything that matters with a human mentor.</p></div>' +
      '<div class="mentor-log" aria-live="polite"></div>' +
      '<form class="mentor-form"><label class="visually-hidden" for="mentor-q">Your question</label>' +
      '<textarea id="mentor-q" name="q" placeholder="Ask about anything on this page, like “why does the turret subtract the robot heading?”"></textarea>' +
      '<div class="button-row"><button type="submit" class="btn btn-primary">Ask</button><button type="button" class="btn btn-quiet mentor-stop" hidden>Stop</button></div>' +
      '<p class="mentor-note">Hints only for graded exercises. Nothing you ask is saved by this page.</p></form>';
    body.after(panel);

    const log = $('.mentor-log', panel);
    const form = $('.mentor-form', panel);
    const box = $('textarea', panel);
    const ask = $('button[type="submit"]', panel);
    const stop = $('.mentor-stop', panel);
    const turns = [];
    let controller = null;

    const bubble = (who, text) => {
      const p = document.createElement('p');
      p.className = `mentor-msg ${who}`;
      p.textContent = text;
      log.appendChild(p);
      log.scrollTop = log.scrollHeight;
      return p;
    };

    $$('figure.code-block', view).forEach((figure) => {
      const cap = $('.code-cap', figure);
      if (!cap || $('.ask-btn', cap)) return;
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'copy-btn ask-btn';
      btn.textContent = 'Ask';
      btn.setAttribute('aria-label', 'Ask the mentor about this code');
      btn.addEventListener('click', () => {
        const code = $$('.line', figure).map((l) => l.textContent).join('\n').slice(0, 4000);
        const title = ($('.code-title', figure) || {}).textContent || 'this code';
        box.value = `Explain this code from ${title} line by line:\n\n${code}`;
        panel.scrollIntoView({ block: 'center' });
        box.focus();
      });
      cap.insertBefore(btn, $('.copy-btn:not(.ask-btn)', cap));
    });

    form.addEventListener('submit', async (event) => {
      event.preventDefault();
      const question = box.value.trim();
      if (!question || controller) return;
      bubble('you', question);
      const answer = bubble('claude', 'Thinking…');
      const context = `${RULES}\n\nLESSON: ${entry.page.title}\n\n${lessonText(view)}`;
      const history = turns.slice(-6);
      const input = [{ role: 'user', content: context }, ...history, { role: 'user', content: question }];
      controller = new AbortController();
      ask.disabled = true;
      stop.hidden = false;
      try {
        const { text, truncated } = await sample(input, {
          cache: false,
          signal: controller.signal,
          onText: ({ text: soFar }) => {
            answer.textContent = soFar;
            log.scrollTop = log.scrollHeight;
          },
        });
        answer.textContent = truncated ? `${text}\n\n(Answer cut short. Ask a narrower question.)` : text;
        turns.push({ role: 'user', content: question }, { role: 'assistant', content: text });
        box.value = '';
      } catch (err) {
        const code = err && err.code;
        if (['not_granted', 'sampling_disabled', 'not_declared', 'capability_disabled', 'capability_removed'].includes(code)) {
          mentorDisabled = true;
          panel.remove();
          C.toast('Ask a mentor is turned off for this view.');
          return;
        }
        const partial = err && err.text ? `${err.text}\n\n` : '';
        const messages = {
          cancelled: `${partial}(Stopped.)`,
          rate_limited: `${partial}Too many questions right now. Wait a minute, then ask again.`,
          session_expired: 'Sign in to Claude again, then ask.',
          refused: 'Claude declined that question. Try rephrasing it.',
          prompt_too_large: 'That question is too long. Shorten the code or text you pasted.',
          empty_completion: 'No answer came back. Try asking in a different way.',
        };
        answer.textContent = messages[code] || `${partial}Something went wrong before the answer finished. Your question is still in the box, so you can ask again.`;
      } finally {
        controller = null;
        ask.disabled = false;
        stop.hidden = true;
      }
    });
    stop.addEventListener('click', () => controller && controller.abort());
  });
})();
