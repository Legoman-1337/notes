/* Плагин «Дыхание 4-7-8» (breathe-calm) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {cycles:0};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var PHASES = [['Вдох', 4], ['Задержка', 7], ['Выдох', 8]];
  var running = false, phase = 0, left = 0, timer = null;
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🌬️</div>';
    if (running) {
      html += '<div style="font-size:24px;font-weight:800;color:var(--accent)">' + PHASES[phase][0] + '</div>'
        + '<div class="hero-num money" style="font-size:50px">' + left + '</div>'
        + '<div class="muted" style="font-size:12px;margin-bottom:14px">циклов пройдено: ' + (db.cycles || 0) + '</div>'
        + '<button class="btn secondary" data-act="stop">Стоп</button>';
    } else {
      html += '<p class="muted" style="margin:0 0 8px">Вдох на 4 счёта, задержка на 7, выдох на 8.<br>Несколько циклов быстро возвращают спокойствие.</p>'
        + '<div class="muted" style="font-size:12px;margin-bottom:14px">пройдено циклов: ' + (db.cycles || 0) + '</div>'
        + '<button class="btn" data-act="start" style="min-width:170px">Начать</button>';
    }
    html += '</div>';
    root.innerHTML = html;
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) {
      phase++;
      if (phase >= PHASES.length) { phase = 0; db.cycles = (db.cycles || 0) + 1; save(); }
      left = PHASES[phase][1];
    }
    render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'start') {
      running = true; phase = 0; left = PHASES[0][1];
      if (timer) clearInterval(timer);
      timer = setInterval(tick, 1000);
      return render();
    }
    if (act === 'stop') {
      running = false;
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
