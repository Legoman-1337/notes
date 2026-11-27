/* Плагин «Вес» (weight-diary) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {items:[]};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


  function render() {
    if (!root) return;
    var items = db.items || [];
    var html = '<div class="card"><div class="row">'
      + '<input type="number" id="f-w" class="grow" placeholder="Вес сегодня, кг" step="0.1" inputmode="decimal">'
      + '<button class="btn" data-act="add">Записать</button></div></div>';
    if (items.length) {
      var first = items[0].kg, prev = items.length > 1 ? items[1].kg : null;
      var delta = prev != null ? Math.round((first - prev) * 10) / 10 : null;
      html += '<div class="card"><div class="row" style="justify-content:center;gap:24px;margin-bottom:10px">'
        + '<div style="text-align:center"><div class="hero-num money" style="font-size:28px">' + first + '</div><div class="muted" style="font-size:11px">последний кг</div></div>'
        + (delta != null ? '<div style="text-align:center"><div class="hero-num money" style="font-size:28px;color:' + (delta <= 0 ? 'var(--accent)' : 'var(--danger)') + '">' + (delta > 0 ? '+' : '') + delta + '</div><div class="muted" style="font-size:11px">изменение</div></div>' : '')
        + '</div>';
      items.slice(0, 10).forEach(function (it) {
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">' + it.d + '</span><b class="money" style="font-size:13px">' + it.kg + ' кг</b></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'add') return;
    var kg = parseFloat(val('f-w').replace(',', '.'));
    if (!kg || kg < 20 || kg > 400) { app.toast('Введите вес в кг'); return; }
    db.items = db.items || [];
    var d = today(0);
    db.items = db.items.filter(function (i) { return i.d !== d; });
    db.items.unshift({ d: d, kg: Math.round(kg * 10) / 10 });
    db.items.sort(function (a, b) { return a.d < b.d ? 1 : -1; });
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
