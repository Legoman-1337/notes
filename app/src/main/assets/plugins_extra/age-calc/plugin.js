/* Плагин «Возраст» (age-calc) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {d:''};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card">'
      + '<div class="row"><span class="chip" style="min-width:120px">Дата рождения</span>'
      + '<input type="date" id="f-d" class="grow" value="' + esc(db.d || '') + '"></div>'
      + '</div><div id="f-res"></div>';
    upd();
  }
  function upd() {
    db.d = val('f-d');
    var box = document.getElementById('f-res');
    if (!box) return;
    if (!db.d) { box.innerHTML = ''; return; }
    var b = new Date(db.d + 'T00:00:00');
    var now = new Date();
    if (isNaN(b) || b >= now) { box.innerHTML = '<div class="muted" style="text-align:center;padding:8px">Дата должна быть в прошлом</div>'; return; }
    var y = now.getFullYear() - b.getFullYear();
    var m = now.getMonth() - b.getMonth();
    if (m < 0 || (m === 0 && now.getDate() < b.getDate())) y--;
    var days = Math.floor((now - b) / 86400000);
    var html = '<div class="card hero">'
      + '<div class="hero-num money">' + y + '</div>'
      + '<div class="muted" style="font-size:12px;margin-bottom:10px">полных лет</div>'
      + '<div class="row" style="justify-content:center;gap:8px">'
      + '<span class="chip money">' + days + ' дн.</span>'
      + '<span class="chip money">' + Math.round(days / 7) + ' нед.</span></div>';
    var next = new Date(now.getFullYear(), b.getMonth(), b.getDate());
    if (next < now) next.setFullYear(now.getFullYear() + 1);
    var to = Math.ceil((next - now) / 86400000);
    html += '<div class="muted" style="font-size:12px;margin-top:10px">до дня рождения: <b>' + to + '</b> дн. 🎂</div></div>';
    box.innerHTML = html;
  }
  function onAction(el) { }
  function _wire() {
    var el = document.getElementById('f-d');
    if (el) el.addEventListener('change', function () { save(); upd(); });
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
