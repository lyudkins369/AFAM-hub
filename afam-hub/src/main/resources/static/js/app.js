/* app.js — logica dell'applicazione autenticata AFAM Hub */
(function () {
  if (!API.getToken()) { location.href = 'index.html'; return; }

  let sezioniCache = [];
  let pwdNuova = null;

  function goto(page) {
    document.querySelectorAll('.nav button').forEach(b => b.classList.toggle('active', b.dataset.page === page));
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById('page-' + page).classList.add('active');
    if (page === 'profilo') loadProfilo();
    if (page === 'risorse') loadRisorse();
    if (page === 'condivisione') loadPreset();
  }
  document.querySelectorAll('[data-page]').forEach(el => el.addEventListener('click', () => goto(el.dataset.page)));

  const modal = document.getElementById('modal');
  function openModal(title, bodyHTML, buttons = []) {
    document.getElementById('modal-title').textContent = title;
    document.getElementById('modal-body').innerHTML = bodyHTML;
    const foot = document.getElementById('modal-foot');
    foot.innerHTML = '';
    buttons.forEach(b => {
      const btn = document.createElement('button');
      btn.className = 'btn ' + (b.class || '');
      btn.textContent = b.label;
      btn.onclick = b.onClick;
      foot.appendChild(btn);
    });
    modal.classList.add('show');
  }
  function closeModal() { modal.classList.remove('show'); }
  document.getElementById('modal-close').onclick = closeModal;
  modal.addEventListener('click', e => { if (e.target === modal) closeModal(); });

  document.getElementById('btn-logout').onclick = async () => {
    await API.post('/auth/logout');
    API.clearToken();
    location.href = 'index.html';
  };

  /* ===== PROFILO ===== */
  async function loadProfilo() {
    const r = await API.get('/profilo');
    const view = document.getElementById('profilo-view');
    if (!r.successo) { view.innerHTML = `<div class="empty"><div class="icon">${ICONS.inbox}</div>${esc(r.messaggio || 'Nessun dato disponibile')}</div>`; return; }
    document.getElementById('greeting').textContent = 'Ciao, ' + (r.nomeUtente || r.nome || '');
    document.getElementById('home-hello').textContent = 'Ciao, ' + (r.nomeUtente || r.nome || '') + '!';
    const rows = [
      ['Nome Utente', r.nomeUtente], ['Nome', r.nome], ['Cognome', r.cognome],
      ['Email Personale', r.emailPersonale],
      ['Istituzione AFAM', r.istituzioneAfam], ['Corso di Studi', r.corsoStudi],
      ['Anno Accademico', r.annoAccademico], ['Anno di Corso', r.annoDiCorso],
      ['Area Disciplinare', r.areaDisciplinare],
    ];
    const foto = r.haFoto
      ? `<img src="/api/file/foto/${r.studenteId}?t=${Date.now()}" alt="Foto profilo" style="width:96px;height:96px;border-radius:50%;object-fit:cover;border:1px solid var(--border)">`
      : `<div style="width:96px;height:96px;border-radius:50%;background:var(--primary-light);display:flex;align-items:center;justify-content:center;font-size:34px">👤</div>`;
    view.innerHTML = `
      <div style="display:flex;align-items:center;gap:18px;margin-bottom:22px">
        ${foto}
        <div>
          <input type="file" id="foto-input" accept="image/*" style="display:none">
          <button class="btn btn-secondary btn-sm" id="btn-foto">Cambia foto</button>
        </div>
      </div>` +
      rows.map(([k, v]) =>
        `<div style="display:flex;justify-content:space-between;padding:11px 0;border-bottom:1px solid var(--border)">
           <span class="muted">${k}</span><strong>${v ? esc(v) : '<span class="muted">—</span>'}</strong></div>`
      ).join('');
    document.getElementById('btn-foto').onclick = () => document.getElementById('foto-input').click();
    document.getElementById('foto-input').onchange = async (e) => {
      const file = e.target.files[0]; if (!file) return;
      const fd = new FormData(); fd.append('file', file);
      const rr = await API.postForm('/profilo/foto', fd);
      toast(rr.messaggio || (rr.successo ? 'Foto aggiornata' : 'Errore'), !rr.successo);
      if (rr.successo) loadProfilo();
    };
    window._profilo = r;
  }

  document.getElementById('btn-edit-profilo').onclick = () => {
    const p = window._profilo || {};
    openModal('Modifica Profilo', `
      <div class="field"><label>Nome Utente</label><input id="m-nu" value="${esc(p.nomeUtente)}"></div>
      <div class="field"><label>Email Personale</label><input id="m-email" type="email" value="${esc(p.emailPersonale)}"></div>
      <div class="field"><label>Istituzione AFAM</label><input id="m-ist" value="${esc(p.istituzioneAfam)}"></div>
      <div class="field"><label>Corso di Studi</label><input id="m-corso" value="${esc(p.corsoStudi)}"></div>
      <div class="row">
        <div class="field"><label>Anno Accademico</label><input id="m-aa" value="${esc(p.annoAccademico)}" placeholder="2025/2026"></div>
        <div class="field"><label>Anno di Corso</label><input id="m-ac" type="number" min="1" value="${esc(p.annoDiCorso)}"></div>
      </div>
      <div class="field"><label>Area Disciplinare</label><input id="m-area" value="${esc(p.areaDisciplinare)}"></div>
    `, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Salva', onClick: async () => {
        const dati = {
          nomeUtente: val('m-nu'), emailPersonale: val('m-email'), istituzioneAfam: val('m-ist'),
          corsoStudi: val('m-corso'), annoAccademico: val('m-aa'), annoDiCorso: val('m-ac'), areaDisciplinare: val('m-area'),
        };
        const r = await API.put('/profilo', dati);
        toast(r.messaggio || (r.successo ? 'Salvato' : 'Errore'), !r.successo);
        if (r.successo) { closeModal(); loadProfilo(); }
      }},
    ]);
  };

  /* ===== RISORSE ===== */
  async function loadRisorse() {
    const r = await API.get('/risorse');
    const view = document.getElementById('risorse-view');
    sezioniCache = r.sezioni || [];
    if (!r.successo || sezioniCache.length === 0) {
      view.innerHTML = `<div class="empty"><div class="icon">${ICONS.folder}</div>Nessuna sezione. Crea la tua prima sezione per iniziare.</div>`;
      return;
    }
    view.innerHTML = sezioniCache.map(sez => renderSezione(sez)).join('');
    bindRisorseEvents();
  }

  function renderSezione(sez) {
    const cartelle = (sez.cartelle || []).map(c => `
      <div class="folder">
        <div class="folder-title">
          <span>${ICONS.folder} ${esc(c.nome)}</span>
          <span class="res-actions">
            <button class="btn btn-ghost btn-sm" data-act="edit-cartella" data-id="${c.id}" data-nome="${esc(c.nome)}">${ICONS.edit}</button>
            <button class="btn btn-ghost btn-sm" data-act="del-cartella" data-id="${c.id}">${ICONS.trash}</button>
          </span>
        </div>
        ${(c.risorse || []).map(renderRisorsa).join('') || '<p class="muted" style="font-size:13px">Cartella vuota</p>'}
      </div>`).join('');
    const libere = (sez.risorse || []).map(renderRisorsa).join('');
    return `<div class="section-block">
      <div class="section-header">
        <h3>${esc(sez.nome)}</h3>
        <span class="section-actions">
          <button class="btn btn-ghost btn-sm" data-act="new-cartella" data-id="${sez.id}">+ Cartella</button>
          <button class="btn btn-ghost btn-sm" data-act="edit-sezione" data-id="${sez.id}" data-nome="${esc(sez.nome)}">${ICONS.edit}</button>
          <button class="btn btn-ghost btn-sm" data-act="del-sezione" data-id="${sez.id}">${ICONS.trash}</button>
        </span>
      </div>
      ${cartelle}
      ${libere || (sez.cartelle && sez.cartelle.length ? '' : '<p class="muted" style="font-size:13px;margin-left:16px">Nessuna risorsa</p>')}
    </div>`;
  }

  function renderRisorsa(r) {
    return `<div class="res-item">
      <div class="meta">
        <span style="font-size:20px">${TIPO_ICON[r.tipo] || ICONS.document}</span>
        <div style="min-width:0">
          <div class="name">${esc(r.nome)} <span class="badge badge-tipo">${esc(r.tipo)}</span></div>
          ${r.descrizione ? `<div class="desc">${esc(r.descrizione)}</div>` : ''}
        </div>
      </div>
      <span class="res-actions">
        <a class="btn btn-ghost btn-sm" href="/api/file/${r.id}" target="_blank">${ICONS.eye}</a>
        <button class="btn btn-ghost btn-sm" data-act="edit-risorsa" data-id="${r.id}">${ICONS.edit}</button>
        <button class="btn btn-ghost btn-sm" data-act="del-risorsa" data-id="${r.id}">${ICONS.trash}</button>
      </span>
    </div>`;
  }

  function bindRisorseEvents() {
    document.querySelectorAll('#risorse-view [data-act]').forEach(el => {
      el.onclick = () => handleRisorsaAction(el.dataset.act, el.dataset.id, el.dataset.nome);
    });
  }

  function handleRisorsaAction(act, id, nome) {
    if (act === 'new-cartella') return modalNomeSemplice('Nuova Cartella', '', async (v) => { const r = await API.post(`/risorse/sezioni/${id}/cartelle`, { nome: v }); done(r); });
    if (act === 'edit-sezione') return modalNomeSemplice('Rinomina Sezione', nome, async (v) => { const r = await API.put(`/risorse/sezioni/${id}`, { nome: v }); done(r); });
    if (act === 'del-sezione') return confirmModal('Eliminare la sezione? Le risorse verranno spostate in "Altro - Uncategorized".', async () => { const r = await API.del(`/risorse/sezioni/${id}`); done(r); });
    if (act === 'edit-cartella') return modalNomeSemplice('Rinomina Cartella', nome, async (v) => { const r = await API.put(`/risorse/cartelle/${id}`, { nome: v }); done(r); });
    if (act === 'del-cartella') return confirmModal('Eliminare la cartella? Le risorse torneranno nella sezione.', async () => { const r = await API.del(`/risorse/cartelle/${id}`); done(r); });
    if (act === 'del-risorsa') return confirmModal('Eliminare questa risorsa?', async () => { const r = await API.del(`/risorse/${id}`); done(r); });
    if (act === 'edit-risorsa') return editRisorsa(id);
  }
  function done(r) { toast(r.messaggio || (r.successo ? 'Fatto' : 'Errore'), !r.successo); if (r.successo) { closeModal(); loadRisorse(); } }

  document.getElementById('btn-new-sezione').onclick = () => modalNomeSemplice('Nuova Sezione', '', async (v) => { const r = await API.post('/risorse/sezioni', { nome: v }); done(r); });

  document.getElementById('btn-new-risorsa').onclick = () => {
    const opts = sezioneOptions();
    openModal('Carica Risorsa', `
      <div class="field"><label>Nome</label><input id="up-nome" required></div>
      <div class="field"><label>File</label><input type="file" id="up-file" required></div>
      <div class="field"><label>Sezione</label><select id="up-sez">${opts}</select></div>
      <div class="field"><label>Cartella (opzionale)</label><select id="up-cart"><option value="">— Nessuna —</option></select></div>
    `, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Carica', onClick: uploadRisorsa },
    ]);
    bindCartelleSelect('up-sez', 'up-cart');
  };

  async function uploadRisorsa() {
    const file = document.getElementById('up-file').files[0];
    const nome = val('up-nome');
    if (!file || !nome) { toast('Inserisci nome e file', true); return; }
    const fd = new FormData();
    fd.append('file', file);
    fd.append('nome', nome);
    const sez = val('up-sez'); const cart = val('up-cart');
    if (sez) fd.append('categoriaId', sez);
    if (cart) fd.append('sottocategoriaId', cart);
    const r = await API.postForm('/risorse', fd);
    done(r);
  }

  function editRisorsa(id) {
    let res = null;
    sezioniCache.forEach(s => {
      (s.risorse || []).forEach(x => { if (String(x.id) === String(id)) res = x; });
      (s.cartelle || []).forEach(c => (c.risorse || []).forEach(x => { if (String(x.id) === String(id)) res = x; }));
    });
    res = res || {};
    const opts = sezioneOptions(res.categoriaId);
    openModal('Modifica Risorsa', `
      <div class="field"><label>Nome</label><input id="er-nome" value="${esc(res.nome)}"></div>
      <div class="field"><label>Descrizione</label><textarea id="er-desc">${esc(res.descrizione)}</textarea></div>
      <div class="field"><label>Sezione</label><select id="er-sez">${opts}</select></div>
      <div class="field"><label>Cartella (opzionale)</label><select id="er-cart"><option value="">— Nessuna —</option></select></div>
    `, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Salva', onClick: async () => {
        const dati = { nome: val('er-nome'), descrizione: val('er-desc') };
        const sez = val('er-sez'); const cart = val('er-cart');
        dati.categoriaId = sez ? Number(sez) : null;
        dati.sottocategoriaId = cart ? Number(cart) : null;
        const r = await API.put('/risorse/' + id, dati);
        done(r);
      }},
    ]);
    bindCartelleSelect('er-sez', 'er-cart', res.sottocategoriaId);
  }

  function sezioneOptions(selected) {
    return sezioniCache.map(s => `<option value="${s.id}" ${String(s.id) === String(selected) ? 'selected' : ''}>${esc(s.nome)}</option>`).join('');
  }
  function bindCartelleSelect(sezSelId, cartSelId, selectedCart) {
    const sezSel = document.getElementById(sezSelId);
    const cartSel = document.getElementById(cartSelId);
    function refresh() {
      const sez = sezioniCache.find(s => String(s.id) === String(sezSel.value));
      const cartelle = sez ? (sez.cartelle || []) : [];
      cartSel.innerHTML = '<option value="">— Nessuna —</option>' +
        cartelle.map(c => `<option value="${c.id}" ${String(c.id) === String(selectedCart) ? 'selected' : ''}>${esc(c.nome)}</option>`).join('');
    }
    sezSel.onchange = refresh;
    refresh();
  }

  /* ===== CONDIVISIONE / PRESET ===== */
  async function loadPreset() {
    const r = await API.get('/preset');
    window._presets = r.preset || [];
    const view = document.getElementById('preset-view');
    const presets = window._presets;
    if (presets.length === 0) {
      view.innerHTML = `<div class="empty"><div class="icon">${ICONS.share}</div>Nessun preset disponibile. Crea un preset per condividere le tue risorse.</div>`;
      return;
    }
    view.innerHTML = `<div class="grid grid-2">${presets.map(renderPreset).join('')}</div>`;
    document.querySelectorAll('#preset-view [data-pact]').forEach(el => {
      el.onclick = () => handlePresetAction(el.dataset.pact, el.dataset.id, el.dataset.nome);
    });
  }

  function renderPreset(p) {
    const pub = p.pubblico ? '<span class="badge badge-pub">Pubblico</span>' : '<span class="badge badge-priv">Privato</span>';
    const linkInfo = p.link
      ? `<div class="sub">🔗 ${esc(p.link.nomeLink || 'Link')} · ${p.link.aperto ? '<span class="badge badge-open">Aperto</span>' : '<span class="badge badge-closed">Non aperto</span>'}</div>`
      : '';
    return `<div class="card preset-card">
      <div class="top"><h3>${esc(p.nome)}</h3>${pub}</div>
      <div class="sub">${p.numeroRisorse} risorsa/e</div>
      ${linkInfo}
      <div class="actions">
        <button class="btn btn-secondary btn-sm" data-pact="risorse" data-id="${p.id}" data-nome="${esc(p.nome)}">Seleziona Risorse</button>
        <button class="btn btn-secondary btn-sm" data-pact="rename" data-id="${p.id}" data-nome="${esc(p.nome)}">Rinomina</button>
        <button class="btn btn-secondary btn-sm" data-pact="public" data-id="${p.id}">${p.pubblico ? 'Rendi Privato' : 'Rendi Pubblico'}</button>
        <button class="btn btn-secondary btn-sm" data-pact="link" data-id="${p.id}" data-nome="${esc(p.nome)}">Crea Link</button>
        <button class="btn btn-danger btn-sm" data-pact="del" data-id="${p.id}">Elimina</button>
      </div>
    </div>`;
  }

  function handlePresetAction(act, id, nome) {
    if (act === 'rename') return modalNomeSemplice('Rinomina Preset', nome, async (v) => { const r = await API.put(`/preset/${id}/nome`, { nome: v }); donePreset(r); });
    if (act === 'public') return (async () => {
      const p = (window._presets || []).find(x => String(x.id) === String(id));
      const nuovo = p ? !p.pubblico : true;
      const r = await API.put(`/preset/${id}/pubblico`, { pubblico: nuovo }); donePreset(r);
    })();
    if (act === 'del') return confirmModal('Eliminare questo preset?', async () => { const r = await API.del('/preset/' + id); donePreset(r); });
    if (act === 'risorse') return selezionaRisorse(id, nome);
    if (act === 'link') return creaLink(id, nome);
  }
  function donePreset(r) { toast(r.messaggio || (r.successo ? 'Fatto' : 'Errore'), !r.successo); if (r.successo) { closeModal(); loadPreset(); } }

  document.getElementById('btn-new-preset').onclick = () => modalNomeSemplice('Aggiungi Preset', '', async (v) => { const r = await API.post('/preset', { nome: v }); donePreset(r); });

  async function selezionaRisorse(presetId, nome) {
    const [risR, detR] = await Promise.all([API.get('/risorse'), API.get('/preset/' + presetId)]);
    const incluse = new Set((detR.preset && detR.preset.risorse ? detR.preset.risorse : []).map(x => x.id));
    const tutte = [];
    (risR.sezioni || []).forEach(s => {
      (s.risorse || []).forEach(x => tutte.push({ ...x, sez: s.nome }));
      (s.cartelle || []).forEach(c => (c.risorse || []).forEach(x => tutte.push({ ...x, sez: s.nome + ' / ' + c.nome })));
    });
    const body = tutte.length === 0
      ? '<p class="muted">Nessuna risorsa disponibile.</p>'
      : `<div class="check-list">${tutte.map(r => `
          <label class="check-row">
            <input type="checkbox" value="${r.id}" ${incluse.has(r.id) ? 'checked' : ''}>
            <span>${TIPO_ICON[r.tipo] || ICONS.document} <strong>${esc(r.nome)}</strong> <span class="muted">· ${esc(r.sez)}</span></span>
          </label>`).join('')}</div>`;
    openModal(`Risorse di "${nome}"`, body, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Salva', onClick: async () => {
        const ids = [...document.querySelectorAll('#modal-body input:checked')].map(c => Number(c.value));
        const r = await API.put(`/preset/${presetId}/risorse`, { risorseIds: ids });
        donePreset(r);
      }},
    ]);
  }

  async function creaLink(presetId, nome) {
    openModal('Crea Link', `
      <p class="muted" style="margin-bottom:14px">Genera un link condivisibile per il preset <strong>${esc(nome)}</strong>.</p>
      <div class="field"><label>Nome del Link</label><input id="ln-nome" placeholder="es. Candidatura Conservatorio"></div>
      <div id="ln-result"></div>
    `, [
      { label: 'Chiudi', class: 'btn-secondary', onClick: () => { closeModal(); loadPreset(); } },
      { label: 'Salva e Copia Link', onClick: async () => {
        const nomeLink = val('ln-nome');
        if (!nomeLink) { toast('Inserisci il nome del link', true); return; }
        const r = await API.post('/link', { presetId: Number(presetId), nomeLink });
        if (!r.successo) { toast(r.messaggio || 'Errore', true); return; }
        const fullUrl = location.origin + '/view.html?link=' + r.url;
        document.getElementById('ln-result').innerHTML = `
          <div class="alert show alert-success">${esc(r.messaggio)}</div>
          <label style="font-size:13px;font-weight:600">URL condivisibile</label>
          <div class="copy-box"><input id="ln-url" readonly value="${esc(fullUrl)}">
          <button class="btn btn-sm" id="ln-copy">Copia</button></div>`;
        try { await navigator.clipboard.writeText(fullUrl); toast('Link copiato negli appunti'); } catch (e) {}
        document.getElementById('ln-copy').onclick = () => { document.getElementById('ln-url').select(); navigator.clipboard.writeText(fullUrl); toast('Copiato'); };
      }},
    ]);
  }

  document.getElementById('btn-my-links').onclick = async () => {
    const r = await API.get('/link');
    const links = r.link || [];
    const body = links.length === 0
      ? '<p class="muted">Nessun link disponibile.</p>'
      : links.map(l => `
        <div class="res-item">
          <div class="meta"><div>
            <div class="name">${esc(l.nomeLink || 'Senza nome')}</div>
            <div class="desc">Preset: ${esc(l.presetNome || '—')}</div>
          </div></div>
          <span>${l.aperto ? '<span class="badge badge-open">Aperto</span>' : '<span class="badge badge-closed">Non aperto</span>'}</span>
        </div>`).join('');
    openModal('I miei Link', body, [{ label: 'Chiudi', class: 'btn-secondary', onClick: closeModal }]);
  };

  /* ===== MODIFICA PASSWORD ===== */
  const pwdAlert = document.getElementById('pwd-alert');
  function showPwd(msg, ok) { pwdAlert.textContent = msg; pwdAlert.className = 'alert show ' + (ok ? 'alert-success' : 'alert-error'); }

  document.getElementById('form-pwd').addEventListener('submit', async e => {
    e.preventDefault();
    const np = document.getElementById('pwd-new').value;
    const nc = document.getElementById('pwd-conf').value;
    if (np.length < 6) { showPwd('La password deve avere almeno 6 caratteri'); return; }
    if (np !== nc) { showPwd('Le password non coincidono'); return; }
    pwdNuova = { np, nc };
    const r = await API.post('/auth/change-password/request-2fa');
    if (!r.successo) { showPwd(r.messaggio || 'Errore'); return; }
    openModal('Verifica 2FA', `
      <p class="muted" style="margin-bottom:14px">Inserisci il codice di verifica.<br><em>(Demo: 123456)</em></p>
      <div class="field"><label>Codice 2FA</label><input id="cp-code" maxlength="6" placeholder="123456"></div>
    `, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Conferma', onClick: async () => {
        if (val('cp-code') !== '123456') { toast('Codice non valido', true); return; }
        const rr = await API.post('/auth/change-password', { nuovaPassword: pwdNuova.np, confermaPassword: pwdNuova.nc });
        toast(rr.messaggio || (rr.successo ? 'Aggiornata' : 'Errore'), !rr.successo);
        if (rr.successo) { closeModal(); document.getElementById('form-pwd').reset(); showPwd('Password aggiornata correttamente', true); }
      }},
    ]);
  });

  function val(id) { const el = document.getElementById(id); return el ? el.value.trim() : ''; }
  function modalNomeSemplice(titolo, valore, onSave) {
    openModal(titolo, `<div class="field"><label>Nome</label><input id="ms-nome" value="${esc(valore)}" autofocus></div>`, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Conferma', onClick: () => { const v = val('ms-nome'); if (!v) { toast('Inserisci un nome', true); return; } onSave(v); } },
    ]);
  }
  function confirmModal(testo, onYes) {
    openModal('Conferma', `<p>${esc(testo)}</p>`, [
      { label: 'Annulla', class: 'btn-secondary', onClick: closeModal },
      { label: 'Conferma', class: 'btn-danger', onClick: onYes },
    ]);
  }

  loadProfilo();
})();
