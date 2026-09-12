// Quiz mini-language → question data → accessible HTML. Syntax documented in PLAN.md and course/reference/maintaining.md.
import { escapeHtml } from './highlight.mjs';

const TYPE_PREFIXES = [
  ['?order', 'order'],
  ['?text', 'text'],
  ['?code', 'code'],
  ['?num', 'num'],
  ['?tf', 'tf'],
  ['??', 'multi'],
  ['?', 'single'],
];

export function parseQuiz(src, where = 'quiz') {
  const lines = String(src).replace(/\r\n?/g, '\n').split('\n');
  const questions = [];
  let q = null;
  let phase = 'prompt';
  let inFence = false;
  const fail = (lineNo, message) => {
    throw new Error(`${where}: quiz line ${lineNo + 1}: ${message}`);
  };

  const begin = (line, lineNo) => {
    for (const [prefix, type] of TYPE_PREFIXES) {
      if (line === prefix || line.startsWith(prefix + ' ')) {
        return { type, line: lineNo, promptLines: [line.slice(prefix.length).trim()], options: [], items: [], answer: null, explainLines: [] };
      }
    }
    return null;
  };

  lines.forEach((raw, lineNo) => {
    const line = raw.replace(/\s+$/, '');
    if (q && phase === 'prompt' && /^\s*(```|~~~)/.test(line)) {
      inFence = !inFence;
      q.promptLines.push(raw);
      return;
    }
    if (q && inFence) {
      q.promptLines.push(raw);
      return;
    }
    if (line.startsWith('?')) {
      const next = begin(line, lineNo);
      if (next) {
        if (q) questions.push(finish(q, fail));
        q = next;
        phase = 'prompt';
        return;
      }
    }
    if (!q) {
      if (line.trim()) fail(lineNo, 'text outside a question (questions start with "?")');
      return;
    }
    let m;
    if ((q.type === 'single' || q.type === 'multi') && (m = /^([+-]) (.+)$/.exec(line))) {
      phase = 'body';
      q.options.push({ text: m[2], correct: m[1] === '+', feedback: '' });
      return;
    }
    if (q.options.length && (m = /^\s+~ (.+)$/.exec(line))) {
      const option = q.options[q.options.length - 1];
      option.feedback = option.feedback ? `${option.feedback} ${m[1]}` : m[1];
      return;
    }
    if ((m = /^= (.*)$/.exec(line))) {
      phase = 'body';
      q.answer = m[1].trim();
      return;
    }
    if (q.type === 'order' && (m = /^\d+\. (.+)$/.exec(line))) {
      phase = 'body';
      q.items.push(m[1]);
      return;
    }
    if ((m = /^>\s?(.*)$/.exec(line))) {
      phase = 'body';
      q.explainLines.push(m[1]);
      return;
    }
    if (phase === 'prompt') {
      q.promptLines.push(raw);
      return;
    }
    if (line.trim()) fail(lineNo, `unexpected line after the options: "${line}"`);
  });
  if (q) questions.push(finish(q, fail));
  if (!questions.length) throw new Error(`${where}: quiz has no questions`);
  return questions;
}

function finish(q, fail) {
  const out = { type: q.type, prompt: q.promptLines.join('\n').trim(), explain: q.explainLines.join('\n').trim() };
  if (!out.prompt) fail(q.line, 'empty question prompt');
  switch (q.type) {
    case 'single':
    case 'multi': {
      const correct = q.options.filter((o) => o.correct).length;
      if (q.options.length < 2) fail(q.line, 'needs at least two options');
      if (q.type === 'single' && correct !== 1) fail(q.line, `single-choice needs exactly one "+" option (found ${correct})`);
      if (q.type === 'multi' && correct < 1) fail(q.line, 'multi-select needs at least one "+" option');
      out.options = q.options;
      break;
    }
    case 'tf':
      if (!/^(true|false)$/i.test(q.answer ?? '')) fail(q.line, 'true/false needs "= true" or "= false"');
      out.answer = /^true$/i.test(q.answer);
      break;
    case 'text':
    case 'code':
      if (!q.answer) fail(q.line, 'needs an "= answer" line');
      out.accept = q.type === 'code' ? [q.answer] : q.answer.split(/\s+\|\s+/).map((a) => a.trim()).filter(Boolean);
      for (const accepted of out.accept) {
        const re = /^\/(.+)\/([a-z]*)$/.exec(accepted);
        if (re) {
          try {
            new RegExp(re[1], re[2]);
          } catch {
            fail(q.line, `invalid regular expression ${accepted}`);
          }
        }
      }
      break;
    case 'num': {
      const m = /^(-?\d*\.?\d+(?:e[+-]?\d+)?)\s*(?:(?:±|\+-|\+\/-)\s*(\d*\.?\d+(?:e[+-]?\d+)?))?\s*(.*)$/i.exec(q.answer ?? '');
      if (!m) fail(q.line, 'numeric needs "= value ± tolerance unit"');
      out.value = Number(m[1]);
      out.tolerance = m[2] !== undefined ? Number(m[2]) : Math.max(Math.abs(out.value) * 0.01, 1e-9);
      out.unit = m[3].trim();
      break;
    }
    case 'order':
      if (q.items.length < 2) fail(q.line, 'ordering needs at least two numbered items');
      out.items = q.items;
      break;
  }
  return out;
}

function hash(text) {
  let h = 2166136261;
  for (let i = 0; i < text.length; i++) h = Math.imul(h ^ text.charCodeAt(i), 16777619);
  return h >>> 0;
}

function seededOrder(length, seed) {
  const order = Array.from({ length }, (_, i) => i);
  let state = seed || 1;
  for (let i = length - 1; i > 0; i--) {
    state = Math.imul(state ^ (state >>> 15), 2246822519) >>> 0;
    const j = state % (i + 1);
    [order[i], order[j]] = [order[j], order[i]];
  }
  if (order.every((v, i) => v === i)) order.push(order.shift());
  return order;
}

const encodeKey = (key) => Buffer.from(JSON.stringify(key), 'utf8').toString('base64');

function renderQuestion(q, index, total, { md, mdInline, idBase }) {
  const id = `${idBase}-q${index + 1}`;
  const parts = [];
  let key;
  const legend = `<legend class="q-legend">Question ${index + 1}<span class="visually-hidden"> of ${total}</span></legend>`;
  const prompt = `<div class="q-prompt" id="${id}-prompt">${md(q.prompt)}</div>`;

  if (q.type === 'single' || q.type === 'multi') {
    const inputType = q.type === 'single' ? 'radio' : 'checkbox';
    key = { t: q.type, c: q.options.map((o, i) => (o.correct ? i : -1)).filter((i) => i >= 0) };
    parts.push(`<div class="q-choices">`);
    q.options.forEach((option, i) => {
      parts.push(
        `<label class="q-choice" for="${id}-o${i}"><input type="${inputType}" id="${id}-o${i}" name="${id}" value="${i}">` +
          `<span class="q-choice-text">${mdInline(option.text)}</span><span class="q-mark" aria-hidden="true"></span></label>`
      );
    });
    parts.push(`</div>`);
    if (q.type === 'multi') parts.unshift(`<p class="q-hint">Select every answer that applies.</p>`);
    q.options.forEach((option, i) => {
      if (option.feedback) parts.push(`<template class="q-fb" data-i="${i}">${mdInline(option.feedback)}</template>`);
    });
  } else if (q.type === 'tf') {
    key = { t: 'tf', a: q.answer };
    parts.push(
      `<div class="q-choices q-choices-row">` +
        `<label class="q-choice" for="${id}-t"><input type="radio" id="${id}-t" name="${id}" value="true"><span class="q-choice-text">True</span><span class="q-mark" aria-hidden="true"></span></label>` +
        `<label class="q-choice" for="${id}-f"><input type="radio" id="${id}-f" name="${id}" value="false"><span class="q-choice-text">False</span><span class="q-mark" aria-hidden="true"></span></label>` +
        `</div>`
    );
  } else if (q.type === 'text' || q.type === 'code') {
    key = { t: q.type, a: q.accept };
    const mono = q.type === 'code' ? ' q-input-mono' : '';
    parts.push(
      `<div class="q-field"><label class="q-input-label" for="${id}-in">${q.type === 'code' ? 'Exact output' : 'Your answer'}</label>` +
        `<input class="q-input${mono}" type="text" id="${id}-in" name="${id}" autocomplete="off" autocapitalize="off" spellcheck="false"></div>`
    );
  } else if (q.type === 'num') {
    key = { t: 'num', v: q.value, tol: q.tolerance };
    parts.push(
      `<div class="q-field"><label class="q-input-label" for="${id}-in">Your answer${q.unit ? ` (${escapeHtml(q.unit)})` : ''}</label>` +
        `<span class="q-num-wrap"><input class="q-input q-input-num" type="text" inputmode="decimal" id="${id}-in" name="${id}" autocomplete="off">` +
        `${q.unit ? `<span class="q-unit" aria-hidden="true">${escapeHtml(q.unit)}</span>` : ''}</span></div>`
    );
  } else if (q.type === 'order') {
    const shown = seededOrder(q.items.length, hash(id));
    key = { t: 'order', n: q.items.length };
    parts.push(`<p class="q-hint">Use the arrow buttons to put the steps in order, first at the top.</p><ol class="q-order">`);
    shown.forEach((itemIndex) => {
      const label = escapeHtml(q.items[itemIndex].replace(/[`*_]/g, ''));
      parts.push(
        `<li class="q-order-item" data-i="${itemIndex}"><span class="q-order-text">${mdInline(q.items[itemIndex])}</span>` +
          `<span class="q-order-moves"><button type="button" class="q-move" data-dir="-1" aria-label="Move up: ${label}">&#8593;</button>` +
          `<button type="button" class="q-move" data-dir="1" aria-label="Move down: ${label}">&#8595;</button></span></li>`
      );
    });
    parts.push(`</ol>`);
  }

  return (
    `<fieldset class="q" data-type="${q.type}" data-key="${encodeKey(key)}" aria-describedby="${id}-prompt">` +
    legend +
    prompt +
    parts.join('') +
    `<div class="q-feedback" aria-live="polite"></div>` +
    (q.explain ? `<template class="q-explain">${md(q.explain)}</template>` : '') +
    `</fieldset>`
  );
}

