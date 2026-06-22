/* view.js — Area Esaminatore Esterno: ricerca studenti (profili consigliati) */
(function () {
  const results = document.getElementById('results');
  const params = new URLSearchParams(location.search);
  const linkUrl = params.get('link');

  window.avatarFail = function (el) { el.style.display = 'none'; el.nextElementSibling.style.display = 'flex'; };

  if (linkUrl) {
    document.getElementById('search-bar').style.display = 'none';
    apriLink(linkUrl);
    return;
  }

  document.getElementById('btn-search').onclick = cerca;
  document.getElementById('q').addEventListener('keydown', e => { if (e.key === 'Enter') cerca(); });
  cerca();

  function iniziali(nome) {
    return (nome || '?').split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0].toUpperCase()).join('');
  }

  function avatar(id, nome, size) {
    const s = size || 64;
    return `<div class="avatar-wrap" style="width:${s}px;height:${s}px">
      <img class="avatar-img" src="/api/file/foto/${id}" alt="" onerror="avatarFail(this)">
      <div class="avatar-fallback" style="display:none;font-size:${Math.round(s / 2.6)}px">${esc(iniziali(nome))}</div>
    </div>`;
  }

  async function cerca() {
    const q = document.getElementById('q').value.trim();
    results.innerHTML = '<div class="spinner">Ricerca in corso…</div>';
    const r = await API.get('/public/studenti' + (q ? '?q=' + encodeURIComponent(q) : ''));
    const studenti = r.studenti || [];
    if (studenti.length === 0) {
      results.innerHTML = '<div class="empty"><div class="icon">${ICONS.search}</div>Nessuno studente con contenuti pubblici trovato.</div>';
      return;
    }
    results.innerHTML = '<h2 class="section-title">Profili in evidenza</h2>' +
      '<div class="people-grid">' + studenti.map(st => `
        <div class="person-card" data-id="${st.studenteId}">
          ${avatar(st.studenteId, st.autore || (st.nome + ' ' + st.cognome), 76)}
          <div class="person-name">${esc(st.autore || (st.nome + ' ' + st.cognome))}</div>
          ${st.istituzione ? `<div class="person-sub">${esc(st.istituzione)}</div>` : ''}
          ${st.areaDisciplinare ? `<div class="person-sub muted">${esc(st.areaDisciplinare)}</div>` : ''}
          <div class="person-meta">${st.numeroRisorse} risorsa/e pubbliche</div>
          <button class="btn person-btn">Vedi profilo</button>
        </div>`).join('') + '</div>';
    results.querySelectorAll('.person-card').forEach(el => { el.onclick = () => visualizzaStudente(el.dataset.id); });
  }

  async function visualizzaStudente(id) {
    results.innerHTML = '<div class="spinner">Caricamento…</div>';
    const r = await API.get('/public/studente/' + id);
    if (!r.successo) { results.innerHTML = '<div class="empty"><div class="icon">' + ICONS.ban + '</div>' + esc(r.messaggio || 'Studente non disponibile') + '</div>'; return; }
    renderContenuto(r.studente, 'Profilo studente', id);
  }

  async function apriLink(url) {
    results.innerHTML = '<div class="spinner">Apertura link…</div>';
    const r = await API.get('/public/link/' + encodeURIComponent(url));
    if (!r.successo) { results.innerHTML = '<div class="empty"><div class="icon">' + ICONS.ban + '</div>' + esc(r.messaggio || 'Link non valido') + '</div>'; return; }
    renderContenuto(r.preset, r.nomeLink || 'Contenuto condiviso', r.preset.studenteId);
  }

  function renderContenuto(obj, etichetta, fotoId) {
    const pr = obj.profilo || {};
    const profiloRows = [
      ['Istituzione AFAM', pr.istituzioneAfam], ['Email', pr.emailPersonale], ['Corso di Studi', pr.corsoStudi],
      ['Anno Accademico', pr.annoAccademico], ['Anno di Corso', pr.annoDiCorso],
      ['Area Disciplinare', pr.areaDisciplinare],
    ].filter(([, v]) => v).map(([k, v]) =>
      `<div style="display:flex;justify-content:space-between;padding:9px 0;border-bottom:1px solid var(--border)">
        <span class="muted">${k}</span><strong>${esc(v)}</strong></div>`).join('');

    const risorse = (obj.risorse || []).map(r => `
      <div class="res-item">
        <div class="meta">
          <span style="font-size:20px">${TIPO_ICON[r.tipo] || '📄'}</span>
          <div><div class="name">${esc(r.nome)} <span class="badge badge-tipo">${esc(r.tipo)}</span></div>
          ${r.descrizione ? `<div class="desc">${esc(r.descrizione)}</div>` : ''}</div>
        </div>
        <a class="btn btn-sm" href="/api/file/${r.id}" target="_blank">Apri</a>
      </div>`).join('') || '<p class="muted">Questo studente non ha ancora reso pubblica alcuna risorsa.</p>';

    const head = `<div class="profile-hero">
        ${fotoId ? avatar(fotoId, obj.nome, 92) : ''}
        <div><h2 style="margin:0">${esc(obj.nome)}</h2><p class="muted" style="margin:2px 0 0">${esc(etichetta)}</p></div>
      </div>`;

    results.innerHTML =
      '<p style="margin-bottom:16px"><a href="view.html">← Torna alla ricerca</a></p>' +
      head +
      (profiloRows ? `<div class="card" style="margin:20px 0"><h3 style="margin-bottom:10px">Profilo Formativo e Artistico</h3>${profiloRows}</div>` : '') +
      '<h3 style="margin-bottom:12px">Risorse pubbliche</h3>' +
      risorse;
  }
})();
