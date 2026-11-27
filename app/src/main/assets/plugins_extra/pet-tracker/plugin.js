/* Плагин «Питомец» (pet-tracker) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {pet:'',days:{}};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var TASKS = [['feed', '🍲', 'Покормила'], ['play', '🎾', 'Поиграла'], ['litter', '🧹', 'Уход']];
  function render() {
    if (!root) return;
    var t = today(0);
    var cur = (db.days && db.days[t]) || {};
    var doneCount = TASKS.filter(function (x) { return cur[x[0]]; }).length;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🐱</div>'
      + '<input type="text" id="f-pet" placeholder="Имя питомца" value="' + esc(db.pet || '') + '" style="width:72%;margin-bottom:12px;text-align:center">'
      + '<div class="row" style="justify-content:center;gap:8px;flex-wrap:wrap">';
    TASKS.forEach(function (task) {
      var on = !!cur[task[0]];
      html += '<button class="chip' + (on ? ' active' : '') + '" data-act="toggle" data-k="' + task[0] + '" style="padding:10px 14px">' + task[1] + ' ' + task[2] + '</button>';
    });
    html += '</div><div class="muted" style="font-size:12px;margin-top:12px">Выполнено сегодня: <b>' + doneCount + ' из 3</b></div></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') === 'toggle') {
      var k = el.getAttribute('data-k');
      db.days = db.days || {};
      var cur = db.days[today(0)] || {};
      if (cur[k]) delete cur[k]; else cur[k] = 1;
      db.days[today(0)] = cur;
      save(); return render();
    }
  }
  function _wire() {
    var el = document.getElementById('f-pet');
    if (el) el.addEventListener('change', function () { db.pet = el.value.trim(); save(); });
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
