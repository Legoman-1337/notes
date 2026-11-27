/* Плагин «Конвертер» (unit-conv) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {cat:'Вес'};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var CATS = {
    'Вес': [['кг', 1], ['фунт', 0.4536], ['унция', 0.02835]],
    'Длина': [['см', 1], ['дюйм', 2.54], ['метр', 100], ['фут', 30.48]],
    'Объём': [['литр', 1], ['галлон', 3.785], ['чашка', 0.25]],
    'Температура': [['°C', 'c'], ['°F', 'f']]
  };
  function render() {
    if (!root) return;
    var cats = Object.keys(CATS);
    var cat = CATS[db.cat] ? db.cat : cats[0];
    var units = CATS[cat];
    var html = '<div class="card"><div class="row" style="flex-wrap:wrap;gap:6px;justify-content:center">';
    cats.forEach(function (c) {
      html += '<button class="chip' + (c === cat ? ' active' : '') + '" data-act="cat" data-c="' + c + '">' + c + '</button>';
    });
    html += '</div></div><div class="card">'
      + '<div class="row" style="margin-bottom:8px">'
      + '<input type="number" id="f-v" class="grow" placeholder="значение" step="any" inputmode="decimal">'
      + '<select id="f-from" style="width:108px">' + units.map(function (u, i) { return '<option value="' + i + '"' + (i === 0 ? ' selected' : '') + '>' + u[0] + '</option>'; }).join('') + '</select></div>'
      + '<div class="row" style="justify-content:center;margin-bottom:8px"><button class="btn secondary small" data-act="swap">⇅ поменять</button></div>'
      + '<div class="row">'
      + '<div class="grow" style="text-align:center;font-size:20px;font-weight:800;color:var(--accent);min-height:28px;align-self:center" id="f-out">…</div>'
      + '<select id="f-to" style="width:108px">' + units.map(function (u, i) { return '<option value="' + i + '"' + (i === 1 ? ' selected' : '') + '>' + u[0] + '</option>'; }).join('') + '</select></div>'
      + '</div>';
    root.innerHTML = html;
    wire();
  }
  function conv(v, fi, ti, cat) {
    var units = CATS[cat];
    if (cat === 'Температура') {
      if (fi === ti) return v;
      if (fi === 0 && ti === 1) return Math.round((v * 9 / 5 + 32) * 10) / 10;
      return Math.round((v - 32) * 5 / 9 * 10) / 10;
    }
    return Math.round(v * units[fi][1] / units[ti][1] * 1000) / 1000;
  }
  function upd() {
    var out = document.getElementById('f-out');
    if (!out) return;
    var v = parseFloat(val('f-v'));
    if (isNaN(v)) { out.textContent = '…'; return; }
    var cat = CATS[db.cat] ? db.cat : Object.keys(CATS)[0];
    var fi = parseInt(val('f-from'), 10) || 0;
    var ti = parseInt(val('f-to'), 10) || 0;
    out.textContent = conv(v, fi, ti, cat) + ' ' + CATS[cat][ti][0];
  }
  function wire() {
    ['f-v', 'f-from', 'f-to'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) { el.addEventListener('input', upd); el.addEventListener('change', upd); }
    });
    upd();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'cat') { db.cat = el.getAttribute('data-c'); return render(); }
    if (act === 'swap') {
      var a = document.getElementById('f-from'), b = document.getElementById('f-to');
      var t = a.selectedIndex; a.selectedIndex = b.selectedIndex; b.selectedIndex = t;
      return upd();
    }
  }
  function _wire() { wire(); }

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
