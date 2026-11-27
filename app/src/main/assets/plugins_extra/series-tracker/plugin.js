/* Плагин «Сериалы» (series-tracker) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {shows:[],showAdd:false};

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
    if (!db.shows || !db.shows.length) {
      html += '<div class="card hero"><div class="hero-icon">📺</div><p class="muted" style="margin:4px 0">Добавьте сериалы, чтобы помнить серию</p></div>';
    }
    (db.shows || []).forEach(function (s) {
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:8px">'
        + '<div class="grow" style="min-width:0"><b style="font-size:14px">' + esc(s.t) + '</b></div>'
        + '<div class="row" style="gap:4px;align-items:center">'
        + '<span class="chip money">S' + s.s + ' · E' + s.e + '</span>'
        + '<button class="btn small secondary" data-act="minus" data-id="' + esc(s.id) + '">−</button>'
        + '<button class="btn small" data-act="plus" data-id="' + esc(s.id) + '">+</button></div>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(s.id) + '">✕</button>'
        + '</div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Добавить сериал</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Название" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'plus' || act === 'minus') {
      (db.shows || []).forEach(function (s) {
        if (s.id !== id) return;
        if (act === 'plus') {
          s.e++;
          if (s.e > 24) { s.e = 1; s.s++; app.toast('Новый сезон! 🎉'); }
        } else if (s.e > 1) s.e--;
      });
      save(); return render();
    }
    if (act === 'del') { db.shows = (db.shows || []).filter(function (s) { return s.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите название'); return; }
      db.shows = db.shows || [];
      db.shows.push({ id: uid(), t: t, s: 1, e: 1 });
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
