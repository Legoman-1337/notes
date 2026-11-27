/* Плагин «Вода» (water-tracker) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {days:{}};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var EMOJI = "💧"; var UNIT = "стаканов"; var GOAL = 8; var MODE = "add";
  function n() { return db.days && db.days[today(0)] ? db.days[today(0)] : 0; }
  function add(v) {
    db.days = db.days || {};
    db.days[today(0)] = Math.max(0, n() + v);
    save(); render();
  }
  function setVal(v) {
    v = parseInt(v, 10) || 0; if (v < 0) v = 0;
    db.days = db.days || {}; db.days[today(0)] = v; save(); render();
  }
  function render() {
    if (!root) return;
    var cur = n();
    var pct = GOAL > 0 ? Math.min(100, Math.round(cur * 100 / GOAL)) : (cur > 0 ? 100 : 0);
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div class="hero-num money">' + cur + (GOAL > 0 ? '<span style="font-size:16px;font-weight:500" class="muted"> / ' + GOAL + '</span>' : '') + '</div>'
      + '<div class="muted" style="font-size:12px;margin:2px 0 12px">' + esc(UNIT) + ' сегодня</div>'
      + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div>'
      + '</div>';
    if (MODE === 'add') {
      html += '<div class="card" style="text-align:center;padding:12px">'
        + '<button class="btn" data-act="plus" style="min-width:100px">+1</button> '
        + '<button class="btn secondary" data-act="minus" style="min-width:72px">−1</button></div>';
    } else {
      html += '<div class="card"><div class="row">'
        + '<input type="number" id="f-n" class="grow" placeholder="Сколько за сегодня" inputmode="numeric">'
        + '<button class="btn" data-act="set">Записать</button></div></div>';
    }
    html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Последние 7 дней</div><div class="row" style="justify-content:space-between">';
    for (var i = 6; i >= 0; i--) {
      var k = today(i), v = (db.days && db.days[k]) || 0;
      var cls = v >= GOAL && GOAL > 0 ? 'color:var(--accent);font-weight:700' : '';
      html += '<div style="text-align:center;min-width:34px"><div style="' + cls + '" class="money">' + v + '</div><div class="muted" style="font-size:10px">' + dlabel(i) + '</div></div>';
    }
    html += '</div></div>';
    var total = 0; for (var k in db.days) total += db.days[k] || 0;
    html += '<div class="muted" style="text-align:center;font-size:12px">Всего: ' + money(total) + '</div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'plus') add(1);
    else if (act === 'minus') add(-1);
    else if (act === 'set') setVal(val('f-n'));
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
