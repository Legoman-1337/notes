/* Плагин «Между датами» (date-diff) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {a:'',b:''};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function diff(a, b) {
    if (!a || !b) return null;
    var d1 = new Date(a + 'T00:00:00'), d2 = new Date(b + 'T00:00:00');
    if (isNaN(d1) || isNaN(d2)) return null;
    return Math.round((d2 - d1) / 86400000);
  }
  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card">'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:86px">Откуда</span>'
      + '<input type="date" id="f-a" class="grow" value="' + esc(db.a || today(0)) + '"></div>'
      + '<div class="row"><span class="chip" style="min-width:86px">Докуда</span>'
      + '<input type="date" id="f-b" class="grow" value="' + esc(db.b || '') + '"></div>'
      + '</div><div id="f-res"></div>';
    upd();
  }
  function upd() {
    db.a = val('f-a'); db.b = val('f-b');
    var d = diff(db.a, db.b);
    var box = document.getElementById('f-res');
    if (!box) return;
    if (d == null) { box.innerHTML = ''; return; }
    var abs = Math.abs(d);
    box.innerHTML = '<div class="card hero">'
      + '<div class="hero-num money">' + abs + '</div>'
      + '<div class="muted" style="font-size:12px">' + (d >= 0 ? 'дней от первой до второй даты' : 'дней от второй до первой') + '</div>'
      + '<div class="muted" style="font-size:12px;margin-top:6px">примерно ' + Math.round(abs / 7) + ' нед. или ' + (Math.round(abs / 30 * 10) / 10) + ' мес.</div>'
      + '</div>';
  }
  function onAction(el) { }
  function _wire() {
    ['f-a', 'f-b'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.addEventListener('change', function () { save(); upd(); });
    });
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
