/* auth.js — gestione dei flussi di Accesso al Sistema */
(function () {
  const panels = {
    login: document.getElementById('form-login'),
    register: document.getElementById('form-register'),
    recover: document.getElementById('form-recover'),
    '2fa': document.getElementById('form-2fa'),
    newpass: document.getElementById('form-newpass'),
  };
  const alertBox = document.getElementById('alert');
  let flow = { email: null, mode: 'login' };

  function show(name) {
    Object.values(panels).forEach(p => p.style.display = 'none');
    panels[name].style.display = 'block';
    hideAlert();
  }
  function showAlert(msg, ok = false) {
    alertBox.textContent = msg;
    alertBox.className = 'alert show ' + (ok ? 'alert-success' : 'alert-error');
  }
  function hideAlert() { alertBox.className = 'alert'; }

  if (API.getToken()) {
    API.get('/auth/me').then(r => { if (r.autenticato) location.href = 'app.html'; });
  }

  document.querySelectorAll('[data-go]').forEach(a => {
    a.addEventListener('click', e => { e.preventDefault(); show(a.dataset.go); });
  });

  panels.login.addEventListener('submit', async e => {
    e.preventDefault();
    const r = await API.post('/auth/login', {
      emailIstituzionale: document.getElementById('login-email').value.trim(),
      password: document.getElementById('login-password').value,
    });
    if (r.successo && r.richiedi2FA) { flow = { email: r.email, mode: 'login' }; show('2fa'); }
    else { showAlert(r.messaggio || 'Credenziali non valide'); }
  });

  panels.register.addEventListener('submit', async e => {
    e.preventDefault();
    const numero = document.getElementById('reg-prefix').value +
      document.getElementById('reg-cell').value.replace(/\s+/g, '').replace(/^0+/, '');
    const r = await API.post('/auth/register', {
      nome: document.getElementById('reg-nome').value.trim(),
      cognome: document.getElementById('reg-cognome').value.trim(),
      codiceFiscale: document.getElementById('reg-cf').value.trim(),
      emailIstituzionale: document.getElementById('reg-email-ist').value.trim(),
      emailPersonale: document.getElementById('reg-email-pers').value.trim(),
      cellulare: numero,
      password: document.getElementById('reg-password').value,
    });
    if (r.successo && r.richiedi2FA) {
      flow = { email: r.email, mode: 'login' };
      showAlert('Registrazione riuscita! Verifica il codice 2FA.', true);
      setTimeout(() => show('2fa'), 600);
    } else { showAlert(r.messaggio || 'Errore nella registrazione'); }
  });

  panels.recover.addEventListener('submit', async e => {
    e.preventDefault();
    const email = document.getElementById('rec-email').value.trim();
    const r = await API.post('/auth/recover', { email });
    if (r.successo && r.richiedi2FA) { flow = { email: r.email, mode: 'recover' }; show('2fa'); }
    else { showAlert(r.messaggio || 'Account non trovato'); }
  });

  panels['2fa'].addEventListener('submit', async e => {
    e.preventDefault();
    const codice = document.getElementById('tfa-code').value.trim();
    if (flow.mode === 'recover') {
      if (codice !== '123456') { showAlert('Codice non valido'); return; }
      show('newpass'); return;
    }
    const r = await API.post('/auth/verify-2fa', { email: flow.email, codice });
    if (r.successo && r.token) { API.setToken(r.token); location.href = 'app.html'; }
    else { showAlert(r.messaggio || 'Codice non valido'); }
  });

  panels.newpass.addEventListener('submit', async e => {
    e.preventDefault();
    const r = await API.post('/auth/reset-password', {
      email: flow.email,
      nuovaPassword: document.getElementById('np-pass').value,
      confermaPassword: document.getElementById('np-conf').value,
    });
    if (r.successo) { showAlert('Password aggiornata! Ora puoi accedere.', true); setTimeout(() => show('login'), 900); }
    else { showAlert(r.messaggio || 'Errore'); }
  });
})();
