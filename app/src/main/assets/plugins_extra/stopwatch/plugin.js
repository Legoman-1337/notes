/* Плагин «Секундомер» (stopwatch) — «Удобные заметки».
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


  var running = false, t0 = 0, acc = 0, timer = null, laps = [];
  function now() { return acc + (running ? Date.now() - t0 : 0); }
  function fmt(ms) {
    var s = Math.floor(ms / 1000);
    return Math.floor(s / 60) + ':' + ('0' + s % 60).slice(-2) + ',' + ('0' + Math.floor(ms % 1000 / 10)).slice(-2);
  }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div id="f-time" class="hero-num money" style="font-size:44px">' + fmt(now()) + '</div>'
      + '<div class="row" style="justify-content:center;gap:10px;margin-top:14px">'
      + '<button class="btn' + (running ? ' secondary' : '') + '" data-act="toggle" style="min-width:110px">' + (running ? 'Стоп' : (acc ? 'Дальше' : 'Старт')) + '</button>'
      + (running ? '<button class="btn secondary" data-act="lap">Круг</button>' : '')
      + (acc && !running ? '<button class="btn secondary" data-act="reset">Сброс</button>' : '')
      + '</div></div>';
    if (laps.length) {
      html += '<div class="card">';
      laps.slice().reverse().forEach(function (l, i) {
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">Круг ' + (laps.length - i) + '</span><b class="money" style="font-size:13px">' + fmt(l) + '</b></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    var t = document.getElementById('f-time');
    if (t) t.textContent = fmt(now());
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'toggle') {
      if (running) { acc = now(); running = false; if (timer) clearInterval(timer); }
      else { t0 = Date.now(); running = true; if (timer) clearInterval(timer); timer = setInterval(tick, 60); }
      return render();
    }
    if (act === 'lap') { laps.push(now()); return render(); }
    if (act === 'reset') { running = false; if (timer) clearInterval(timer); acc = 0; laps = []; return render(); }
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
