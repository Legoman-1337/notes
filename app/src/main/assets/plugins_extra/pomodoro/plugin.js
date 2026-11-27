/* Плагин «Помодоро» (pomodoro) — «Удобные заметки».
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


  var FOCUS = 25 * 60, BREAK = 5 * 60;
  var running = false, left = FOCUS, mode = 'focus', timer = null;
  function fmt(s) {
    var m = Math.floor(s / 60), r = s % 60;
    return (m < 10 ? '0' : '') + m + ':' + (r < 10 ? '0' : '') + r;
  }
  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card hero">'
      + '<div class="hero-icon">' + (mode === 'focus' ? '🍅' : '☕') + '</div>'
      + '<div class="muted" style="font-size:12px">' + (mode === 'focus' ? 'время работать' : 'перерыв') + '</div>'
      + '<div class="hero-num money" style="font-size:46px">' + fmt(left) + '</div>'
      + '<div class="row" style="justify-content:center;gap:10px;margin-top:14px">'
      + '<button class="btn' + (running ? ' secondary' : '') + '" data-act="toggle" style="min-width:110px">' + (running ? 'Пауза' : 'Старт') + '</button>'
      + '<button class="btn secondary" data-act="reset">Сброс</button></div>'
      + '<div class="muted" style="font-size:12px;margin-top:12px">🍅 завершено сегодня: ' + ((db.days || {})[today(0)] || 0) + '</div>'
      + '</div>';
  }
  function finish() {
    if (mode === 'focus') {
      db.days = db.days || {};
      db.days[today(0)] = ((db.days || {})[today(0)] || 0) + 1;
      save(); app.toast('🍅 Помодоро готово! 5 минут перерыва');
      mode = 'break'; left = BREAK;
    } else {
      app.toast('Перерыв окончен — за дело!');
      mode = 'focus'; left = FOCUS;
    }
    running = false;
    render();
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) finish(); else render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'toggle') {
      running = !running;
      if (running) { if (timer) clearInterval(timer); timer = setInterval(tick, 1000); }
      else if (timer) clearInterval(timer);
      return render();
    }
    if (act === 'reset') {
      running = false; mode = 'focus'; left = FOCUS;
      if (timer) clearInterval(timer);
      return render();
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
