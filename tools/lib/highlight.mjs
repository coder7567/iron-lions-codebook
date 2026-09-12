// Build-time syntax highlighting. Emits <span class="tk-*"> tokens so the site ships no highlighter.
// Token classes: kw keyword, ty type, fn call, str string, num number, com comment, ann annotation,
// lit literal, const UPPER_CASE, key JSON key, var shell variable, flag CLI flag, ins/del/hunk diff.

export const escapeHtml = (s) =>
  String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');

const JAVA_KEYWORDS = new Set(
  ('abstract assert boolean break byte case catch char class const continue default do double else enum ' +
    'extends final finally float for goto if implements import instanceof int interface long native new ' +
    'package private protected public return short static strictfp super switch synchronized this throw ' +
    'throws transient try void volatile while var record yield sealed permits').split(' ')
);
const GROOVY_KEYWORDS = new Set([...JAVA_KEYWORDS, 'def', 'as', 'in', 'it', 'task']);
const LITERALS = new Set(['true', 'false', 'null']);

const NUMBER = /(?:0[xX][\da-fA-F_]+[lL]?|0[bB][01_]+[lL]?|(?:\d[\d_]*)?\.?\d[\d_]*(?:[eE][+-]?\d+)?[fFdDlL]?)(?![\w$])/y;

function identClassifier(keywords) {
  return (word, code, end) => {
    if (keywords.has(word)) return 'kw';
    if (LITERALS.has(word)) return 'lit';
    if (/^[A-Z][A-Z0-9_]+$/.test(word)) return 'const';
    if (/^[A-Z]/.test(word)) return 'ty';
    if (/^\s*\(/.test(code.slice(end, end + 40))) return 'fn';
    return null;
  };
}

const RULES = {
  java: [
    { re: /\/\*[\s\S]*?\*\//y, cls: 'com' },
    { re: /\/\/[^\n]*/y, cls: 'com' },
    { re: /"""[\s\S]*?"""/y, cls: 'str' },
    { re: /"(?:\\.|[^"\\\n])*"/y, cls: 'str' },
    { re: /'(?:\\.|[^'\\\n])+'/y, cls: 'str' },
    { re: /@[A-Za-z_]\w*/y, cls: 'ann' },
    { re: NUMBER, cls: 'num' },
    { re: /[A-Za-z_$][\w$]*/y, cls: identClassifier(JAVA_KEYWORDS) },
  ],
  groovy: [
    { re: /\/\*[\s\S]*?\*\//y, cls: 'com' },
    { re: /\/\/[^\n]*/y, cls: 'com' },
    { re: /"(?:\\.|[^"\\\n])*"/y, cls: 'str' },
    { re: /'(?:\\.|[^'\\\n])*'/y, cls: 'str' },
    { re: NUMBER, cls: 'num' },
    { re: /[A-Za-z_$][\w$]*/y, cls: identClassifier(GROOVY_KEYWORDS) },
  ],
  json: [
    { re: /"(?:\\.|[^"\\\n])*"(?=\s*:)/y, cls: 'key' },
    { re: /"(?:\\.|[^"\\\n])*"/y, cls: 'str' },
    { re: /-?\d+(?:\.\d+)?(?:[eE][+-]?\d+)?/y, cls: 'num' },
    { re: /\b(?:true|false|null)\b/y, cls: 'lit' },
  ],
  shell: [
    { re: /(?<=^|\s)#[^\n]*/y, cls: 'com' },
    { re: /"(?:\\.|[^"\\])*"/y, cls: 'str' },
    { re: /'[^'\n]*'/y, cls: 'str' },
    { re: /\$\{[^}\n]*\}|\$env:\w+|\$[A-Za-z_]\w*/y, cls: 'var' },
    { re: /(?<=^|\s)--?[A-Za-z][\w-]*/y, cls: 'flag' },
    { re: /\b\d+(?:\.\d+)?\b/y, cls: 'num' },
    {
      re: /[A-Za-z_.\/\\][\w.\/\\:-]*/y,
      cls: (word, code, end) => {
        const start = end - word.length;
        const lineStart = code.lastIndexOf('\n', start - 1) + 1;
        return /^\s*$/.test(code.slice(lineStart, start)) ? 'fn' : null;
      },
    },
  ],
};

const ALIASES = {
  java: 'java', groovy: 'groovy', gradle: 'groovy', json: 'json', jsonc: 'json',
  bash: 'shell', sh: 'shell', shell: 'shell', console: 'shell', powershell: 'shell', ps: 'shell',
  ps1: 'shell', pwsh: 'shell', properties: 'shell', ini: 'shell',
};

function tokenize(code, rules) {
  const tokens = [];
  let i = 0;
  let plain = '';
  const flush = () => {
    if (plain) tokens.push({ cls: null, text: plain });
    plain = '';
  };
  outer: while (i < code.length) {
    for (const rule of rules) {
      rule.re.lastIndex = i;
      const m = rule.re.exec(code);
      if (m && m[0].length > 0) {
        const end = i + m[0].length;
        const cls = typeof rule.cls === 'function' ? rule.cls(m[0], code, end) : rule.cls;
        if (cls) {
          flush();
          tokens.push({ cls, text: m[0] });
        } else {
          plain += m[0];
        }
        i = end;
        continue outer;
      }
    }
    plain += code[i];
    i++;
  }
  flush();
  return tokens;
}

/** Returns one HTML string per source line (tokens that span lines are split and re-opened). */
export function highlightLines(code, lang = '') {
  const source = String(code).replace(/\r\n?/g, '\n');
  const key = ALIASES[String(lang).toLowerCase()];
  if (String(lang).toLowerCase() === 'diff') {
    return source.split('\n').map((line) => {
      const cls = line.startsWith('+') ? 'ins' : line.startsWith('-') ? 'del' : line.startsWith('@@') ? 'hunk' : null;
      return cls ? `<span class="tk-${cls}">${escapeHtml(line)}</span>` : escapeHtml(line);
    });
  }
  if (!key) return source.split('\n').map(escapeHtml);
  const lines = [''];
  for (const token of tokenize(source, RULES[key])) {
    token.text.split('\n').forEach((part, index) => {
      if (index > 0) lines.push('');
      if (part) lines[lines.length - 1] += token.cls ? `<span class="tk-${token.cls}">${escapeHtml(part)}</span>` : escapeHtml(part);
    });
  }
  return lines;
}

export function highlight(code, lang = '') {
  return highlightLines(code, lang).join('\n');
}
