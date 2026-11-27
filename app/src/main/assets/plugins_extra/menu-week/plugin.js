/* Плагин «Меню недели» (menu-week) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {menu:{}};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var DAYS = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'];
  function render() {
    if (!root) return;
    var html = '<div class="card">';
    DAYS.forEach(function (d, idx) {
      var short = ['Пн','Вт','Ср','Чт','Пт','Сб','Вс'][idx];
      html += '<div class="row" style="margin-bottom:8px">'
        + '<span class="chip" style="min-width:46px;text-align:center">' + short + '</span>'
        + '<input type="text" class="grow menu-day" data-day="' + short + '" placeholder="' + d + '" value="' + esc((db.menu || {})[short] || '') + '">'
        + '</div>';
    });
    html += '<button class="btn" data-act="save" style="width:100%;margin-top:6px">Сохранить меню 🍲</button></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'save') return;
    db.menu = db.menu || {};
    var inputs = root.querySelectorAll('.menu-day');
    for (var i = 0; i < inputs.length; i++) {
      var d = inputs[i].getAttribute('data-day');
      var v = inputs[i].value.trim();
      if (v) db.menu[d] = v; else delete db.menu[d];
    }
    save(); app.toast('Меню сохранено 🍲');
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
