/* Плагин «Валюты» (currency-memo) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {rate:0};

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
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:118px">Курс ₽ за 1</span>'
      + '<input type="number" id="f-rate" class="grow" placeholder="например 95" value="' + (db.rate || '') + '" inputmode="decimal"></div>'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:118px">₽ → валюта</span>'
      + '<input type="number" id="f-rub" class="grow" placeholder="рубли" inputmode="decimal"></div>'
      + '<div class="row"><span class="chip" style="min-width:118px">валюта → ₽</span>'
      + '<input type="number" id="f-cur" class="grow" placeholder="валюта" inputmode="decimal"></div>'
      + '</div>'
      + '<div class="muted" style="text-align:center;font-size:12px">Работает полностью офлайн по вашему курсу</div>';
    wire();
  }
  function wire() {
    var rub = document.getElementById('f-rub');
    var cur = document.getElementById('f-cur');
    if (rub) rub.addEventListener('input', function () {
      var r = parseFloat(rub.value) || 0;
      var rate2 = parseFloat(val('f-rate')) || 0;
      if (cur && rate2) cur.value = (Math.round(r / rate2 * 100) / 100).toString();
    });
    if (cur) cur.addEventListener('input', function () {
      var c = parseFloat(cur.value) || 0;
      var rate2 = parseFloat(val('f-rate')) || 0;
      if (rub && rate2) rub.value = (Math.round(c * rate2 * 100) / 100).toString();
    });
    var r = document.getElementById('f-rate');
    if (r) r.addEventListener('input', function () { db.rate = parseFloat(r.value) || 0; save(); });
  }
  function onAction(el) { }

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
