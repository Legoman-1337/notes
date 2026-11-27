/* Плагин «Приём-чек» (pills-check) — «Удобные заметки».
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


  var EMOJI = "✅"; var LABEL = "приём отмечен";
  function render() {
    if (!root) return;
    var streak = 0;
    for (var i = 0; i < 4000; i++) { if (db.days && db.days[today(i)]) streak++; else break; }
    var doneToday = !!(db.days && db.days[today(0)]);
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div class="hero-num money">' + streak + '</div>'
      + '<div class="muted" style="font-size:12px;margin-bottom:14px">' + esc(LABEL) + ' · дней подряд</div>';
    html += '<div style="display:flex;gap:5px;justify-content:center;flex-wrap:wrap;margin-bottom:16px">';
    for (var i = 13; i >= 0; i--) {
      var on = db.days && db.days[today(i)];
      html += '<span style="width:17px;height:17px;border-radius:6px;display:inline-block;'
        + (on ? 'background:var(--accent)' : 'background:var(--surface2);border:1px solid var(--border)') + '"></span>';
    }
    html += '</div>';
    html += '<button class="btn' + (doneToday ? ' secondary' : '') + '" data-act="toggle" style="min-width:210px">'
      + (doneToday ? 'Снять отметку за сегодня' : 'Отметить сегодня ✓') + '</button></div>';
    var total = 0; for (var k in db.days) total++;
    html += '<div class="muted" style="text-align:center;font-size:12px">Всего отмечено дней: ' + total + '</div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'toggle') return;
    db.days = db.days || {};
    if (db.days[today(0)]) delete db.days[today(0)]; else db.days[today(0)] = 1;
    save(); render();
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
