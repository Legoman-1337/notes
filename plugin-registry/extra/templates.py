# -*- coding: utf-8 -*-
"""Шаблоны plugin.js для полезных плагинов «Удобных заметок» (v3.5 UI)."""
import json


def _j(v):
    return json.dumps(v, ensure_ascii=False)


def _js_list(items):
    return "[" + ",".join(_j(x) for x in items) + "]"


HELPERS = """
  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\\B(?=(\\d{3})+(?!\\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}
"""

HEADER = """/* Плагин «{name}» ({id}) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {{
  var app = null, root = null;
  var db = {db};
{helpers}
"""

PREFILL = "    if (typeof _prefill === 'function') _prefill();"
EXTRA_WIRING = "    if (typeof _wire === 'function') _wire();"


def wrap(body, db="{ }"):
    return (HEADER.format(name="%NAME%", id="%ID%", db=db, helpers=HELPERS)
            + body
            + """
  function activate(api) {
    app = api;
    try {
      var loaded = JSON.parse(app.load() || '{}');
      if (loaded && typeof loaded === 'object') { for (var k in db) { if (loaded[k] !== undefined) db[k] = loaded[k]; } }
    } catch (e) {}
""" + PREFILL + """
    root = document.getElementById('root');
    root.addEventListener('click', function (ev) {
      var el = ev.target.closest ? ev.target.closest('[data-act]') : null;
      if (el) { ev.preventDefault(); onAction(el); }
    });
""" + EXTRA_WIRING + """
    render();
  }
  function deactivate() { if (app) save(); root = null; app = null; }
  window.plugin = { activate: activate, deactivate: deactivate };
})();
""")


def _stub():
    return """
  function _prefill() {}
  function _wire() {}
"""


def user_random(emoji, btn, item_placeholder, hint="", preset=None):
    preset = preset or []
    body = """
  var EMOJI = %EMOJI%; var BTN = %BTN%; var HINT = %HINT%;
  var PRESET = %PRESET%;
  function _prefill() { if (PRESET.length && (!db.items || !db.items.length)) { db.items = PRESET.slice(); save(); } }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div style="font-size:19px;font-weight:800;min-height:32px;margin-bottom:12px">' + (db.last ? esc(db.last) : '<span class="muted" style="font-weight:500;font-size:15px">Нажмите кнопку ниже</span>') + '</div>'
      + '<button class="btn" data-act="go"' + ((!db.items || !db.items.length) ? ' disabled' : '') + '>' + BTN + '</button>'
      + '</div>';
    html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Ваш список идей</div>';
    (db.items || []).forEach(function (t, i) {
      html += '<div class="row" style="margin-bottom:6px"><span class="chip grow" style="text-align:left">' + esc(t) + '</span>'
        + '<button class="btn danger small" data-act="del" data-i="' + i + '">✕</button></div>';
    });
    if (!db.items || !db.items.length) html += '<p class="muted" style="font-size:12px;margin:4px 0 8px">' + (HINT || 'Добавьте хотя бы один пункт') + '</p>';
    html += '<div class="row" style="margin-top:8px">'
      + '<input type="text" id="f-item" class="grow" placeholder="' + esc(IP) + '">'
      + '<button class="btn" data-act="add">+</button></div></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'add') {
      var v = val('f-item').trim();
      if (!v) return;
      db.items = db.items || []; db.items.push(v); save(); return render();
    }
    if (act === 'del') {
      var i = parseInt(el.getAttribute('data-i'), 10);
      db.items.splice(i, 1); if (db.last && db.items.indexOf(db.last) < 0) db.last = '';
      save(); return render();
    }
    if (act === 'go') {
      if (!db.items || !db.items.length) return;
      db.last = db.items[Math.floor(Math.random() * db.items.length)];
      save(); return render();
    }
  }
""" + _stub()
    return wrap(body
                .replace("%EMOJI%", _j(emoji))
                .replace("%BTN%", _j(btn))
                .replace("%HINT%", _j(hint))
                .replace("%PRESET%", _js_list(preset))
                .replace("IP", _j(item_placeholder)), db="{items:[],last:''}")