/**
 * @param questions parsed questions
 * @param opts {md, mdInline, idBase, quizKey, title, mode: 'quiz'|'exam', pass}
 */
export function renderQuiz(questions, opts) {
  const { idBase, quizKey, mode = 'quiz', pass = 80 } = opts;
  const total = questions.length;
  const titleId = `${idBase}-title`;
  if (mode === 'exam') {
    const title = opts.title || 'Unit test';
    return (
      `<section class="quiz exam" data-quiz="${escapeHtml(quizKey)}" data-pass="${pass}" data-total="${total}" aria-labelledby="${titleId}">` +
      `<div class="quiz-head"><h2 class="quiz-title" id="${titleId}">${escapeHtml(title)}</h2>` +
      `<p class="quiz-sub">${total} questions · pass mark ${pass}% · nothing is graded until you submit</p></div>` +
      `<form class="exam-form" novalidate>` +
      questions.map((q, i) => renderQuestion(q, i, total, opts)).join('') +
      `<div class="exam-actions"><button type="submit" class="btn btn-primary">Submit unit test</button>` +
      `<button type="button" class="btn btn-quiet exam-retake" hidden>Retake</button></div>` +
      `</form><div class="exam-result" aria-live="polite" hidden></div></section>`
    );
  }
  const title = opts.title || 'Check your understanding';
  return (
    `<section class="quiz" data-quiz="${escapeHtml(quizKey)}" data-total="${total}" aria-labelledby="${titleId}">` +
    `<div class="quiz-head"><h2 class="quiz-title" id="${titleId}">${escapeHtml(title)}</h2>` +
    `<p class="quiz-sub">${total} question${total === 1 ? '' : 's'} · checked instantly · results stay on this device</p></div>` +
    questions
      .map(
        (q, i) =>
          `<form class="q-form" novalidate>${renderQuestion(q, i, total, opts)}` +
          `<div class="q-actions"><button type="submit" class="btn btn-primary">Check answer</button>` +
          `<button type="button" class="btn btn-quiet q-retry" hidden>Try again</button></div></form>`
      )
      .join('') +
    `<p class="quiz-score" aria-live="polite"></p></section>`
  );
}
