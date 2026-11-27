/* Плагин «Гарантии» (warranties) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {items:[],showAdd:false};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var EMOJI = "🧾"; var P1 = "Техника / покупка"; var P2 = "Гарантия до"; var DONE = ""; var ADDBTN = "+ Покупка";
  var PRESET = []; var HASDONE = false; var EMPTYHINT = "Добавьте покупки с гарантией";
  function _prefill() {
    if (PRESET.length && (!db.items || !db.items.length)) {
      db.items = PRESET.map(function (t) { return { id: uid(), t: t, note: '', done: false }; });
      save();
    }
  }
  function render() {
    if (!root) return;
    var html = '';
    if (!db.items || !db.items.length) {
      html += '<div class="card hero"><div class="hero-icon">' + EMOJI + '</div><p class="muted" style="margin:4px 0">' + esc(EMPTYHINT) + '</p></div>';
    }
    (db.items || []).forEach(function (it) {
      html += '<div class="card" style="padding:10px 13px;' + (it.done ? 'opacity:.58' : '') + '">'
        + '<div class="row" style="gap:10px">'
        + (HASDONE ? '<button class="btn small' + (it.done ? '' : ' secondary') + '" data-act="toggle" data-id="' + esc(it.id) + '" style="min-width:42px">' + (it.done ? '↩' : '✓') + '</button>' : '')
        + '<div class="grow" style="min-width:0"><b style="' + (it.done ? 'text-decoration:line-through' : '') + '">' + esc(it.t) + '</b>'
        + (it.note ? '<div class="muted" style="font-size:12px;margin-top:2px">' + esc(it.note) + '</div>' : '')
        + '</div>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(it.id) + '">✕</button>'
        + '</div></div>';
    });
    var done = 0; (db.items || []).forEach(function (i) { if (i.done) done++; });
    if (HASDONE && db.items && db.items.length) {
      var pct = Math.round(done * 100 / db.items.length);
      html += '<div class="card" style="padding:11px 14px"><div class="row" style="justify-content:space-between;margin-bottom:6px"><span class="muted" style="font-size:12px">' + esc(DONE) + '</span><b class="money" style="font-size:12px">' + done + ' из ' + db.items.length + '</b></div>'
        + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div></div>';
    }
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">' + esc(ADDBTN) + '</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="' + esc(P1) + '" style="width:100%;margin-bottom:8px">'
        + (P2 ? '<input type="text" id="f-note" placeholder="' + esc(P2) + '" style="width:100%;margin-bottom:8px">' : '')
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'toggle') {
      (db.items || []).forEach(function (i) { if (i.id === id) i.done = !i.done; });
      save(); return render();
    }
    if (act === 'del') {
      db.items = (db.items || []).filter(function (i) { return i.id !== id; });
      save(); return render();
    }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите текст'); return; }
      var note = P2 ? val('f-note').trim() : '';
      db.items = db.items || [];
      db.items.unshift({ id: uid(), t: t, note: note, done: false });
      db.showAdd = false; save(); app.toast('Добавлено'); return render();
    }
  }

  function _prefill() {}
  function _wire() {}

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