def day_counter(emoji, unit, goal, mode="add"):
    body = """
  var EMOJI = %EMOJI%; var UNIT = %UNIT%; var GOAL = %GOAL%; var MODE = %MODE%;
  function n() { return db.days && db.days[today(0)] ? db.days[today(0)] : 0; }
  function add(v) {
    db.days = db.days || {};
    db.days[today(0)] = Math.max(0, n() + v);
    save(); render();
  }
  function setVal(v) {
    v = parseInt(v, 10) || 0; if (v < 0) v = 0;
    db.days = db.days || {}; db.days[today(0)] = v; save(); render();
  }
  function render() {
    if (!root) return;
    var cur = n();
    var pct = GOAL > 0 ? Math.min(100, Math.round(cur * 100 / GOAL)) : (cur > 0 ? 100 : 0);
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div class="hero-num money">' + cur + (GOAL > 0 ? '<span style="font-size:16px;font-weight:500" class="muted"> / ' + GOAL + '</span>' : '') + '</div>'
      + '<div class="muted" style="font-size:12px;margin:2px 0 12px">' + esc(UNIT) + ' сегодня</div>'
      + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div>'
      + '</div>';
    if (MODE === 'add') {
      html += '<div class="card" style="text-align:center;padding:12px">'
        + '<button class="btn" data-act="plus" style="min-width:100px">+1</button> '
        + '<button class="btn secondary" data-act="minus" style="min-width:72px">−1</button></div>';
    } else {
      html += '<div class="card"><div class="row">'
        + '<input type="number" id="f-n" class="grow" placeholder="Сколько за сегодня" inputmode="numeric">'
        + '<button class="btn" data-act="set">Записать</button></div></div>';
    }
    html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Последние 7 дней</div><div class="row" style="justify-content:space-between">';
    for (var i = 6; i >= 0; i--) {
      var k = today(i), v = (db.days && db.days[k]) || 0;
      var cls = v >= GOAL && GOAL > 0 ? 'color:var(--accent);font-weight:700' : '';
      html += '<div style="text-align:center;min-width:34px"><div style="' + cls + '" class="money">' + v + '</div><div class="muted" style="font-size:10px">' + dlabel(i) + '</div></div>';
    }
    html += '</div></div>';
    var total = 0; for (var k in db.days) total += db.days[k] || 0;
    html += '<div class="muted" style="text-align:center;font-size:12px">Всего: ' + money(total) + '</div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'plus') add(1);
    else if (act === 'minus') add(-1);
    else if (act === 'set') setVal(val('f-n'));
  }
""" + _stub()
    return wrap(body.replace("%EMOJI%", _j(emoji)).replace("%UNIT%", _j(unit))
                .replace("%GOAL%", str(goal)).replace("%MODE%", _j(mode)), db="{days:{}}")


def streak(emoji, label):
    body = """
  var EMOJI = %EMOJI%; var LABEL = %LABEL%;
  function render() {
    if (!root) return;
    var streak = 0;
    for (var i = 0; i < 4000; i++) { if (db.days && db.days[today(i)]) streak++; else break; }
    var doneToday = !!(db.days && db.days[today(0)]);
    var html = '<div class="card hero">'
      + '<div class="hero-icon">' + EMOJI + '</div>'
      + '<div class="hero-num money">' + streak + '</div>'
      + '<div class="muted" style="font-size:12px;margin-bottom:14px">' + esc(LABEL) + ' · дней подряд</div>';
    html += '<div style="display:flex;gap:5px;justify-content:center;flex-wrap:wrap;margin-bottom:16px">';
    for (var i = 13; i >= 0; i--) {
      var on = db.days && db.days[today(i)];
      html += '<span style="width:17px;height:17px;border-radius:6px;display:inline-block;'
        + (on ? 'background:var(--accent)' : 'background:var(--surface2);border:1px solid var(--border)') + '"></span>';
    }
    html += '</div>';
    html += '<button class="btn' + (doneToday ? ' secondary' : '') + '" data-act="toggle" style="min-width:210px">'
      + (doneToday ? 'Снять отметку за сегодня' : 'Отметить сегодня ✓') + '</button></div>';
    var total = 0; for (var k in db.days) total++;
    html += '<div class="muted" style="text-align:center;font-size:12px">Всего отмечено дней: ' + total + '</div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'toggle') return;
    db.days = db.days || {};
    if (db.days[today(0)]) delete db.days[today(0)]; else db.days[today(0)] = 1;
    save(); render();
  }
""" + _stub()
    return wrap(body.replace("%EMOJI%", _j(emoji)).replace("%LABEL%", _j(label)), db="{days:{}}")


