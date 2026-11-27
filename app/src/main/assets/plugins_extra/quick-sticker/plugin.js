/* Плагин «Стикер» (quick-sticker) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {text:''};

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
      + '<textarea id="f-txt" placeholder="Запишите, что не хочется забыть… сохраняется само" style="width:100%;min-height:220px;resize:vertical;box-sizing:border-box">' + esc(db.text || '') + '</textarea>'
      + '<div class="row" style="justify-content:space-between;margin-top:8px" id="f-meta">'
      + '<span class="muted" style="font-size:11px">' + ((db.text || '').length ? 'сохранено ✓' : 'пока пусто') + '</span>'
      + '<span class="muted" style="font-size:11px">' + (db.text || '').length + ' симв.</span></div>'
      + '<button class="btn danger small" data-act="clear" style="margin-top:8px">Очистить</button></div>';
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'clear') return;
    db.text = '';
    save(); render();
  }
  function _wire() {
    var el = document.getElementById('f-txt');
    if (el) el.addEventListener('input', function () {
      db.text = el.value;
      save();
      var meta = document.getElementById('f-meta');
      if (meta) meta.innerHTML = '<span class="muted" style="font-size:11px">сохранено ✓</span>'
        + '<span class="muted" style="font-size:11px">' + el.value.length + ' симв.</span>';
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
