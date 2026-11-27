/* Плагин «Разделить счёт» (bill-split) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {tip:false,sum:0,n:0};

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
      + '<input type="number" id="f-sum" placeholder="Общий счёт ₽" value="' + (db.sum || '') + '" style="width:100%;margin-bottom:8px" inputmode="decimal">'
      + '<div class="row">'
      + '<input type="number" id="f-n" class="grow" placeholder="Сколько человек" value="' + (db.n || '') + '" inputmode="numeric">'
      + '<button class="chip' + (db.tip ? ' active' : '') + '" data-act="tip">+10% чаевые</button></div>'
      + '</div><div id="f-res"></div>';
    upd();
  }
  function upd() {
    var sum = parseFloat(val('f-sum').replace(',', '.')) || 0;
    var n = parseInt(val('f-n'), 10) || 0;
    var all = sum + (db.tip ? Math.round(sum * 10 / 100) : 0);
    var each = n > 0 ? Math.ceil(all / n) : 0;
    var box = document.getElementById('f-res');
    if (!box) return;
    box.innerHTML = '<div class="card hero">'
      + '<div class="muted" style="font-size:12px">каждый платит</div>'
      + '<div class="hero-num money">' + money(each) + ' ₽</div>'
      + (n > 0 && sum ? '<div class="muted" style="font-size:12px;margin-top:4px">всего: ' + money(all) + ' ₽</div>' : '')
      + '</div>';
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'tip') return;
    db.tip = !db.tip; save(); render();
  }
  function _wire() {
    ['f-sum', 'f-n'].forEach(function (id) {
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
