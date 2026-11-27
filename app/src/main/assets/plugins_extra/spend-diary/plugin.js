/* Плагин «Траты дня» (spend-diary) — «Удобные заметки».
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


  function render() {
    if (!root) return;
    var t = today(0);
    var list = (db.days && db.days[t]) || [];
    var total = list.reduce(function (s, i) { return s + (i.a || 0); }, 0);
    var html = '<div class="card hero">'
      + '<div class="muted" style="font-size:12px">потрачено сегодня</div>'
      + '<div class="hero-num money">' + money(total) + ' ₽</div></div>';
    html += '<div class="card"><div class="row">'
      + '<input type="text" id="f-t" class="grow" placeholder="На что">'
      + '<input type="number" id="f-a" placeholder="₽" style="width:90px" inputmode="numeric">'
      + '<button class="btn" data-act="add">+</button></div></div>';
    if (list.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Сегодня</div>';
      list.forEach(function (i, idx) {
        html += '<div class="row" style="margin-bottom:6px"><div class="grow" style="font-size:14px">' + esc(i.t) + '</div>'
          + '<b class="money" style="font-size:14px">' + money(i.a) + ' ₽</b>'
          + '<button class="btn danger small" data-act="del" data-i="' + idx + '">✕</button></div>';
      });
      html += '</div>';
    }
    var keys = Object.keys(db.days || {}).sort().reverse().slice(1, 5);
    if (keys.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:6px;font-weight:600">Прошлые дни</div>';
      keys.forEach(function (k) {
        var s = (db.days[k] || []).reduce(function (a, i) { return a + (i.a || 0); }, 0);
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">' + k + '</span><b class="money" style="font-size:13px">' + money(s) + ' ₽</b></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var t = today(0);
    if (act === 'add') {
      var what = val('f-t').trim();
      var a = parseInt(val('f-a'), 10) || 0;
      if (!what || !a) { app.toast('Нужны название и сумма'); return; }
      db.days = db.days || {};
      db.days[t] = db.days[t] || [];
      db.days[t].unshift({ t: what, a: a });
      save(); return render();
    }
    if (act === 'del') {
      var idx = parseInt(el.getAttribute('data-i'), 10);
      db.days[t].splice(idx, 1);
      save(); return render();
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