def list_check(emoji, place1, place2=None, done_label="Сделано", add_btn="+ Пункт",
               preset=None, has_done=True, item_hint="Пока пусто — добавьте первый пункт"):
    preset = preset or []
    body = """
  var EMOJI = %EMOJI%; var P1 = %P1%; var P2 = %P2%; var DONE = %DONE%; var ADDBTN = %ADDBTN%;
  var PRESET = %PRESET%; var HASDONE = %HASDONE%; var EMPTYHINT = %EMPTY%;
  function _prefill() {
    if (PRESET.length && (!db.items || !db.items.length)) {
      db.items = PRESET.map(function (t) { return { id: uid(), t: t, note: '', done: false }; });
      save();
    }
  }
  function render() {
    if (!root) return;
    var html = '';
    if (!db.items || !db.items.length) {
      html += '<div class="card hero"><div class="hero-icon">' + EMOJI + '</div><p class="muted" style="margin:4px 0">' + esc(EMPTYHINT) + '</p></div>';
    }
    (db.items || []).forEach(function (it) {
      html += '<div class="card" style="padding:10px 13px;' + (it.done ? 'opacity:.58' : '') + '">'
        + '<div class="row" style="gap:10px">'
        + (HASDONE ? '<button class="btn small' + (it.done ? '' : ' secondary') + '" data-act="toggle" data-id="' + esc(it.id) + '" style="min-width:42px">' + (it.done ? '↩' : '✓') + '</button>' : '')
        + '<div class="grow" style="min-width:0"><b style="' + (it.done ? 'text-decoration:line-through' : '') + '">' + esc(it.t) + '</b>'
        + (it.note ? '<div class="muted" style="font-size:12px;margin-top:2px">' + esc(it.note) + '</div>' : '')
        + '</div>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(it.id) + '">✕</button>'
        + '</div></div>';
    });
    var done = 0; (db.items || []).forEach(function (i) { if (i.done) done++; });
    if (HASDONE && db.items && db.items.length) {
      var pct = Math.round(done * 100 / db.items.length);
      html += '<div class="card" style="padding:11px 14px"><div class="row" style="justify-content:space-between;margin-bottom:6px"><span class="muted" style="font-size:12px">' + esc(DONE) + '</span><b class="money" style="font-size:12px">' + done + ' из ' + db.items.length + '</b></div>'
        + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div></div>';
    }
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">' + esc(ADDBTN) + '</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="' + esc(P1) + '" style="width:100%;margin-bottom:8px">'
        + (P2 ? '<input type="text" id="f-note" placeholder="' + esc(P2) + '" style="width:100%;margin-bottom:8px">' : '')
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'toggle') {
      (db.items || []).forEach(function (i) { if (i.id === id) i.done = !i.done; });
      save(); return render();
    }
    if (act === 'del') {
      db.items = (db.items || []).filter(function (i) { return i.id !== id; });
      save(); return render();
    }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите текст'); return; }
      var note = P2 ? val('f-note').trim() : '';
      db.items = db.items || [];
      db.items.unshift({ id: uid(), t: t, note: note, done: false });
      db.showAdd = false; save(); app.toast('Добавлено'); return render();
    }
  }
""" + _stub()
    return wrap(body
                .replace("%EMOJI%", _j(emoji))
                .replace("%P1%", _j(place1))
                .replace("%P2%", _j(place2) if place2 else "null")
                .replace("%DONE%", _j(done_label))
                .replace("%ADDBTN%", _j(add_btn))
                .replace("%PRESET%", _js_list(preset))
                .replace("%HASDONE%", "true" if has_done else "false")
                .replace("%EMPTY%", _j(item_hint)), db="{items:[],showAdd:false}")