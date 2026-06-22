/* api.js — wrapper centralizzato per le chiamate REST al backend AFAM Hub */
const API = (() => {
  const BASE = '/api';
  const TOKEN_KEY = 'afam_token';

  const getToken = () => localStorage.getItem(TOKEN_KEY);
  const setToken = (t) => localStorage.setItem(TOKEN_KEY, t);
  const clearToken = () => localStorage.removeItem(TOKEN_KEY);

  function headers(json = true) {
    const h = {};
    if (json) h['Content-Type'] = 'application/json';
    const t = getToken();
    if (t) h['Authorization'] = 'Bearer ' + t;
    return h;
  }

  async function request(method, path, body, isForm = false) {
    const opts = { method, headers: headers(!isForm) };
    if (body !== undefined && body !== null) {
      opts.body = isForm ? body : JSON.stringify(body);
    }
    const res = await fetch(BASE + path, opts);
    if (res.status === 401) {
      // sessione scaduta
      clearToken();
      if (!location.pathname.endsWith('index.html') && location.pathname !== '/') {
        location.href = 'index.html';
      }
    }
    let data = {};
    try { data = await res.json(); } catch (e) { data = {}; }
    return data;
  }

  return {
    getToken, setToken, clearToken,
    get: (p) => request('GET', p),
    post: (p, b) => request('POST', p, b),
    put: (p, b) => request('PUT', p, b),
    del: (p) => request('DELETE', p),
    postForm: (p, formData) => request('POST', p, formData, true),
  };
})();

/* Utility UI condivise */
function toast(msg, isError = false) {
  let el = document.getElementById('toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'toast';
    el.className = 'toast';
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.className = 'toast show' + (isError ? ' error' : '');
  clearTimeout(el._t);
  el._t = setTimeout(() => { el.className = 'toast' + (isError ? ' error' : ''); }, 2600);
}

function esc(s) {
  if (s === null || s === undefined) return '';
  return String(s).replace(/[&<>"']/g, c => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
  }[c]));
}

function svgIcon(p){return `<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="vertical-align:-3px">${p}</svg>`;}
const ICONS = {
  share: svgIcon('<circle cx="6" cy="12" r="2.5"/><circle cx="18" cy="6" r="2.5"/><circle cx="18" cy="18" r="2.5"/><path d="M8.2 10.8l7.6-3.6M8.2 13.2l7.6 3.6"/>'),
  folder: svgIcon('<path d="M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>'),
  edit: svgIcon('<path d="M4 20h4L18 10l-4-4L4 16z"/><path d="M13.5 6.5l4 4"/>'),
  trash: svgIcon('<path d="M4 7h16"/><path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/><path d="M6 7l1 13a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1l1-13"/>'),
  eye: svgIcon('<path d="M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12z"/><circle cx="12" cy="12" r="3"/>'),
  image: svgIcon('<rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="10" r="2"/><path d="M21 16l-5-5L5 21"/>'),
  video: svgIcon('<rect x="3" y="5" width="18" height="14" rx="2"/><path d="M10 9l5 3-5 3z"/>'),
  audio: svgIcon('<path d="M9 18V6l10-2v12"/><circle cx="6" cy="18" r="3"/><circle cx="16" cy="16" r="3"/>'),
  document: svgIcon('<path d="M6 2h8l6 6v14H6z"/><path d="M14 2v6h6"/><path d="M9 13h6M9 17h6"/>'),
  search: svgIcon('<circle cx="11" cy="11" r="7"/><path d="M21 21l-4-4"/>'),
  ban: svgIcon('<circle cx="12" cy="12" r="9"/><path d="M6 6l12 12"/>'),
  inbox: svgIcon('<path d="M3 13l3-8h12l3 8"/><path d="M3 13v6h18v-6"/><path d="M3 13h5l1 2h6l1-2h5"/>'),
};
const TIPO_ICON = { IMMAGINE: ICONS.image, VIDEO: ICONS.video, AUDIO: ICONS.audio, DOCUMENTO: ICONS.document };
