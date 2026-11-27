/* Плагин «Расклад зарплаты» (salary-plan) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {sum:'',a:50,b:30};

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
      + '<input type="number" id="f-sum" placeholder="Сумма дохода ₽" value="' + (db.sum || '') + '" style="width:100%;margin-bottom:8px" inputmode="decimal">'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:116px">Нужное %</span><input type="number" id="f-a" class="grow" value="' + (db.a || 50) + '" inputmode="numeric"></div>'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:116px">Желания %</span><input type="number" id="f-b" class="grow" value="' + (db.b || 30) + '" inputmode="numeric"></div>'
      + '<div class="row"><span class="chip" style="min-width:116px">Копилка %</span><input type="text" id="f-c" class="grow" value="20" disabled></div>'
      + '</div><div id="f-res"></div>';
    upd();
  }
  function upd() {
    var sum = parseFloat(val('f-sum').replace(',', '.')) || 0;
    var a = parseInt(val('f-a'), 10) || 0;
    var b = parseInt(val('f-b'), 10) || 0;
    var c = Math.max(0, 100 - a - b);
    var cEl = document.getElementById('f-c');
    if (cEl) cEl.value = c;
    var box = document.getElementById('f-res');
    if (!box) return;
    box.innerHTML = sum ? '<div class="card" style="padding:15px">'
      + '<div class="row" style="justify-content:space-between;margin-bottom:6px"><span class="muted">🏠 Нужное (' + a + '%)</span><b class="money">' + money(sum * a / 100) + ' ₽</b></div>'
      + '<div class="row" style="justify-content:space-between;margin-bottom:6px"><span class="muted">🎉 Желания (' + b + '%)</span><b class="money">' + money(sum * b / 100) + ' ₽</b></div>'
      + '<div class="row" style="justify-content:space-between"><span class="muted">🐷 Копилка (' + c + '%)</span><b class="money" style="color:var(--accent)">' + money(sum * c / 100) + ' ₽</b></div>'
      + '</div>' : '';
  }
  function onAction(el) { }
  function _wire() {
    ['f-sum', 'f-a', 'f-b'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.addEventListener('input', function () {
        if (el.id !== 'f-sum') { db[el.id === 'f-a' ? 'a' : 'b'] = parseInt(el.value, 10) || 0; save(); }
        else { db.sum = el.value; }
        upd();
      });
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
