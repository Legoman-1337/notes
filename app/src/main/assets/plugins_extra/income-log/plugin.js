/* Плагин «Доходы» (income-log) — «Удобные заметки».
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


  function render() {
    if (!root) return;
    var total = (db.items || []).reduce(function (s, i) { return s + (parseInt(i.a, 10) || 0); }, 0);
    var html = '<div class="card hero">'
      + '<div class="muted" style="font-size:12px">всего поступлений</div>'
      + '<div class="hero-num money" style="color:var(--accent)">' + money(total) + ' ₽</div>'
      + '<div class="muted" style="font-size:12px;margin-top:4px">записей: ' + (db.items || []).length + '</div></div>';
    (db.items || []).forEach(function (i) {
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:10px">'
        + '<div class="grow" style="min-width:0"><b>' + esc(i.t) + '</b>'
        + (i.note ? '<div class="muted" style="font-size:12px">' + esc(i.note) + '</div>' : '') + '</div>'
        + '<b class="money" style="color:var(--accent)">' + money(i.a) + ' ₽</b>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(i.id) + '">✕</button></div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Новое поступление</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Откуда (аванс, возврат…)" style="width:100%;margin-bottom:8px">'
        + '<div class="row">'
        + '<input type="number" id="f-a" class="grow" placeholder="Сумма ₽" inputmode="numeric">'
        + '<input type="text" id="f-note" placeholder="Дата" style="width:100px"></div>'
        + '<div class="row" style="margin-top:8px"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'del') { db.items = (db.items || []).filter(function (i) { return i.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      var a = parseInt(val('f-a'), 10) || 0;
      if (!t || !a) { app.toast('Нужны источник и сумма'); return; }
      db.items = db.items || [];
      db.items.unshift({ id: uid(), t: t, a: a, note: val('f-note').trim() });
      db.showAdd = false; save(); app.toast('Записано'); return render();
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
