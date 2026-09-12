/* Iron Lions Codebook runtime: routing, navigation, page rendering, progress, theme. Classic script (works from file://). */
(function () {
  'use strict';
  const INDEX = window.__CODEBOOK_INDEX__;
  const IS_ARTIFACT = Boolean(window.__CODEBOOK_ARTIFACT__);
  const VERSION_QUERY = !IS_ARTIFACT && INDEX.version ? `?v=${encodeURIComponent(INDEX.version)}` : '';
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
  const esc = (s) => String(s ?? '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

  // ---------- storage (localStorage can be missing or throw; fall back to memory) ----------
  const memory = {};
  const store = {
    get(key, fallback) {
      try {
        const raw = localStorage.getItem('codebook:' + key);
        return raw == null ? fallback : JSON.parse(raw);
      } catch (e) {
        return key in memory ? memory[key] : fallback;
      }
    },
    set(key, value) {
      memory[key] = value;
      try {
        localStorage.setItem('codebook:' + key, JSON.stringify(value));
      } catch (e) {
        /* private mode or blocked storage: keep in memory for this visit */
      }
    },
  };

  const blank = () => ({ v: 1, lessons: {}, quizzes: {}, exams: {}, exercises: {}, tasks: {}, visited: {}, last: null });
  let state = Object.assign(blank(), store.get('progress', {}));
  const save = () => {
    store.set('progress', state);
    refreshProgressUi();
  };
  const progress = {
    all: () => state,
    replace(next) {
      state = Object.assign(blank(), next || {});
      save();
    },
    reset() {
      state = blank();
      save();
    },
    isDone: (route) => Boolean(state.lessons[route] && state.lessons[route].done),
    setDone(route, done) {
      state.lessons[route] = { done, at: Date.now() };
      save();
    },
    quiz: (key) => state.quizzes[key] || null,
    setQuiz(key, data) {
      state.quizzes[key] = data;
      save();
    },
    exam: (key) => state.exams[key] || null,
    setExam(key, data) {
      state.exams[key] = data;
      save();
    },
    exercise: (id) => Boolean(state.exercises[id] && state.exercises[id].done),
    setExercise(id, done) {
      state.exercises[id] = { done, at: Date.now() };
      save();
    },
    task: (key) => Boolean(state.tasks[key]),
    setTask(key, done) {
      state.tasks[key] = done;
      save();
    },
    visit(route) {
      state.visited[route] = Date.now();
      state.last = route;
      store.set('progress', state);
    },
  };

  // ---------- course model ----------
  const flat = [];
  for (const unit of INDEX.units) {
    unit.pages.forEach((page, i) => flat.push({ unit, page, route: `${unit.slug}/${page.slug}`, lessonNumber: page.kind === 'lesson' ? i + 1 : null }));
  }
  const refPages = INDEX.reference.map((page) => ({ unit: null, page, route: `reference/${page.slug}` }));
  const byRoute = new Map([...flat, ...refPages].map((entry) => [entry.route, entry]));
  const lessonsOnly = flat.filter((e) => e.page.kind === 'lesson');

  function statusOf(entry) {
    if (entry.page.kind === 'unit-test') {
      const exam = state.exams[`${entry.route}#exam1`];
      if (exam && exam.passed) return 'done';
      return exam ? 'started' : 'none';
    }
    if (progress.isDone(entry.route)) return 'done';
    const quizStarted = Object.keys(state.quizzes).some((k) => k.startsWith(entry.route + '#'));
    return quizStarted || state.visited[entry.route] ? 'started' : 'none';
  }
  const unitStats = (unit) => {
    const entries = flat.filter((e) => e.unit === unit);
    const done = entries.filter((e) => statusOf(e) === 'done').length;
    return { done, total: entries.length, minutes: unit.pages.reduce((sum, p) => sum + (p.minutes || 0), 0), exercises: unit.pages.reduce((n, p) => n + p.exercises.length, 0) };
  };
  const percentComplete = () => {
    const all = flat.length;
    return all ? Math.round((flat.filter((e) => statusOf(e) === 'done').length / all) * 100) : 0;
  };

  // ---------- content loading ----------
  const pending = {};
  function loadContent(key) {
    const bag = (window.__CODEBOOK_CONTENT__ = window.__CODEBOOK_CONTENT__ || {});
    if (bag[key]) return Promise.resolve(bag[key]);
    if (!pending[key]) {
      pending[key] = new Promise((resolve, reject) => {
        const s = document.createElement('script');
        s.src = `assets/content/${key}.js${VERSION_QUERY}`;
        s.onload = () => (bag[key] ? resolve(bag[key]) : reject(new Error('empty content file')));
        s.onerror = () => {
          delete pending[key];
          reject(new Error(`Could not load assets/content/${key}.js`));
        };
        document.head.appendChild(s);
      });
    }
    return pending[key];
  }

  // ---------- navigation ----------
  const view = $('#view');
  const sidebar = $('#sidebar');
  const tocEl = $('#toc');
  let currentRoute = null;

  function navHtml(activeRoute) {
    const activeUnit = activeRoute ? activeRoute.split('/')[0] : null;
    const top = [
      ['', 'Home'],
      ['progress', 'Your progress'],
    ]
      .map(([r, label]) => `<a href="#/${r}"${(activeRoute || '') === r ? ' aria-current="page"' : ''}>${label}</a>`)
      .join('');
    const units = INDEX.units
      .map((unit) => {
        const stats = unitStats(unit);
        const open = unit.slug === activeUnit;
        const lessons = unit.pages
          .map((page) => {
            const route = `${unit.slug}/${page.slug}`;
            const status = statusOf(byRoute.get(route));
            const label = status === 'done' ? 'completed' : status === 'started' ? 'started' : 'not started';
            return (
              `<li><a class="nav-link" href="#/${route}"${route === activeRoute ? ' aria-current="page"' : ''}>` +
              `<span class="nav-status" data-status="${status}"><span class="visually-hidden">${label}: </span></span>` +
              `<span>${esc(page.title)}</span>${page.kind === 'unit-test' ? '<span class="nav-kind">test</span>' : ''}</a></li>`
            );
          })
          .join('');
        return (
          `<li class="nav-unit${open ? ' is-current' : ''}"><button type="button" class="nav-unit-toggle" aria-expanded="${open}" aria-controls="nav-${unit.slug}">` +
          `<span class="nav-unit-num">${unit.number}</span><span>${esc(unit.title)}</span>` +
          `<span class="nav-unit-meter">${stats.done}/${stats.total}</span></button>` +
          `<ul class="nav-lessons" id="nav-${unit.slug}"${open ? '' : ' hidden'}>${lessons}</ul></li>`
        );
      })
      .join('');
    const refs = refPages
      .map((e) => `<li><a class="nav-link" href="#/${e.route}"${e.route === activeRoute ? ' aria-current="page"' : ''}><span>${esc(e.page.title)}</span></a></li>`)
      .join('');
    return (
      `<div class="nav-top">${top}</div>` +
      `<p class="nav-heading">Units</p><ul class="nav-list">${units}</ul>` +
      `<p class="nav-heading">Reference</p><ul class="nav-list nav-lessons nav-ref">${refs}</ul>`
    );
  }

  function wireNav(root) {
    root.addEventListener('click', (event) => {
      const toggle = event.target.closest('.nav-unit-toggle');
      if (toggle) {
        const list = document.getElementById(toggle.getAttribute('aria-controls'));
        const open = toggle.getAttribute('aria-expanded') !== 'true';
        toggle.setAttribute('aria-expanded', String(open));
        list.hidden = !open;
      }
    });
  }

  function renderNav() {
    const openUnits = new Set($$('.nav-unit-toggle[aria-expanded="true"]', sidebar).map((b) => b.getAttribute('aria-controls')));
    sidebar.innerHTML = navHtml(currentRoute);
    openUnits.forEach((id) => {
      const btn = $(`.nav-unit-toggle[aria-controls="${id}"]`, sidebar);
      if (btn) {
        btn.setAttribute('aria-expanded', 'true');
        document.getElementById(id).hidden = false;
      }
    });
    const active = $('.nav-link[aria-current="page"]', sidebar);
    if (active && !isVisibleIn(active, sidebar)) active.scrollIntoView({ block: 'center' });
  }
  const isVisibleIn = (el, container) => {
    const a = el.getBoundingClientRect();
    const b = container.getBoundingClientRect();
    return a.top >= b.top && a.bottom <= b.bottom;
  };

  function refreshProgressUi() {
    const pill = $('#progress-pill');
    if (pill) pill.textContent = `${percentComplete()}% complete`;
    if (sidebar.innerHTML) renderNav();
  }

  // ---------- routing ----------
  function parseHash() {
    const raw = decodeURIComponent(location.hash.replace(/^#\/?/, ''));
    const [path, query = ''] = raw.split('?');
    const params = new URLSearchParams(query);
    return { path: path.replace(/\/+$/, ''), heading: params.get('h') };
  }

  async function route() {
    const { path, heading } = parseHash();
    if (path === currentRoute && view.dataset.ready) {
      if (heading) scrollToHeading(heading);
      return;
    }
    currentRoute = path;
    closeNavDialog();
    try {
      if (!path) renderHome();
      else if (path === 'progress') renderProgress();
      else if (INDEX.units.some((u) => u.slug === path)) renderUnit(INDEX.units.find((u) => u.slug === path));
      else if (byRoute.has(path)) await renderPage(byRoute.get(path));
      else renderNotFound(path);
    } catch (err) {
      view.className = 'view';
      view.innerHTML = `<h1 class="page-title">This page did not load</h1><p>${esc(err.message)}</p><p>If you opened the site from a zip, extract the whole folder first and open <code>index.html</code> again.</p>`;
    }
    view.dataset.ready = 'true';
    renderNav();
    if (heading) requestAnimationFrame(() => scrollToHeading(heading));
    else {
      window.scrollTo(0, 0);
      const h1 = $('h1', view);
      if (h1 && document.activeElement !== document.body) h1.focus({ preventScroll: true });
    }
  }

  function scrollToHeading(id) {
    const target = document.getElementById(id);
    if (!target) return;
    const details = target.closest('details');
    if (details) details.open = true;
    target.scrollIntoView({ block: 'start' });
    if (target.matches('h2, h3, h4')) target.focus({ preventScroll: true });
  }

  function setToc(headings) {
    const hasToc = Boolean(headings && headings.length >= 2);
    document.querySelector('.layout').classList.toggle('no-toc', !hasToc);
    if (!hasToc) {
      tocEl.innerHTML = '';
      return;
    }
    tocEl.innerHTML =
      `<p class="toc-title">On this page</p><ol class="toc-list">` +
      headings.map((h) => `<li><a class="toc-link${h.level === 3 ? ' toc-l3' : ''}" href="#/${currentRoute}?h=${esc(h.id)}" data-target="${esc(h.id)}">${esc(h.text)}</a></li>`).join('') +
      `</ol>`;
    const links = $$('.toc-link', tocEl);
    const targets = links.map((l) => document.getElementById(l.dataset.target)).filter(Boolean);
    if (!('IntersectionObserver' in window) || !targets.length) return;
    const visible = new Map();
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => visible.set(entry.target.id, entry.isIntersecting));
        const firstVisible = targets.find((t) => visible.get(t.id));
        if (!firstVisible) return;
        links.forEach((l) => l.setAttribute('aria-current', String(l.dataset.target === firstVisible.id)));
      },
      { rootMargin: '-64px 0px -60% 0px', threshold: 0 }
    );
    targets.forEach((t) => observer.observe(t));
    tocEl._observer && tocEl._observer.disconnect();
    tocEl._observer = observer;
  }

  // ---------- page renderers ----------
  const minutesLabel = (m) => (m >= 60 ? `${Math.floor(m / 60)} h ${m % 60 ? `${m % 60} min` : ''}`.trim() : `${m} min`);

  async function renderPage(entry) {
    const key = entry.unit ? entry.unit.slug : 'reference';
    view.className = 'view';
    view.innerHTML = `<p class="loading">Loading ${esc(entry.page.title)}…</p>`;
    const bag = await loadContent(key);
    const data = bag[entry.page.slug];
    if (!data) throw new Error(`No content for ${entry.route}`);
    const { unit, page } = entry;
    progress.visit(entry.route);
    document.title = `${page.title} · ${INDEX.title}`;

    let eyebrow = '<p class="eyebrow">Reference</p>';
    if (unit) {
      const lessonCount = unit.pages.filter((p) => p.kind === 'lesson').length;
      const where = page.kind === 'unit-test' ? 'Unit test' : `Lesson ${entry.lessonNumber} of ${lessonCount}`;
      eyebrow = `<p class="eyebrow"><a href="#/${unit.slug}">Unit ${unit.number} · ${esc(unit.title)}</a><span>${where}</span></p>`;
    }
    const meta = [];
    if (page.minutes) meta.push(`<li><strong>${page.minutes}</strong> min</li>`);
    if (page.quizzes) meta.push(`<li><strong>${page.quizzes}</strong> ${page.quizzes === 1 ? 'check' : 'checks'}</li>`);
    if (page.exercises.length) meta.push(`<li><strong>${page.exercises.length}</strong> ${page.exercises.length === 1 ? 'exercise' : 'exercises'}</li>`);
    if (page.kind === 'unit-test') meta.push('<li>pass mark <strong>80%</strong></li>');
    const chips = data.files.length
      ? `<ul class="file-chips" aria-label="Team code this page reads">${data.files
          .map((f) => `<li><a class="chip" href="${esc(INDEX.repo.url)}/blob/${INDEX.repo.sha}/${encodeURI(f)}" target="_blank" rel="noopener noreferrer">${esc(f.split('/').pop())}</a></li>`)
          .join('')}</ul>`
      : '';
    const objectives = data.objectives.length
      ? `<div class="objectives"><p class="objectives-title">By the end you can</p><ul>${data.objectives.map((o) => `<li>${esc(o)}</li>`).join('')}</ul></div>`
      : '';

    let foot = '';
    if (unit) {
      const i = flat.indexOf(entry);
      const prev = flat[i - 1];
      const next = flat[i + 1];
      const link = (e, cls, label) =>
        e ? `<a class="pager-link ${cls}" href="#/${e.route}"><span class="pager-label">${label}</span><span class="pager-title">${esc(e.page.title)}</span></a>` : '';
      const done = progress.isDone(entry.route);
      const completeRow =
        page.kind === 'lesson'
          ? `<div class="complete-row"><button type="button" class="btn ${done ? 'btn-quiet' : 'btn-primary'} complete-btn" aria-pressed="${done}">${done ? 'Completed' : 'Mark lesson complete'}</button>` +
            `<p class="complete-note">Progress is saved in this browser. Export it from <a href="#/progress">Your progress</a>.</p></div>`
          : '';
      foot = `<footer class="page-foot">${completeRow}<nav class="pager" aria-label="Previous and next">${link(prev, 'prev', 'Previous')}${link(next, 'next', 'Next')}</nav></footer>`;
    }

    view.innerHTML =
      `<article class="page page-${page.kind}" aria-labelledby="page-title"><header class="page-head">${eyebrow}` +
      `<h1 class="page-title" id="page-title" tabindex="-1">${esc(page.title)}</h1>` +
      (data.summary ? `<p class="page-summary">${esc(data.summary)}</p>` : '') +
      (meta.length ? `<ul class="meta-row">${meta.join('')}</ul>` : '') +
      chips +
      objectives +
      `</header><div class="page-body">${data.html}</div>${foot}</article>`;

    enhance(view, entry);
    setToc(data.headings);
    Codebook.hooks.page.forEach((fn) => {
      try {
        fn({ view, route: entry.route, entry, data });
      } catch (err) {
        console.error(err);
      }
    });
  }

  function renderUnit(unit) {
    view.className = 'view';
    document.title = `Unit ${unit.number}: ${unit.title} · ${INDEX.title}`;
    setToc([]);
    const stats = unitStats(unit);
    const rows = unit.pages
      .map((page, i) => {
        const route = `${unit.slug}/${page.slug}`;
        const status = statusOf(byRoute.get(route));
        const n = page.kind === 'lesson' ? i + 1 : '✓';
        const meta = status === 'done' ? '<span class="done">done</span>' : page.minutes ? `${page.minutes} min` : '';
        return (
          `<li><a class="lesson-row" href="#/${route}"><span class="lesson-n" aria-hidden="true">${n}</span>` +
          `<span><span class="lesson-title">${esc(page.title)}</span>${page.summary ? `<span class="lesson-summary">${esc(page.summary)}</span>` : ''}</span>` +
          `<span class="lesson-meta">${meta}</span></a></li>`
        );
      })
      .join('');
    view.innerHTML =
      `<header class="page-head"><p class="eyebrow">Unit ${unit.number}</p><h1 class="page-title" id="page-title" tabindex="-1">${esc(unit.title)}</h1>` +
      `<p class="page-summary">${esc(unit.blurb)}</p><ul class="meta-row"><li><strong>${stats.done}/${stats.total}</strong> complete</li>` +
      `<li><strong>${minutesLabel(stats.minutes)}</strong> of lessons</li><li><strong>${stats.exercises}</strong> exercises</li></ul></header>` +
      `<ol class="lesson-list">${rows}</ol>`;
  }

  function renderHome() {
    view.className = 'view view-wide';
    document.title = INDEX.title;
    setToc([]);
    const last = state.last && byRoute.get(state.last);
    const totalMinutes = INDEX.units.reduce((sum, u) => sum + unitStats(u).minutes, 0);
    const exerciseCount = INDEX.exercises.length;
    const resume = last
      ? `<div class="resume"><p><span class="resume-label">Pick up where you left off</span><br><strong>${esc(last.page.title)}</strong></p><a class="btn btn-primary" href="#/${last.route}">Resume</a></div>`
      : '';
    const units = INDEX.units
      .map((unit) => {
        const s = unitStats(unit);
        const pct = s.total ? Math.round((s.done / s.total) * 100) : 0;
        return (
          `<li><a class="unit-row${s.done === s.total && s.total ? ' is-done' : ''}" href="#/${unit.slug}"><span class="unit-num" aria-hidden="true">${String(unit.number).padStart(2, '0')}</span>` +
          `<span class="unit-title"><span class="visually-hidden">Unit ${unit.number}: </span>${esc(unit.title)}</span><span class="unit-blurb">${esc(unit.blurb)}</span>` +
          `<span class="unit-stats"><span>${unit.pages.filter((p) => p.kind === 'lesson').length} lessons</span><span>${minutesLabel(s.minutes)}</span>` +
          `<span>${s.exercises} exercises</span><span class="bar" role="img" aria-label="${pct}% complete"><span style="width:${pct}%"></span></span></span></a></li>`
        );
      })
      .join('');
    view.innerHTML =
      `<section class="hero" aria-labelledby="page-title"><div class="hero-copy">` +
      `<p class="hero-kicker">FRC Team ${INDEX.team} · Linn-Mar Robotics · Marion, Iowa</p>` +
      `<h1 class="hero-title" id="page-title" tabindex="-1">From hello world to <em>shooting on the move</em></h1>` +
      `<p class="hero-lede">A ground-up programming course built on the Iron Lions’ own 2026 robot code. Lessons point at real lines of <a href="${esc(INDEX.repo.url)}" target="_blank" rel="noopener noreferrer">Rebuilt-2026</a>, checks save as you go, and every exercise is graded by JUnit tests you run in VS Code.</p>` +
      `<div class="hero-actions"><a class="btn btn-primary" href="#/00-welcome/welcome-to-967">Start at Unit 0</a><a class="btn btn-quiet" href="#/00-welcome/placement-check">Take the placement check</a></div>` +
      `${resume}</div><figure class="hero-code">${INDEX.homeExcerpt || ''}<ol class="hero-notes">` +
      `<li>Line 273 turns the robot’s own speed into field-relative speed.</li>` +
      `<li>Line 278 looks up how long a FUEL shot flies at this distance, scaled by the tunable “Constant of Reality.”</li>` +
      `<li>Lines 281–282 aim at where the HUB will <em>appear</em> to be after the robot keeps moving. Unit 14 builds this with you.</li>` +
      `</ol></figure></section>` +
      `<section class="home-section" aria-labelledby="map-title"><div class="home-section-head"><h2 id="map-title">Course map</h2>` +
      `<p>${INDEX.units.length} units · ${lessonsOnly.length} lessons · ${minutesLabel(totalMinutes)} · ${exerciseCount} graded exercises</p></div><ol class="course-map">${units}</ol></section>` +
      `<section class="home-section" aria-labelledby="how-title"><div class="home-section-head"><h2 id="how-title">How each unit works</h2></div><ol class="how-list">` +
      `<li><strong>Read</strong><p>Short lessons that explain an idea, then show where it lives in our robot code.</p></li>` +
      `<li><strong>Check</strong><p>Quick questions after each lesson. You get feedback immediately and can retry.</p></li>` +
      `<li><strong>Build</strong><p>Coding exercises in a real GradleRIO project, graded by JUnit tests.</p></li>` +
      `<li><strong>Prove</strong><p>A unit test at the end of each unit. Pass with 80% to mark the unit done.</p></li>` +
      `<li><strong>Ship</strong><p>Capstone projects that change real subsystems, reviewed like team pull requests.</p></li></ol></section>` +
      `<section class="home-section" aria-labelledby="season-title"><div class="home-section-head"><h2 id="season-title">Suggested pace for 2026–27</h2><p>Adjust with your mentors.</p></div><ol class="season">` +
      `<li><time datetime="2026-09">September</time><p><strong>Units 0–2.</strong> Install the toolchain, run the simulator, learn Java basics.</p></li>` +
      `<li><time datetime="2026-10-02">Oct 2</time><p><strong>Clash in the Corn.</strong> Shadow the pit programmer with Units 3–4 (objects and Git) finished.</p></li>` +
      `<li><time datetime="2026-11">November</time><p><strong>Units 5–7.</strong> Advanced Java, robot foundations, and command-based code.</p></li>` +
      `<li><time datetime="2026-12">December</time><p><strong>Units 8–11.</strong> State machines, controls, swerve, logging. Read the 2027 Systemcore lesson.</p></li>` +
      `<li><time datetime="2027-01-09">Jan 9, 2027</time><p><strong>Kickoff.</strong> Units 12–14 become your build-season playbook; start a capstone.</p></li>` +
      `<li><time datetime="2027-02">Build season</time><p><strong>Units 15–16.</strong> Test, review, and practice the pit runbook before the first event.</p></li></ol></section>` +
      `<section class="home-section" aria-labelledby="ref-title"><div class="home-section-head"><h2 id="ref-title">Keep these open</h2></div><p class="quick-links">` +
      refPages.slice(0, 7).map((e) => `<a class="btn btn-quiet" href="#/${e.route}">${esc(e.page.title)}</a>`).join('') +
      `</p></section>`;
    enhance(view, null);
  }

  function renderProgress() {
    view.className = 'view';
    document.title = `Your progress · ${INDEX.title}`;
    setToc([]);
    const lessonsDone = lessonsOnly.filter((e) => statusOf(e) === 'done').length;
    const tests = flat.filter((e) => e.page.kind === 'unit-test');
    const testsPassed = tests.filter((e) => statusOf(e) === 'done').length;
    const exercisesDone = INDEX.exercises.filter((ex) => progress.exercise(ex.id)).length;
    const correct = Object.values(state.quizzes).reduce((n, q) => n + Object.values(q.results || {}).filter((r) => r.correct).length, 0);
    const rows = INDEX.units
      .map((unit) => {
        const s = unitStats(unit);
        const exam = state.exams[`${unit.slug}/unit-test#exam1`];
        return `<tr><td><a href="#/${unit.slug}">Unit ${unit.number}: ${esc(unit.title)}</a></td><td>${s.done}/${s.total}</td><td>${exam ? `${exam.best}%${exam.passed ? ' (passed)' : ''}` : '—'}</td></tr>`;
      })
      .join('');
    view.innerHTML =
      `<header class="page-head"><p class="eyebrow">Stored in this browser</p><h1 class="page-title" id="page-title" tabindex="-1">Your progress</h1>` +
      `<p class="page-summary">Progress lives on this device only. Export it before switching computers, then import it on the new one.</p></header>` +
      `<div class="stat-row"><p class="stat"><span class="stat-num">${lessonsDone}/${lessonsOnly.length}</span><span class="stat-label">lessons complete</span></p>` +
      `<p class="stat"><span class="stat-num">${testsPassed}/${tests.length}</span><span class="stat-label">unit tests passed</span></p>` +
      `<p class="stat"><span class="stat-num">${exercisesDone}/${INDEX.exercises.length}</span><span class="stat-label">exercises passing</span></p>` +
      `<p class="stat"><span class="stat-num">${correct}</span><span class="stat-label">check questions answered correctly</span></p></div>` +
      `<div class="table-wrap"><table><thead><tr><th scope="col">Unit</th><th scope="col">Pages done</th><th scope="col">Unit test best</th></tr></thead><tbody>${rows}</tbody></table></div>` +
      `<h2>Move or reset your progress</h2><div class="button-row" id="progress-actions">` +
      `<button type="button" class="btn btn-quiet" data-action="export">Export progress file</button>` +
      `<label class="btn btn-quiet" for="import-file">Import progress file</label><input class="visually-hidden" type="file" id="import-file" accept="application/json,.json">` +
      `<button type="button" class="btn btn-quiet" data-action="reset">Reset all progress</button></div>` +
      `<h2>Get the exercises project</h2><p>The exercises are a GradleRIO project you open in WPILib VS Code. Download it here if a mentor has not already shared it.</p>` +
      `<div class="button-row"><button type="button" class="btn btn-primary" data-action="zip-student">Download exercises (.zip)</button>` +
      `<button type="button" class="btn btn-quiet" data-action="zip-mentor">Download with solutions (mentors)</button></div>`;
    Codebook.hooks.progressPage.forEach((fn) => fn(view));
  }

  function renderNotFound(path) {
    view.className = 'view';
    document.title = `Not found · ${INDEX.title}`;
    setToc([]);
    view.innerHTML = `<h1 class="page-title" id="page-title" tabindex="-1">No page at “${esc(path)}”</h1><p>The link may be from an older version of the course. Head back to the <a href="#/">course map</a> or search with <kbd>Ctrl</kbd> <kbd>K</kbd>.</p>`;
  }

  // ---------- enhancements inside rendered content ----------
  function enhance(root, entry) {
    $$('figure.code-block', root).forEach((figure) => {
      if ($('.copy-btn', figure)) return;
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = 'copy-btn';
      btn.textContent = 'Copy';
      btn.setAttribute('aria-label', 'Copy code');
      const cap = $('.code-cap', figure);
      if (cap) cap.appendChild(btn);
      else figure.prepend(btn);
    });
    $$('[role="tablist"]', root).forEach(wireTabs);
    $$('input[data-task]', root).forEach((box) => (box.checked = progress.task(box.dataset.task)));
    $$('input[data-exercise-done]', root).forEach((box) => (box.checked = progress.exercise(box.dataset.exerciseDone)));
    if (entry) {
      const complete = $('.complete-btn', root);
      if (complete) {
        complete.addEventListener('click', () => {
          const done = !progress.isDone(entry.route);
          progress.setDone(entry.route, done);
          complete.setAttribute('aria-pressed', String(done));
          complete.textContent = done ? 'Completed' : 'Mark lesson complete';
          complete.classList.toggle('btn-primary', !done);
          complete.classList.toggle('btn-quiet', done);
          toast(done ? 'Lesson marked complete' : 'Lesson marked not complete');
        });
      }
    }
  }

  function wireTabs(list) {
    const tabs = $$('[role="tab"]', list);
    const select = (tab, focus) => {
      tabs.forEach((t) => {
        const on = t === tab;
        t.setAttribute('aria-selected', String(on));
        t.tabIndex = on ? 0 : -1;
        document.getElementById(t.getAttribute('aria-controls')).hidden = !on;
      });
      if (focus) tab.focus();
    };
    tabs.forEach((tab, i) => {
      tab.addEventListener('click', () => select(tab, false));
      tab.addEventListener('keydown', (event) => {
        const next = { ArrowRight: i + 1, ArrowLeft: i - 1, Home: 0, End: tabs.length - 1 }[event.key];
        if (next === undefined) return;
        event.preventDefault();
        select(tabs[(next + tabs.length) % tabs.length], true);
      });
    });
  }

  document.addEventListener('click', async (event) => {
    const copy = event.target.closest('.copy-btn');
    if (copy) {
      const figure = copy.closest('figure');
      const text = $$('.line', figure).map((l) => l.textContent).join('\n');
      const ok = await copyText(text);
      copy.textContent = ok ? 'Copied' : 'Select';
      setTimeout(() => (copy.textContent = 'Copy'), 1600);
      if (!ok) toast('Copy was blocked here. Select the code and press Ctrl C.');
      return;
    }
    const reveal = event.target.closest('.ex-reveal');
    if (reveal) {
      const panel = reveal.closest('[role="tabpanel"]');
      reveal.closest('.ex-gate').hidden = true;
      $('.ex-solution', panel).hidden = false;
      return;
    }
    const navLink = event.target.closest('.nav-dialog a');
    if (navLink) closeNavDialog();
  });

  document.addEventListener('change', (event) => {
    const t = event.target;
    if (t.matches('input[data-task]')) progress.setTask(t.dataset.task, t.checked);
    if (t.matches('input[data-exercise-done]')) {
      progress.setExercise(t.dataset.exerciseDone, t.checked);
      $$(`input[data-exercise-done="${t.dataset.exerciseDone}"]`).forEach((b) => (b.checked = t.checked));
      if (t.checked) toast('Nice. Exercise marked as passing.');
    }
  });

  async function copyText(text) {
    try {
      await navigator.clipboard.writeText(text);
      return true;
    } catch (e) {
      const area = document.createElement('textarea');
      area.value = text;
      area.setAttribute('readonly', '');
      area.style.position = 'fixed';
      area.style.opacity = '0';
      document.body.appendChild(area);
      area.select();
      let ok = false;
      try {
        ok = document.execCommand('copy');
      } catch (err) {
        ok = false;
      }
      area.remove();
      return ok;
    }
  }

  let toastTimer = null;
  function toast(message) {
    const el = $('#toast');
    el.textContent = message;
    el.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => (el.hidden = true), 2600);
  }

  // ---------- dialogs & theme ----------
  const navDialog = $('#nav-dialog');
  const navOpen = $('.nav-open');
  function closeNavDialog() {
    if (navDialog.open) navDialog.close();
  }
  navOpen.addEventListener('click', () => {
    $('.nav-dialog-body', navDialog).innerHTML = navHtml(currentRoute);
    wireNav($('.nav-dialog-body', navDialog));
    navDialog.showModal();
    navOpen.setAttribute('aria-expanded', 'true');
  });
  navDialog.addEventListener('close', () => navOpen.setAttribute('aria-expanded', 'false'));
  $('.nav-close', navDialog).addEventListener('click', closeNavDialog);

  const themeBtn = $('.theme-toggle');
  const systemDark = window.matchMedia('(prefers-color-scheme: dark)');
  function applyTheme() {
    const pinned = store.get('theme', null);
    const root = document.documentElement;
    if (pinned) root.setAttribute('data-theme', pinned);
    else root.removeAttribute('data-theme');
    const meta = $('meta[name="color-scheme"]');
    if (meta) meta.content = pinned || 'light dark';
    const opposite = systemDark.matches ? 'light' : 'dark';
    themeBtn.setAttribute('aria-pressed', String(Boolean(pinned)));
    $('.theme-toggle-label', themeBtn).textContent = pinned ? 'Use the system theme' : `Use the ${opposite} theme`;
    themeBtn.title = pinned ? 'Use the system theme' : `Use the ${opposite} theme`;
  }
  if (!IS_ARTIFACT) {
    themeBtn.hidden = false;
    themeBtn.addEventListener('click', () => {
      const pinned = store.get('theme', null);
      store.set('theme', pinned ? null : systemDark.matches ? 'light' : 'dark');
      applyTheme();
    });
    systemDark.addEventListener('change', applyTheme);
    applyTheme();
  }

  // ---------- public API for quiz.js / extras.js ----------
  const Codebook = (window.Codebook = {
    index: INDEX,
    isArtifact: IS_ARTIFACT,
    versionQuery: VERSION_QUERY,
    store,
    progress,
    toast,
    esc,
    byRoute,
    flat,
    hooks: { page: [], progressPage: [] },
    currentRoute: () => currentRoute,
    use(name) {
      try {
        return window.claude && typeof window.claude.use === 'function' ? window.claude.use(name).catch(() => null) : Promise.resolve(null);
      } catch (e) {
        return Promise.resolve(null);
      }
    },
  });

  function start() {
    wireNav(sidebar);
    window.addEventListener('hashchange', route);
    refreshProgressUi();
    route();
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start);
  else setTimeout(start, 0);
})();
