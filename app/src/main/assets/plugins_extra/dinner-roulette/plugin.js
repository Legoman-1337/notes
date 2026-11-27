/* Плагин «Что на ужин» (dinner-roulette) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {items:[],last:''};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var EMOJI = "🍽️"; var BTN = "Выбрать блюдо"; var HINT = "Добавьте свои любимые блюда";
  var PRESET = ["Плов","Макароны с сыром","Борщ","Пельмени","Гречка с котлетой","Рис с овощами","Запечённая картошка","Куриный суп","Домашняя пицца","Блины","Салат и рыба","Творожная запеканка"];
  function _prefill() { if (PRESET.length && (!db.items || !db.items.length)) { db.items = PRESET.slice(); save(); } }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div style="font-size:19px;font-weight:800;min-height:32px;margin-bottom:12px">' + (db.last ? esc(db.last) : '<span class="muted" style="font-weight:500;font-size:15px">Нажмите кнопку ниже</span>') + '</div>'
      + '<button class="btn" data-act="go"' + ((!db.items || !db.items.length) ? ' disabled' : '') + '>' + BTN + '</button>'
      + '</div>';
    html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Ваш список идей</div>';
    (db.items || []).forEach(function (t, i) {
      html += '<div class="row" style="margin-bottom:6px"><span class="chip grow" style="text-align:left">' + esc(t) + '</span>'
        + '<button class="btn danger small" data-act="del" data-i="' + i + '">✕</button></div>';
    });
    if (!db.items || !db.items.length) html += '<p class="muted" style="font-size:12px;margin:4px 0 8px">' + (HINT || 'Добавьте хотя бы один пункт') + '</p>';
    html += '<div class="row" style="margin-top:8px">'
      + '<input type="text" id="f-item" class="grow" placeholder="' + esc("Добавьте своё блюдо") + '">'
      + '<button class="btn" data-act="add">+</button></div></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'add') {
      var v = val('f-item').trim();
      if (!v) return;
      db.items = db.items || []; db.items.push(v); save(); return render();
    }
    if (act === 'del') {
      var i = parseInt(el.getAttribute('data-i'), 10);
      db.items.splice(i, 1); if (db.last && db.items.indexOf(db.last) < 0) db.last = '';
      save(); return render();
    }
    if (act === 'go') {
      if (!db.items || !db.items.length) return;
      db.last = db.items[Math.floor(Math.random() * db.items.length)];
      save(); return render();
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
