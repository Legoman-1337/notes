/* Плагин «Таймер» (timers-mini) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = { };

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var left = 0, total = 0, running = false, timer = null;
  function fmt(s) {
    var m = Math.floor(s / 60), r = s % 60;
    return m + ':' + (r < 10 ? '0' : '') + r;
  }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-num money" style="font-size:44px">' + (total || left ? fmt(left) : '0:00') + '</div>';
    if (total) html += '<div class="prog-track" style="margin:10px 14% 12px"><div class="prog-fill" style="width:' + Math.round(left * 100 / total) + '%"></div></div>';
    if (!running) {
      html += '<div class="row" style="justify-content:center;margin:10px 0">'
        + '<input type="number" id="f-m" placeholder="мин" style="width:86px" inputmode="numeric">'
        + '<input type="number" id="f-s" placeholder="сек" style="width:86px" inputmode="numeric"></div>'
        + '<button class="btn" data-act="start" style="min-width:150px">Старт</button>';
    } else {
      html += '<button class="btn secondary" data-act="stop" style="min-width:150px">Стоп</button>';
    }
    if (total && !running) html += '<div style="margin-top:8px"><button class="btn danger small" data-act="reset">Сброс</button></div>';
    html += '</div>';
    root.innerHTML = html;
  }
  function tick() {
    if (!root) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) {
      left = 0; running = false; clearInterval(timer);
      app.toast('⏰ Время вышло!');
    }
    render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'start') {
      var m = parseInt(val('f-m'), 10) || 0;
      var s = parseInt(val('f-s'), 10) || 0;
      total = m * 60 + s;
      if (!total) { app.toast('Укажите время'); return; }
      left = total; running = true;
      if (timer) clearInterval(timer);
      timer = setInterval(tick, 1000);
      return render();
    }
    if (act === 'stop') { running = false; if (timer) clearInterval(timer); return render(); }
    if (act === 'reset') { running = false; if (timer) clearInterval(timer); total = 0; left = 0; return render(); }
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
