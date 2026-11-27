/* Плагин «Скидка» (discount-calc) — «Удобные заметки».
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


  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card">'
      + '<input type="number" id="f-p" placeholder="Цена ₽" value="' + (db.price || '') + '" style="width:100%;margin-bottom:8px" inputmode="decimal">'
      + '<div class="row">'
      + '<input type="number" id="f-d" class="grow" placeholder="Скидка %" value="' + (db.pct || '') + '" inputmode="decimal">'
      + [10, 20, 30, 50].map(function (p) { return '<button class="chip' + ((db.pct || 0) === p ? ' active' : '') + '" data-act="pct" data-p="' + p + '">' + p + '%</button>'; }).join('')
      + '</div></div><div id="f-res"></div>';
    upd();
  }
  function upd() {
    var price = parseFloat(val('f-p').replace(',', '.')) || 0;
    var pct = parseFloat(val('f-d').replace(',', '.')) || 0;
    var off = Math.round(price * pct / 100);
    var box = document.getElementById('f-res');
    if (!box) return;
    box.innerHTML = price ? '<div class="card hero">'
      + '<div class="hero-num money" style="color:var(--accent)">' + money(price - off) + ' ₽</div>'
      + '<div class="muted" style="font-size:13px;margin-top:4px">экономия: <b class="money">' + money(off) + ' ₽</b></div></div>' : '';
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'pct') return;
    var p = el.getAttribute('data-p');
    var el2 = document.getElementById('f-d');
    if (el2) { el2.value = p; upd(); }
  }
  function _wire() {
    ['f-p', 'f-d'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) el.addEventListener('input', upd);
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
