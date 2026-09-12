/* Quiz and unit-test engine. Questions are rendered at build time; this grades them and saves results. */
(function () {
  'use strict';
  const C = window.Codebook;
  const $ = (sel, root) => root.querySelector(sel);
  const $$ = (sel, root) => Array.from(root.querySelectorAll(sel));

  const decodeKey = (fs) => JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(fs.dataset.key), (c) => c.charCodeAt(0))));
  const squash = (s) => String(s).trim().replace(/\s+/g, ' ');

  function answered(fs) {
    const type = fs.dataset.type;
    if (type === 'single' || type === 'multi' || type === 'tf') return $$('input:checked', fs).length > 0;
    if (type === 'order') return true;
    const input = $('.q-input', fs);
    return Boolean(input && input.value.trim());
  }

  function grade(fs) {
    const key = decodeKey(fs);
    switch (key.t) {
      case 'single':
      case 'multi': {
        const picked = $$('input:checked', fs).map((i) => Number(i.value)).sort((a, b) => a - b);
        return picked.length === key.c.length && picked.every((v, i) => v === key.c[i]);
      }
      case 'tf': {
        const picked = $('input:checked', fs);
        return Boolean(picked) && picked.value === String(key.a);
      }
      case 'text': {
        const value = $('.q-input', fs).value.trim();
        return key.a.some((accepted) => {
          const re = /^\/(.+)\/([a-z]*)$/.exec(accepted);
          if (re) return new RegExp(re[1], re[2]).test(value);
          return squash(value).toLowerCase() === squash(accepted).toLowerCase();
        });
      }
      case 'code':
        return squash($('.q-input', fs).value) === squash(key.a[0]);
      case 'num': {
        const raw = $('.q-input', fs).value.trim().replace(/,/g, '').replace(/\s+/g, '');
        const value = Number(raw);
        return raw !== '' && Number.isFinite(value) && Math.abs(value - key.v) <= key.tol + 1e-12;
      }
      case 'order':
        return $$('.q-order-item', fs).every((li, i) => Number(li.dataset.i) === i);
      default:
        return false;
    }
  }

  function clearMarks(fs) {
    delete fs.dataset.result;
    delete fs.dataset.locked;
    $$('.is-correct, .is-wrong', fs).forEach((el) => el.classList.remove('is-correct', 'is-wrong'));
    $$('.q-mark', fs).forEach((m) => (m.textContent = ''));
    $('.q-feedback', fs).innerHTML = '';
    delete $('.q-feedback', fs).dataset.result;
    $$('input, .q-move', fs).forEach((el) => (el.disabled = false));
    const input = $('.q-input', fs);
    if (input) input.removeAttribute('aria-invalid');
  }

  function markChoices(fs, reveal) {
    const key = decodeKey(fs);
    if (key.t === 'single' || key.t === 'multi') {
      $$('.q-choice', fs).forEach((label, i) => {
        const input = $('input', label);
        const isAnswer = key.c.includes(i);
        const mark = $('.q-mark', label);
        if (input.checked && !isAnswer) {
          label.classList.add('is-wrong');
          mark.textContent = 'Your pick';
        } else if (isAnswer && (reveal || input.checked)) {
          label.classList.add('is-correct');
          mark.textContent = input.checked ? 'Correct' : 'Answer';
        }
      });
    } else if (key.t === 'tf') {
      $$('.q-choice', fs).forEach((label) => {
        const input = $('input', label);
        const isAnswer = input.value === String(key.a);
        const mark = $('.q-mark', label);
        if (input.checked && !isAnswer) {
          label.classList.add('is-wrong');
          mark.textContent = 'Your pick';
        } else if (isAnswer && (reveal || input.checked)) {
          label.classList.add('is-correct');
          mark.textContent = input.checked ? 'Correct' : 'Answer';
        }
      });
    } else if (key.t === 'order') {
      $$('.q-order-item', fs).forEach((li, i) => li.classList.add(Number(li.dataset.i) === i ? 'is-correct' : 'is-wrong'));
    }
  }

  function correctAnswerText(fs) {
    const key = decodeKey(fs);
    if (key.t === 'text') return key.a.filter((a) => !/^\/.+\/[a-z]*$/.test(a))[0] || '';
    if (key.t === 'code') return key.a[0];
    if (key.t === 'num') return `${key.v} (within ±${key.tol})`;
    return '';
  }

  function feedback(fs, correct, { reveal, attempt }) {
    const box = $('.q-feedback', fs);
    const explain = $('template.q-explain', fs);
    const parts = [];
    let verdict = correct ? 'Correct.' : reveal ? 'Not this time. Here is the answer.' : 'Not quite. Try again.';
    if (!correct && attempt === 'exam') verdict = answered(fs) ? 'Incorrect.' : 'Not answered.';
    parts.push(`<p class="q-verdict">${verdict}</p>`);
    $$('input:checked', fs).forEach((input) => {
      const fb = $(`template.q-fb[data-i="${input.value}"]`, fs);
      if (fb) parts.push(`<p>${fb.innerHTML}</p>`);
    });
    if (!correct && reveal) {
      const text = correctAnswerText(fs);
      if (text) parts.push(`<p>Accepted answer: <code>${C.esc(text)}</code></p>`);
    }
    if ((correct || reveal) && explain) parts.push(explain.innerHTML);
    box.dataset.result = correct ? 'correct' : 'wrong';
    box.innerHTML = parts.join('');
    const input = $('.q-input', fs);
    if (input) input.setAttribute('aria-invalid', String(!correct));
  }

  function lock(fs) {
    fs.dataset.locked = 'true';
    $$('input, .q-move', fs).forEach((el) => (el.disabled = true));
  }

  // ---------- per-lesson quizzes ----------
  function wireQuiz(section) {
    const quizKey = section.dataset.quiz;
    const forms = $$('.q-form', section);
    const scoreEl = $('.quiz-score', section);
    const saved = C.progress.quiz(quizKey) || { results: {} };
    const updateScore = () => {
      const results = Object.values(saved.results);
      const right = results.filter((r) => r.correct).length;
      scoreEl.textContent = results.length ? `${right} of ${forms.length} answered correctly${results.length < forms.length ? ' so far' : ''}.` : '';
    };
    updateScore();

    forms.forEach((form, index) => {
      const fs = $('fieldset.q', form);
      const retry = $('.q-retry', form);
      const submit = $('button[type="submit"]', form);
      let attempts = 0;
      form.addEventListener('submit', (event) => {
        event.preventDefault();
        if (fs.dataset.locked) return;
        if (!answered(fs)) {
          const box = $('.q-feedback', fs);
          box.dataset.result = 'wrong';
          box.innerHTML = '<p class="q-verdict">Choose or type an answer first.</p>';
          return;
        }
        attempts += 1;
        clearMarks(fs);
        const correct = grade(fs);
        const reveal = correct || attempts >= 2;
        markChoices(fs, reveal);
        feedback(fs, correct, { reveal, attempt: attempts });
        if (reveal) {
          lock(fs);
          submit.hidden = true;
          retry.hidden = false;
          retry.textContent = 'Reset question';
        }
        const previous = saved.results[index];
        saved.results[index] = { correct: correct || Boolean(previous && previous.correct), attempts };
        C.progress.setQuiz(quizKey, saved);
        updateScore();
      });
      retry.addEventListener('click', () => {
        attempts = 0;
        clearMarks(fs);
        $$('input[type="radio"], input[type="checkbox"]', fs).forEach((i) => (i.checked = false));
        const input = $('.q-input', fs);
        if (input) input.value = '';
        retry.hidden = true;
        submit.hidden = false;
        (input || $('input', fs) || submit).focus();
      });
    });
  }

  // ---------- unit tests ----------
  function wireExam(section) {
    const quizKey = section.dataset.quiz;
    const pass = Number(section.dataset.pass || 80);
    const form = $('.exam-form', section);
    const result = $('.exam-result', section);
    const retake = $('.exam-retake', section);
    const submit = $('button[type="submit"]', form);
    const questions = $$('fieldset.q', form);
    const route = quizKey.split('#')[0];

    const showRecord = (record, fresh) => {
      if (!record) return;
      result.hidden = false;
      const badge = record.passed ? '<span class="exam-badge pass">Passed</span>' : '<span class="exam-badge fail">Not yet passed</span>';
      result.innerHTML =
        `<p class="exam-score"><span class="exam-score-num">${fresh ? record.last : record.best}%</span>${badge}</p>` +
        `<p>${fresh ? `You scored ${record.last}% (${record.right} of ${questions.length}).` : 'Your best score so far.'} Best: ${record.best}%. Pass mark: ${pass}%.</p>` +
        (fresh && !record.passedNow ? '<p>Review the explanations under each question, revisit those lessons, then retake the test.</p>' : '');
    };
    showRecord(C.progress.exam(quizKey), false);

    form.addEventListener('submit', (event) => {
      event.preventDefault();
      const unanswered = questions.filter((fs) => !answered(fs)).length;
      if (unanswered && !form.dataset.confirmed) {
        form.dataset.confirmed = 'true';
        result.hidden = false;
        result.innerHTML = `<p><strong>${unanswered} question${unanswered === 1 ? ' is' : 's are'} unanswered.</strong> Unanswered questions count as wrong. Press “Submit unit test” again to grade anyway.</p>`;
        result.scrollIntoView({ block: 'nearest' });
        return;
      }
      let right = 0;
      questions.forEach((fs) => {
        clearMarks(fs);
        const correct = answered(fs) && grade(fs);
        if (correct) right += 1;
        markChoices(fs, true);
        feedback(fs, correct, { reveal: true, attempt: 'exam' });
        lock(fs);
      });
      const score = Math.round((right / questions.length) * 100);
      const previous = C.progress.exam(quizKey);
      const passedNow = score >= pass;
      const record = {
        last: score,
        right,
        best: Math.max(score, previous ? previous.best : 0),
        passed: passedNow || Boolean(previous && previous.passed),
        passedNow,
        at: Date.now(),
      };
      C.progress.setExam(quizKey, record);
      if (record.passed) C.progress.setDone(route, true);
      submit.hidden = true;
      retake.hidden = false;
      showRecord(record, true);
      result.scrollIntoView({ block: 'start' });
      C.toast(passedNow ? `Passed with ${score}%` : `Scored ${score}%. Pass mark is ${pass}%.`);
    });

    retake.addEventListener('click', () => {
      delete form.dataset.confirmed;
      questions.forEach((fs) => {
        clearMarks(fs);
        $$('input[type="radio"], input[type="checkbox"]', fs).forEach((i) => (i.checked = false));
        const input = $('.q-input', fs);
        if (input) input.value = '';
      });
      retake.hidden = true;
      submit.hidden = false;
      result.hidden = true;
      questions[0].scrollIntoView({ block: 'start' });
    });
  }

  // ---------- ordering questions ----------
  document.addEventListener('click', (event) => {
    const move = event.target.closest('.q-move');
    if (!move || move.disabled) return;
    const item = move.closest('.q-order-item');
    const list = item.parentElement;
    const dir = Number(move.dataset.dir);
    const sibling = dir < 0 ? item.previousElementSibling : item.nextElementSibling;
    if (!sibling) return;
    if (dir < 0) list.insertBefore(item, sibling);
    else list.insertBefore(sibling, item);
    move.focus();
    const position = Array.from(list.children).indexOf(item) + 1;
    const box = item.closest('fieldset').querySelector('.q-feedback');
    box.removeAttribute('data-result');
    box.innerHTML = `<p class="visually-hidden">Moved to position ${position}.</p>`;
  });

  C.hooks.page.push(({ view }) => {
    $$('section.quiz:not(.exam)', view).forEach(wireQuiz);
    $$('section.quiz.exam', view).forEach(wireExam);
  });
})();
