/* Плагин «Дневник сна» (sleep-diary) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {nights:{},lastS:'23:00',lastW:'07:00'};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function hours(sleep, wake) {
    var p = sleep.split(':'), q = wake.split(':');
    if (p.length < 2 || q.length < 2) return 0;
    var s = parseInt(p[0], 10) * 60 + parseInt(p[1], 10);
    var w = parseInt(q[0], 10) * 60 + parseInt(q[1], 10);
    var d = w - s; if (d <= 0) d += 24 * 60;
    return Math.round(d / 60 * 10) / 10;
  }
  function render() {
    if (!root) return;
    var html = '<div class="card">'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:86px">Легли</span>'
      + '<input type="time" id="f-s" class="grow" value="' + (db.lastS || '23:00') + '"></div>'
      + '<div class="row" style="margin-bottom:10px"><span class="chip" style="min-width:86px">Встали</span>'
      + '<input type="time" id="f-w" class="grow" value="' + (db.lastW || '07:00') + '"></div>'
      + '<button class="btn" data-act="add" style="width:100%">Записать сон 😴</button></div>';
    var keys = Object.keys(db.nights || {}).sort().reverse().slice(0, 7);
    if (keys.length) {
      var avg = keys.reduce(function (s, k) { return s + db.nights[k]; }, 0) / keys.length;
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Последние ночи</div>';
      keys.forEach(function (k) {
        var h = db.nights[k];
        var color = h >= 7 ? 'var(--accent)' : (h < 5.5 ? 'var(--danger)' : 'var(--text)');
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">' + k + '</span><b class="money" style="color:' + color + ';font-size:13px">' + h + ' ч</b></div>';
      });
      html += '<div class="muted" style="font-size:12px;margin-top:8px">среднее: <b>' + (Math.round(avg * 10) / 10) + ' ч</b></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'add') return;
    var s = val('f-s'), w = val('f-w');
    var h = hours(s, w);
    if (!h) { app.toast('Укажите время'); return; }
    db.lastS = s; db.lastW = w;
    db.nights = db.nights || {};
    db.nights[today(0)] = h;
    save(); app.toast('Записано: ' + h + ' ч сна'); render();
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
