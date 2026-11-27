/* Плагин «Дневник настроения» (mood-diary) — «Удобные заметки».
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


  var MOODS = [['😄','радость'], ['🙂','хорошо'], ['😐','обычно'], ['😕','грустно'], ['😔','тяжело']];
  function render() {
    if (!root) return;
    var t = today(0);
    var cur = db.days && db.days[t];
    var html = '<div class="card hero" style="padding:18px 14px">'
      + '<div class="muted" style="font-size:12px;margin-bottom:10px">Как настроение сегодня?</div>'
      + '<div class="row" style="justify-content:center;gap:8px">';
    MOODS.forEach(function (m) {
      var on = cur && cur.m === m[0];
      html += '<button data-act="mood" data-m="' + m[0] + '" style="font-size:' + (on ? '34px' : '26px') + ';background:' + (on ? 'var(--surface2)' : 'transparent') + ';border:' + (on ? '2px solid var(--accent)' : '2px solid transparent') + ';border-radius:14px;padding:6px 8px;cursor:pointer">' + m[0] + '</button>';
    });
    html += '</div>'
      + '<input type="text" id="f-note" placeholder="Пара слов о дне (необязательно)" value="' + esc(cur ? cur.n || '' : '') + '" style="width:100%;margin-top:12px">'
      + '<button class="btn" data-act="save" style="width:100%;margin-top:8px">Сохранить день</button>'
      + '</div>';
    var keys = Object.keys(db.days || {}).sort().reverse().slice(0, 14);
    if (keys.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Последние дни</div>';
      keys.forEach(function (k) {
        var d = db.days[k];
        html += '<div class="row" style="margin-bottom:8px"><span style="font-size:24px">' + d.m + '</span>'
          + '<div class="grow" style="min-width:0"><div class="muted" style="font-size:11px">' + k + '</div>'
          + (d.n ? '<div style="font-size:13px">' + esc(d.n) + '</div>' : '') + '</div></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'mood') {
      var m = el.getAttribute('data-m');
      db.days = db.days || {};
      var cur = db.days[today(0)] || { n: '' };
      cur.m = m; db.days[today(0)] = cur;
      save(); return render();
    }
    if (act === 'save') {
      db.days = db.days || {};
      var cur = db.days[today(0)] || { m: '🙂' };
      cur.n = val('f-note').trim();
      db.days[today(0)] = cur;
      save(); app.toast('День сохранён ' + cur.m);
      return render();
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
