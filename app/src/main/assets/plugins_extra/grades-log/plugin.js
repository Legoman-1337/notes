/* Плагин «Оценки» (grades-log) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {grades:[]};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  var SCALE = [2, 3, 4, 5];
  function render() {
    if (!root) return;
    var html = '<div class="card">'
      + '<input type="text" id="f-s" placeholder="Предмет" style="width:100%;margin-bottom:8px">'
      + '<div class="row" style="justify-content:center;gap:8px">'
      + SCALE.map(function (v) { return '<button class="chip" data-act="add" data-v="' + v + '" style="min-width:48px;font-weight:700">' + v + '</button>'; }).join('')
      + '</div></div>';
    var subjects = {};
    (db.grades || []).forEach(function (g) {
      subjects[g.s] = subjects[g.s] || []; subjects[g.s].push(g.v);
    });
    var names = Object.keys(subjects);
    if (names.length) {
      var all = [], htmlRows = '';
      names.forEach(function (s) {
        var arr = subjects[s];
        arr.forEach(function (v) { all.push(v); });
        var avg = arr.reduce(function (a, b) { return a + b; }, 0) / arr.length;
        htmlRows += '<div class="row" style="justify-content:space-between;margin-bottom:6px">'
          + '<span style="font-size:14px">' + esc(s) + '</span>'
          + '<span><b class="money" style="color:' + (avg >= 4.5 ? 'var(--accent)' : 'var(--text)') + '">' + (Math.round(avg * 100) / 100) + '</b> '
          + '<span class="muted" style="font-size:11px">(' + arr.join(', ') + ')</span></span></div>';
      });
      var total = all.reduce(function (a, b) { return a + b; }, 0) / all.length;
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Предметы</div>' + htmlRows
        + '<div class="row" style="justify-content:space-between;margin-top:10px;padding-top:8px;border-top:1px solid var(--border)"><b>Общий средний</b><b class="money" style="color:var(--accent)">' + (Math.round(total * 100) / 100) + '</b></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'add') return;
    var s = val('f-s').trim();
    var v = parseInt(el.getAttribute('data-v'), 10);
    if (!s) { app.toast('Введите предмет'); return; }
    db.grades = db.grades || [];
    db.grades.unshift({ s: s, v: v });
    save(); render();
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
