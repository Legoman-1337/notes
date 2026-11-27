/* Плагин «Паролик» (password-gen) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {pw:'',len:12,dig:true,spec:false};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var CHARS = 'abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ';
  var DIGITS = '23456789';
  var SPECIAL = '!@#$%&*';
  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card hero">'
      + '<div style="font-size:20px;font-weight:800;font-family:monospace;word-break:break-all;background:var(--surface2);border:1px solid var(--border);border-radius:12px;padding:14px 10px;min-height:50px">' + (db.pw || '—') + '</div>'
      + '</div>'
      + '<div class="card"><div class="row" style="margin-bottom:10px"><span class="chip" style="min-width:96px">Длина</span>'
      + '<input type="number" id="f-len" class="grow" value="' + (db.len || 12) + '" min="4" max="40" inputmode="numeric"></div>'
      + '<div class="row" style="gap:8px;justify-content:center;flex-wrap:wrap;margin-bottom:10px">'
      + '<button class="chip' + (db.dig !== false ? ' active' : '') + '" data-act="dig">Цифры 123</button>'
      + '<button class="chip' + (db.spec ? ' active' : '') + '" data-act="spec">Символы !@#</button>'
      + '</div><button class="btn" data-act="go" style="width:100%">Сгенерировать 🔐</button></div>';
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'dig') { db.dig = db.dig === false; save(); return render(); }
    if (act === 'spec') { db.spec = !db.spec; save(); return render(); }
    if (act !== 'go') return;
    var len = parseInt(val('f-len'), 10) || 12;
    if (len < 4) len = 4; if (len > 40) len = 40;
    db.len = len;
    var pool = CHARS + (db.dig !== false ? DIGITS : '') + (db.spec ? SPECIAL : '');
    var out = '';
    for (var i = 0; i < len; i++) out += pool[Math.floor(Math.random() * pool.length)];
    db.pw = out;
    save(); render();
  }

  function activate(api) {
    app = api;
    try {
      var loaded = JSON.parse(app.load() || '{}');
      if (loaded && typeof loaded === 'object') { for (var k in db) { if (loaded[k] !== undefined) db[k] = loaded[k]; } }
    } catch (e) {}
    if (typeof _prefill === 'function') _prefill();
    root = document.getElementById('root');
    root.addEventListener('click', function (ev) {
      var el = ev.target.closest ? ev.target.closest('[data-act]') : null;
      if (el) { ev.preventDefault(); onAction(el); }
    });
    if (typeof _wire === 'function') _wire();
    render();
  }
  function deactivate() { if (app) save(); root = null; app = null; }
  window.plugin = { activate: activate, deactivate: deactivate };
})();
