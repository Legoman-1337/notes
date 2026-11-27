/* Плагин «Копилка мечты» (piggy-bank) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {goal:0,name:'',saved:0,log:[]};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function render() {
    if (!root) return;
    if (!db.goal) {
      root.innerHTML = '<div class="card hero">'
        + '<div class="hero-icon">🐷</div>'
        + '<input type="text" id="f-name" placeholder="На что копим?" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><input type="number" id="f-goal" class="grow" placeholder="Сумма цели ₽" inputmode="numeric">'
        + '<button class="btn" data-act="set">Начать</button></div></div>';
      return;
    }
    var pct = Math.min(100, Math.round((db.saved || 0) * 100 / db.goal));
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🐷</div>'
      + '<b style="font-size:17px">' + esc(db.name || 'Мечта') + '</b>'
      + '<div class="hero-num money" style="margin-top:6px">' + money(db.saved || 0) + ' ₽</div>'
      + '<div class="muted" style="font-size:12px;margin-bottom:12px">из ' + money(db.goal) + ' ₽ · ' + pct + '%</div>'
      + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div>'
      + '</div>'
      + '<div class="card"><div class="row">'
      + '<input type="number" id="f-add" class="grow" placeholder="Отложить ₽" inputmode="numeric">'
      + '<button class="btn" data-act="add">Отложить</button></div></div>';
    if (db.log && db.log.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:6px;font-weight:600">История</div>'
        + db.log.slice(0, 5).map(function (l) { return '<div class="muted" style="font-size:12px;margin:2px 0">' + esc(l) + '</div>'; }).join('') + '</div>';
    }
    html += '<div class="card" style="text-align:center"><button class="btn danger small" data-act="reset">Начать новую цель</button></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'set') {
      var g = parseInt(val('f-goal'), 10) || 0;
      if (!g) { app.toast('Введите сумму цели'); return; }
      db.goal = g; db.name = val('f-name').trim(); db.saved = 0; db.log = [];
      save(); return render();
    }
    if (act === 'add') {
      var a = parseInt(val('f-add'), 10) || 0;
      if (!a) { app.toast('Сколько откладываем?'); return; }
      db.saved = (db.saved || 0) + a;
      db.log = db.log || [];
      db.log.unshift(today(0).slice(5) + ': ' + (a > 0 ? '+' : '') + money(a) + ' ₽' + (db.saved >= db.goal ? ' — цель достигнута! 🎉' : ''));
      save();
      if (db.saved >= db.goal) app.toast('Мечта собрана! 🎉');
      return render();
    }
    if (act === 'reset') { db.goal = 0; db.saved = 0; db.name = ''; db.log = []; save(); return render(); }
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
