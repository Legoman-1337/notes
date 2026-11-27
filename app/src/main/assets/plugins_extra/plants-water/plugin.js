/* Плагин «Полив цветов» (plants-water) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {plants:[],showAdd:false};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function render() {
    if (!root) return;
    var html = '';
    if (!db.plants || !db.plants.length) {
      html += '<div class="card hero"><div class="hero-icon">🌷</div><p class="muted" style="margin:4px 0">Добавьте ваши растения, чтобы не забывать о поливе</p></div>';
    }
    (db.plants || []).forEach(function (p) {
      var days = null;
      if (p.d) {
        var d = new Date(p.d + 'T00:00:00');
        days = Math.floor((new Date(today(0) + 'T00:00:00') - d) / 86400000);
      }
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:10px">'
        + '<span style="font-size:24px">🌿</span>'
        + '<div class="grow" style="min-width:0"><b>' + esc(p.t) + '</b>'
        + '<div class="muted" style="font-size:12px;margin-top:2px">' + (days == null ? 'ещё не поливали' : (days === 0 ? 'полито сегодня ✓' : 'полито ' + days + ' дн. назад')) + '</div></div>'
        + '<button class="btn small' + (days === 0 ? ' secondary' : '') + '" data-act="water" data-id="' + esc(p.id) + '">💧</button>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(p.id) + '">✕</button>'
        + '</div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Добавить растение</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Название растения" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'water') {
      (db.plants || []).forEach(function (p) { if (p.id === id) p.d = today(0); });
      save(); app.toast('Полито 💧'); return render();
    }
    if (act === 'del') { db.plants = (db.plants || []).filter(function (p) { return p.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите название'); return; }
      db.plants = db.plants || [];
      db.plants.push({ id: uid(), t: t, d: '' });
      db.showAdd = false; save(); return render();
    }
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
