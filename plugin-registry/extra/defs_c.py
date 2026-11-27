# -*- coding: utf-8 -*-
"""Полезные плагины, группа 3: финансы, здоровье, время, учёба и утилиты."""
from templates import wrap, list_check, streak, day_counter


def custom(body, db="{ }"):
    return wrap(body, db)


def plugins():
    P = []

    # ------------------------------- Финансы
    P.append(dict(
        id="spend-diary", name="Траты дня", icon="💸", cat="Деньги",
        desc="Быстрый дневник расходов за день и сравнение с прошлыми днями",
        js=custom("""
  function render() {
    if (!root) return;
    var t = today(0);
    var list = (db.days && db.days[t]) || [];
    var total = list.reduce(function (s, i) { return s + (i.a || 0); }, 0);
    var html = '<div class="card hero">'
      + '<div class="muted" style="font-size:12px">потрачено сегодня</div>'
      + '<div class="hero-num money">' + money(total) + ' ₽</div></div>';
    html += '<div class="card"><div class="row">'
      + '<input type="text" id="f-t" class="grow" placeholder="На что">'
      + '<input type="number" id="f-a" placeholder="₽" style="width:90px" inputmode="numeric">'
      + '<button class="btn" data-act="add">+</button></div></div>';
    if (list.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Сегодня</div>';
      list.forEach(function (i, idx) {
        html += '<div class="row" style="margin-bottom:6px"><div class="grow" style="font-size:14px">' + esc(i.t) + '</div>'
          + '<b class="money" style="font-size:14px">' + money(i.a) + ' ₽</b>'
          + '<button class="btn danger small" data-act="del" data-i="' + idx + '">✕</button></div>';
      });
      html += '</div>';
    }
    var keys = Object.keys(db.days || {}).sort().reverse().slice(1, 5);
    if (keys.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:6px;font-weight:600">Прошлые дни</div>';
      keys.forEach(function (k) {
        var s = (db.days[k] || []).reduce(function (a, i) { return a + (i.a || 0); }, 0);
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">' + k + '</span><b class="money" style="font-size:13px">' + money(s) + ' ₽</b></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var t = today(0);
    if (act === 'add') {
      var what = val('f-t').trim();
      var a = parseInt(val('f-a'), 10) || 0;
      if (!what || !a) { app.toast('Нужны название и сумма'); return; }
      db.days = db.days || {};
      db.days[t] = db.days[t] || [];
      db.days[t].unshift({ t: what, a: a });
      save(); return render();
    }
    if (act === 'del') {
      var idx = parseInt(el.getAttribute('data-i'), 10);
      db.days[t].splice(idx, 1);
      save(); return render();
    }
  }
""", db="{days:{}}")))

    P.append(dict(
        id="income-log", name="Доходы", icon="💵", cat="Деньги",
        desc="Аккуратный журнал поступлений с подсчётом общей суммы",
        js=custom("""
  function render() {
    if (!root) return;
    var total = (db.items || []).reduce(function (s, i) { return s + (parseInt(i.a, 10) || 0); }, 0);
    var html = '<div class="card hero">'
      + '<div class="muted" style="font-size:12px">всего поступлений</div>'
      + '<div class="hero-num money" style="color:var(--accent)">' + money(total) + ' ₽</div>'
      + '<div class="muted" style="font-size:12px;margin-top:4px">записей: ' + (db.items || []).length + '</div></div>';
    (db.items || []).forEach(function (i) {
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:10px">'
        + '<div class="grow" style="min-width:0"><b>' + esc(i.t) + '</b>'
        + (i.note ? '<div class="muted" style="font-size:12px">' + esc(i.note) + '</div>' : '') + '</div>'
        + '<b class="money" style="color:var(--accent)">' + money(i.a) + ' ₽</b>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(i.id) + '">✕</button></div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Новое поступление</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Откуда (аванс, возврат…)" style="width:100%;margin-bottom:8px">'
        + '<div class="row">'
        + '<input type="number" id="f-a" class="grow" placeholder="Сумма ₽" inputmode="numeric">'
        + '<input type="text" id="f-note" placeholder="Дата" style="width:100px"></div>'
        + '<div class="row" style="margin-top:8px"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'del') { db.items = (db.items || []).filter(function (i) { return i.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      var a = parseInt(val('f-a'), 10) || 0;
      if (!t || !a) { app.toast('Нужны источник и сумма'); return; }
      db.items = db.items || [];
      db.items.unshift({ id: uid(), t: t, a: a, note: val('f-note').trim() });
      db.showAdd = false; save(); app.toast('Записано'); return render();
    }
  }
""", db="{items:[],showAdd:false}")))

    P.append(dict(
        id="piggy-bank", name="Копилка мечты", icon="🐷", cat="Деньги",
        desc="Финансовая цель и прогресс откладываний",
        js=custom("""
  function render() {
    if (!root) return;
    if (!db.goal) {
      root.innerHTML = '<div class="card hero">'
        + '<div class="hero-icon">🐷</div>'
        + '<input type="text" id="f-name" placeholder="На что копим?" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><input type="number" id="f-goal" class="grow" placeholder="Сумма цели ₽" inputmode="numeric">'
        + '<button class="btn" data-act="set">Начать</button></div></div>';
      return;
    }
    var pct = Math.min(100, Math.round((db.saved || 0) * 100 / db.goal));
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🐷</div>'
      + '<b style="font-size:17px">' + esc(db.name || 'Мечта') + '</b>'
      + '<div class="hero-num money" style="margin-top:6px">' + money(db.saved || 0) + ' ₽</div>'
      + '<div class="muted" style="font-size:12px;margin-bottom:12px">из ' + money(db.goal) + ' ₽ · ' + pct + '%</div>'
      + '<div class="prog-track"><div class="prog-fill" style="width:' + pct + '%"></div></div>'
      + '</div>'
      + '<div class="card"><div class="row">'
      + '<input type="number" id="f-add" class="grow" placeholder="Отложить ₽" inputmode="numeric">'
      + '<button class="btn" data-act="add">Отложить</button></div></div>';
    if (db.log && db.log.length) {
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:6px;font-weight:600">История</div>'
        + db.log.slice(0, 5).map(function (l) { return '<div class="muted" style="font-size:12px;margin:2px 0">' + esc(l) + '</div>'; }).join('') + '</div>';
    }
    html += '<div class="card" style="text-align:center"><button class="btn danger small" data-act="reset">Начать новую цель</button></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'set') {
      var g = parseInt(val('f-goal'), 10) || 0;
      if (!g) { app.toast('Введите сумму цели'); return; }
      db.goal = g; db.name = val('f-name').trim(); db.saved = 0; db.log = [];
      save(); return render();
    }
    if (act === 'add') {
      var a = parseInt(val('f-add'), 10) || 0;
      if (!a) { app.toast('Сколько откладываем?'); return; }
      db.saved = (db.saved || 0) + a;
      db.log = db.log || [];
      db.log.unshift(today(0).slice(5) + ': ' + (a > 0 ? '+' : '') + money(a) + ' ₽' + (db.saved >= db.goal ? ' — цель достигнута! 🎉' : ''));
      save();
      if (db.saved >= db.goal) app.toast('Мечта собрана! 🎉');
      return render();
    }
    if (act === 'reset') { db.goal = 0; db.saved = 0; db.name = ''; db.log = []; save(); return render(); }
  }
""", db="{goal:0,name:'',saved:0,log:[]}")))

    P.append(dict(
        id="subscriptions-list", name="Подписки", icon="📺", cat="Деньги",
        desc="Учёт ежемесячных подписок и списаний — ничего лишнего",
        js=list_check("📺", "Подписка", "Цена / день списания", done_label="Отменили", has_done=True,
                      add_btn="+ Подписка", item_hint="Добавьте подписки и даты списаний")))

    P.append(dict(
        id="discount-calc", name="Скидка", icon="💯", cat="Деньги",
        desc="Итоговая цена со скидкой и сумма экономии",
        js=custom("""
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
""")))

    P.append(dict(
        id="bill-split", name="Разделить счёт", icon="➗", cat="Деньги",
        desc="Счёт на компанию — сколько платит каждый",
        js=custom("""
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
""", db="{tip:false,sum:0,n:0}")))

    P.append(dict(
        id="salary-plan", name="Расклад зарплаты", icon="💰", cat="Деньги",
        desc="Планирование дохода 50/30/20 с гибкой настройкой процентов",
        js=custom("""
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
""", db="{sum:'',a:50,b:30}")))

    P.append(dict(
        id="currency-memo", name="Валюты", icon="💱", cat="Деньги",
        desc="Перевод рублей в валюту и обратно по своему курсу без интернета",
        js=custom("""
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
""", db="{rate:0}")))

    # ------------------------------- Здоровье
    P.append(dict(
        id="water-tracker", name="Вода", icon="💧", cat="Здоровье",
        desc="Учёт выпитой воды за день с историей за неделю",
        js=day_counter("💧", "стаканов", 8)))

    P.append(dict(
        id="pills-check", name="Приём-чек", icon="✅", cat="Здоровье",
        desc="Отметка ежедневного приёма витаминов или лекарств",
        js=streak("✅", "приём отмечен")))

    P.append(dict(
        id="sleep-diary", name="Дневник сна", icon="😴", cat="Здоровье",
        desc="Лёг / встал → точная длительность сна и среднее за неделю",
        js=custom("""
  function hours(sleep, wake) {
    var p = sleep.split(':'), q = wake.split(':');
    if (p.length < 2 || q.length < 2) return 0;
    var s = parseInt(p[0], 10) * 60 + parseInt(p[1], 10);
    var w = parseInt(q[0], 10) * 60 + parseInt(q[1], 10);
    var d = w - s; if (d <= 0) d += 24 * 60;
    return Math.round(d / 60 * 10) / 10;
  }
  function render() {
    if (!root) return;
    var html = '<div class="card">'
      + '<div class="row" style="margin-bottom:8px"><span class="chip" style="min-width:86px">Легли</span>'
      + '<input type="time" id="f-s" class="grow" value="' + (db.lastS || '23:00') + '"></div>'
      + '<div class="row" style="margin-bottom:10px"><span class="chip" style="min-width:86px">Встали</span>'
      + '<input type="time" id="f-w" class="grow" value="' + (db.lastW || '07:00') + '"></div>'
      + '<button class="btn" data-act="add" style="width:100%">Записать сон 😴</button></div>';
    var keys = Object.keys(db.nights || {}).sort().reverse().slice(0, 7);
    if (keys.length) {
      var avg = keys.reduce(function (s, k) { return s + db.nights[k]; }, 0) / keys.length;
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Последние ночи</div>';
      keys.forEach(function (k) {
        var h = db.nights[k];
        var color = h >= 7 ? 'var(--accent)' : (h < 5.5 ? 'var(--danger)' : 'var(--text)');
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">' + k + '</span><b class="money" style="color:' + color + ';font-size:13px">' + h + ' ч</b></div>';
      });
      html += '<div class="muted" style="font-size:12px;margin-top:8px">среднее: <b>' + (Math.round(avg * 10) / 10) + ' ч</b></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'add') return;
    var s = val('f-s'), w = val('f-w');
    var h = hours(s, w);
    if (!h) { app.toast('Укажите время'); return; }
    db.lastS = s; db.lastW = w;
    db.nights = db.nights || {};
    db.nights[today(0)] = h;
    save(); app.toast('Записано: ' + h + ' ч сна'); render();
  }
""", db="{nights:{},lastS:'23:00',lastW:'07:00'}")))

    P.append(dict(
        id="weight-diary", name="Вес", icon="⚖️", cat="Здоровье",
        desc="Дневник взвешиваний с автоматической разницей",
        js=custom("""
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
""", db="{items:[]}")))

    P.append(dict(
        id="breathe-calm", name="Дыхание 4-7-8", icon="🌬️", cat="Здоровье",
        desc="Практика спокойного дыхания: вдох 4 · задержка 7 · выдох 8",
        js=custom("""
  var PHASES = [['Вдох', 4], ['Задержка', 7], ['Выдох', 8]];
  var running = false, phase = 0, left = 0, timer = null;
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🌬️</div>';
    if (running) {
      html += '<div style="font-size:24px;font-weight:800;color:var(--accent)">' + PHASES[phase][0] + '</div>'
        + '<div class="hero-num money" style="font-size:50px">' + left + '</div>'
        + '<div class="muted" style="font-size:12px;margin-bottom:14px">циклов пройдено: ' + (db.cycles || 0) + '</div>'
        + '<button class="btn secondary" data-act="stop">Стоп</button>';
    } else {
      html += '<p class="muted" style="margin:0 0 8px">Вдох на 4 счёта, задержка на 7, выдох на 8.<br>Несколько циклов быстро возвращают спокойствие.</p>'
        + '<div class="muted" style="font-size:12px;margin-bottom:14px">пройдено циклов: ' + (db.cycles || 0) + '</div>'
        + '<button class="btn" data-act="start" style="min-width:170px">Начать</button>';
    }
    html += '</div>';
    root.innerHTML = html;
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) {
      phase++;
      if (phase >= PHASES.length) { phase = 0; db.cycles = (db.cycles || 0) + 1; save(); }
      left = PHASES[phase][1];
    }
    render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'start') {
      running = true; phase = 0; left = PHASES[0][1];
      if (timer) clearInterval(timer);
      timer = setInterval(tick, 1000);
      return render();
    }
    if (act === 'stop') {
      running = false;
      if (timer) clearInterval(timer);
      return render();
    }
  }
""", db="{cycles:0}")))

    # ------------------------------- Учёба, списки и инструменты
    P.append(dict(
        id="pomodoro", name="Помодоро", icon="🍅", cat="Учёба",
        desc="Таймер фокуса: 25 минут работы и 5 минут перерыва",
        js=custom("""
  var FOCUS = 25 * 60, BREAK = 5 * 60;
  var running = false, left = FOCUS, mode = 'focus', timer = null;
  function fmt(s) {
    var m = Math.floor(s / 60), r = s % 60;
    return (m < 10 ? '0' : '') + m + ':' + (r < 10 ? '0' : '') + r;
  }
  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card hero">'
      + '<div class="hero-icon">' + (mode === 'focus' ? '🍅' : '☕') + '</div>'
      + '<div class="muted" style="font-size:12px">' + (mode === 'focus' ? 'время работать' : 'перерыв') + '</div>'
      + '<div class="hero-num money" style="font-size:46px">' + fmt(left) + '</div>'
      + '<div class="row" style="justify-content:center;gap:10px;margin-top:14px">'
      + '<button class="btn' + (running ? ' secondary' : '') + '" data-act="toggle" style="min-width:110px">' + (running ? 'Пауза' : 'Старт') + '</button>'
      + '<button class="btn secondary" data-act="reset">Сброс</button></div>'
      + '<div class="muted" style="font-size:12px;margin-top:12px">🍅 завершено сегодня: ' + ((db.days || {})[today(0)] || 0) + '</div>'
      + '</div>';
  }
  function finish() {
    if (mode === 'focus') {
      db.days = db.days || {};
      db.days[today(0)] = ((db.days || {})[today(0)] || 0) + 1;
      save(); app.toast('🍅 Помодоро готово! 5 минут перерыва');
      mode = 'break'; left = BREAK;
    } else {
      app.toast('Перерыв окончен — за дело!');
      mode = 'focus'; left = FOCUS;
    }
    running = false;
    render();
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) finish(); else render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'toggle') {
      running = !running;
      if (running) { if (timer) clearInterval(timer); timer = setInterval(tick, 1000); }
      else if (timer) clearInterval(timer);
      return render();
    }
    if (act === 'reset') {
      running = false; mode = 'focus'; left = FOCUS;
      if (timer) clearInterval(timer);
      return render();
    }
  }
""", db="{days:{}}")))

    P.append(dict(
        id="timers-mini", name="Таймер", icon="⏱️", cat="Мелочи",
        desc="Удобный таймер на минуты и секунды для кухни и дел",
        js=custom("""
  var left = 0, total = 0, running = false, timer = null;
  function fmt(s) {
    var m = Math.floor(s / 60), r = s % 60;
    return m + ':' + (r < 10 ? '0' : '') + r;
  }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-num money" style="font-size:44px">' + (total || left ? fmt(left) : '0:00') + '</div>';
    if (total) html += '<div class="prog-track" style="margin:10px 14% 12px"><div class="prog-fill" style="width:' + Math.round(left * 100 / total) + '%"></div></div>';
    if (!running) {
      html += '<div class="row" style="justify-content:center;margin:10px 0">'
        + '<input type="number" id="f-m" placeholder="мин" style="width:86px" inputmode="numeric">'
        + '<input type="number" id="f-s" placeholder="сек" style="width:86px" inputmode="numeric"></div>'
        + '<button class="btn" data-act="start" style="min-width:150px">Старт</button>';
    } else {
      html += '<button class="btn secondary" data-act="stop" style="min-width:150px">Стоп</button>';
    }
    if (total && !running) html += '<div style="margin-top:8px"><button class="btn danger small" data-act="reset">Сброс</button></div>';
    html += '</div>';
    root.innerHTML = html;
  }
  function tick() {
    if (!root) { if (timer) clearInterval(timer); return; }
    left--;
    if (left <= 0) {
      left = 0; running = false; clearInterval(timer);
      app.toast('⏰ Время вышло!');
    }
    render();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'start') {
      var m = parseInt(val('f-m'), 10) || 0;
      var s = parseInt(val('f-s'), 10) || 0;
      total = m * 60 + s;
      if (!total) { app.toast('Укажите время'); return; }
      left = total; running = true;
      if (timer) clearInterval(timer);
      timer = setInterval(tick, 1000);
      return render();
    }
    if (act === 'stop') { running = false; if (timer) clearInterval(timer); return render(); }
    if (act === 'reset') { running = false; if (timer) clearInterval(timer); total = 0; left = 0; return render(); }
  }
""")));

    P.append(dict(
        id="stopwatch", name="Секундомер", icon="⏱", cat="Мелочи",
        desc="Секундомер с кругами — для спорта, готовки и замеров",
        js=custom("""
  var running = false, t0 = 0, acc = 0, timer = null, laps = [];
  function now() { return acc + (running ? Date.now() - t0 : 0); }
  function fmt(ms) {
    var s = Math.floor(ms / 1000);
    return Math.floor(s / 60) + ':' + ('0' + s % 60).slice(-2) + ',' + ('0' + Math.floor(ms % 1000 / 10)).slice(-2);
  }
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div id="f-time" class="hero-num money" style="font-size:44px">' + fmt(now()) + '</div>'
      + '<div class="row" style="justify-content:center;gap:10px;margin-top:14px">'
      + '<button class="btn' + (running ? ' secondary' : '') + '" data-act="toggle" style="min-width:110px">' + (running ? 'Стоп' : (acc ? 'Дальше' : 'Старт')) + '</button>'
      + (running ? '<button class="btn secondary" data-act="lap">Круг</button>' : '')
      + (acc && !running ? '<button class="btn secondary" data-act="reset">Сброс</button>' : '')
      + '</div></div>';
    if (laps.length) {
      html += '<div class="card">';
      laps.slice().reverse().forEach(function (l, i) {
        html += '<div class="row" style="justify-content:space-between;margin-bottom:4px"><span class="muted" style="font-size:12px">Круг ' + (laps.length - i) + '</span><b class="money" style="font-size:13px">' + fmt(l) + '</b></div>';
      });
      html += '</div>';
    }
    root.innerHTML = html;
  }
  function tick() {
    if (!root || !running) { if (timer) clearInterval(timer); return; }
    var t = document.getElementById('f-time');
    if (t) t.textContent = fmt(now());
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'toggle') {
      if (running) { acc = now(); running = false; if (timer) clearInterval(timer); }
      else { t0 = Date.now(); running = true; if (timer) clearInterval(timer); timer = setInterval(tick, 60); }
      return render();
    }
    if (act === 'lap') { laps.push(now()); return render(); }
    if (act === 'reset') { running = false; if (timer) clearInterval(timer); acc = 0; laps = []; return render(); }
  }
""")));

    P.append(dict(
        id="date-diff", name="Между датами", icon="📅", cat="Мелочи",
        desc="Точный расчёт дней, недель и месяцев между двумя датами",
        js=custom("""
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
""", db="{a:'',b:''}")))

    P.append(dict(
        id="age-calc", name="Возраст", icon="🎂", cat="Мелочи",
        desc="Точный возраст в годах и днях + обратный отсчёт до дня рождения",
        js=custom("""
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
""", db="{d:''}")))

    P.append(dict(
        id="unit-conv", name="Конвертер", icon="📐", cat="Мелочи",
        desc="Вес, длина, объём и температура в одном месте",
        js=custom("""
  var CATS = {
    'Вес': [['кг', 1], ['фунт', 0.4536], ['унция', 0.02835]],
    'Длина': [['см', 1], ['дюйм', 2.54], ['метр', 100], ['фут', 30.48]],
    'Объём': [['литр', 1], ['галлон', 3.785], ['чашка', 0.25]],
    'Температура': [['°C', 'c'], ['°F', 'f']]
  };
  function render() {
    if (!root) return;
    var cats = Object.keys(CATS);
    var cat = CATS[db.cat] ? db.cat : cats[0];
    var units = CATS[cat];
    var html = '<div class="card"><div class="row" style="flex-wrap:wrap;gap:6px;justify-content:center">';
    cats.forEach(function (c) {
      html += '<button class="chip' + (c === cat ? ' active' : '') + '" data-act="cat" data-c="' + c + '">' + c + '</button>';
    });
    html += '</div></div><div class="card">'
      + '<div class="row" style="margin-bottom:8px">'
      + '<input type="number" id="f-v" class="grow" placeholder="значение" step="any" inputmode="decimal">'
      + '<select id="f-from" style="width:108px">' + units.map(function (u, i) { return '<option value="' + i + '"' + (i === 0 ? ' selected' : '') + '>' + u[0] + '</option>'; }).join('') + '</select></div>'
      + '<div class="row" style="justify-content:center;margin-bottom:8px"><button class="btn secondary small" data-act="swap">⇅ поменять</button></div>'
      + '<div class="row">'
      + '<div class="grow" style="text-align:center;font-size:20px;font-weight:800;color:var(--accent);min-height:28px;align-self:center" id="f-out">…</div>'
      + '<select id="f-to" style="width:108px">' + units.map(function (u, i) { return '<option value="' + i + '"' + (i === 1 ? ' selected' : '') + '>' + u[0] + '</option>'; }).join('') + '</select></div>'
      + '</div>';
    root.innerHTML = html;
    wire();
  }
  function conv(v, fi, ti, cat) {
    var units = CATS[cat];
    if (cat === 'Температура') {
      if (fi === ti) return v;
      if (fi === 0 && ti === 1) return Math.round((v * 9 / 5 + 32) * 10) / 10;
      return Math.round((v - 32) * 5 / 9 * 10) / 10;
    }
    return Math.round(v * units[fi][1] / units[ti][1] * 1000) / 1000;
  }
  function upd() {
    var out = document.getElementById('f-out');
    if (!out) return;
    var v = parseFloat(val('f-v'));
    if (isNaN(v)) { out.textContent = '…'; return; }
    var cat = CATS[db.cat] ? db.cat : Object.keys(CATS)[0];
    var fi = parseInt(val('f-from'), 10) || 0;
    var ti = parseInt(val('f-to'), 10) || 0;
    out.textContent = conv(v, fi, ti, cat) + ' ' + CATS[cat][ti][0];
  }
  function wire() {
    ['f-v', 'f-from', 'f-to'].forEach(function (id) {
      var el = document.getElementById(id);
      if (el) { el.addEventListener('input', upd); el.addEventListener('change', upd); }
    });
    upd();
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'cat') { db.cat = el.getAttribute('data-c'); return render(); }
    if (act === 'swap') {
      var a = document.getElementById('f-from'), b = document.getElementById('f-to');
      var t = a.selectedIndex; a.selectedIndex = b.selectedIndex; b.selectedIndex = t;
      return upd();
    }
  }
  function _wire() { wire(); }
""", db="{cat:'Вес'}")))

    P.append(dict(
        id="password-gen", name="Паролик", icon="🔐", cat="Мелочи",
        desc="Генератор надёжных паролей нужной длины",
        js=custom("""
  var CHARS = 'abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ';
  var DIGITS = '23456789';
  var SPECIAL = '!@#$%&*';
  function render() {
    if (!root) return;
    root.innerHTML = '<div class="card hero">'
      + '<div style="font-size:20px;font-weight:800;font-family:monospace;word-break:break-all;background:var(--surface2);border:1px solid var(--border);border-radius:12px;padding:14px 10px;min-height:50px">' + (db.pw || '—') + '</div>'
      + '</div>'
      + '<div class="card"><div class="row" style="margin-bottom:10px"><span class="chip" style="min-width:96px">Длина</span>'
      + '<input type="number" id="f-len" class="grow" value="' + (db.len || 12) + '" min="4" max="40" inputmode="numeric"></div>'
      + '<div class="row" style="gap:8px;justify-content:center;flex-wrap:wrap;margin-bottom:10px">'
      + '<button class="chip' + (db.dig !== false ? ' active' : '') + '" data-act="dig">Цифры 123</button>'
      + '<button class="chip' + (db.spec ? ' active' : '') + '" data-act="spec">Символы !@#</button>'
      + '</div><button class="btn" data-act="go" style="width:100%">Сгенерировать 🔐</button></div>';
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    if (act === 'dig') { db.dig = db.dig === false; save(); return render(); }
    if (act === 'spec') { db.spec = !db.spec; save(); return render(); }
    if (act !== 'go') return;
    var len = parseInt(val('f-len'), 10) || 12;
    if (len < 4) len = 4; if (len > 40) len = 40;
    db.len = len;
    var pool = CHARS + (db.dig !== false ? DIGITS : '') + (db.spec ? SPECIAL : '');
    var out = '';
    for (var i = 0; i < len; i++) out += pool[Math.floor(Math.random() * pool.length)];
    db.pw = out;
    save(); render();
  }
""", db="{pw:'',len:12,dig:true,spec:false}")))

    P.append(dict(
        id="books-list", name="Прочитать", icon="📚", cat="Учёба",
        desc="Список книг — прочитанные отмечаются галочкой",
        js=list_check("📚", "Название книги", "Автор", done_label="Прочитано",
                      add_btn="+ Книга", item_hint="Добавьте книги, которые хотите прочитать")))

    P.append(dict(
        id="movies-list", name="Посмотреть", icon="🎬", cat="Учёба",
        desc="Список фильмов и сериалов на вечер",
        js=list_check("🎬", "Название", "Где смотреть / кто советовал", done_label="Посмотрели",
                      add_btn="+ Фильм", item_hint="Добавьте фильмы «на потом»")))

    P.append(dict(
        id="series-tracker", name="Сериалы", icon="📺", cat="Учёба",
        desc="На каком сезоне и серии остановились",
        js=custom("""
  function render() {
    if (!root) return;
    var html = '';
    if (!db.shows || !db.shows.length) {
      html += '<div class="card hero"><div class="hero-icon">📺</div><p class="muted" style="margin:4px 0">Добавьте сериалы, чтобы помнить серию</p></div>';
    }
    (db.shows || []).forEach(function (s) {
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:8px">'
        + '<div class="grow" style="min-width:0"><b style="font-size:14px">' + esc(s.t) + '</b></div>'
        + '<div class="row" style="gap:4px;align-items:center">'
        + '<span class="chip money">S' + s.s + ' · E' + s.e + '</span>'
        + '<button class="btn small secondary" data-act="minus" data-id="' + esc(s.id) + '">−</button>'
        + '<button class="btn small" data-act="plus" data-id="' + esc(s.id) + '">+</button></div>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(s.id) + '">✕</button>'
        + '</div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Добавить сериал</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Название" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'plus' || act === 'minus') {
      (db.shows || []).forEach(function (s) {
        if (s.id !== id) return;
        if (act === 'plus') {
          s.e++;
          if (s.e > 24) { s.e = 1; s.s++; app.toast('Новый сезон! 🎉'); }
        } else if (s.e > 1) s.e--;
      });
      save(); return render();
    }
    if (act === 'del') { db.shows = (db.shows || []).filter(function (s) { return s.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите название'); return; }
      db.shows = db.shows || [];
      db.shows.push({ id: uid(), t: t, s: 1, e: 1 });
      db.showAdd = false; save(); return render();
    }
  }
""", db="{shows:[],showAdd:false}")))

    P.append(dict(
        id="homework-list", name="Домашка", icon="📚", cat="Учёба",
        desc="Учебные задания со сроками и отметкой выполнения",
        js=list_check("📚", "Предмет", "Задание / к какому дню", done_label="Сделано",
                      add_btn="+ Задание", item_hint="Заданий пока нет")))

    P.append(dict(
        id="grades-log", name="Оценки", icon="🎓", cat="Учёба",
        desc="Оценки по предметам и автоматический средний балл",
        js=custom("""
  var SCALE = [2, 3, 4, 5];
  function render() {
    if (!root) return;
    var html = '<div class="card">'
      + '<input type="text" id="f-s" placeholder="Предмет" style="width:100%;margin-bottom:8px">'
      + '<div class="row" style="justify-content:center;gap:8px">'
      + SCALE.map(function (v) { return '<button class="chip" data-act="add" data-v="' + v + '" style="min-width:48px;font-weight:700">' + v + '</button>'; }).join('')
      + '</div></div>';
    var subjects = {};
    (db.grades || []).forEach(function (g) {
      subjects[g.s] = subjects[g.s] || []; subjects[g.s].push(g.v);
    });
    var names = Object.keys(subjects);
    if (names.length) {
      var all = [], htmlRows = '';
      names.forEach(function (s) {
        var arr = subjects[s];
        arr.forEach(function (v) { all.push(v); });
        var avg = arr.reduce(function (a, b) { return a + b; }, 0) / arr.length;
        htmlRows += '<div class="row" style="justify-content:space-between;margin-bottom:6px">'
          + '<span style="font-size:14px">' + esc(s) + '</span>'
          + '<span><b class="money" style="color:' + (avg >= 4.5 ? 'var(--accent)' : 'var(--text)') + '">' + (Math.round(avg * 100) / 100) + '</b> '
          + '<span class="muted" style="font-size:11px">(' + arr.join(', ') + ')</span></span></div>';
      });
      var total = all.reduce(function (a, b) { return a + b; }, 0) / all.length;
      html += '<div class="card"><div class="muted" style="font-size:12px;margin-bottom:8px;font-weight:600">Предметы</div>' + htmlRows
        + '<div class="row" style="justify-content:space-between;margin-top:10px;padding-top:8px;border-top:1px solid var(--border)"><b>Общий средний</b><b class="money" style="color:var(--accent)">' + (Math.round(total * 100) / 100) + '</b></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'add') return;
    var s = val('f-s').trim();
    var v = parseInt(el.getAttribute('data-v'), 10);
    if (!s) { app.toast('Введите предмет'); return; }
    db.grades = db.grades || [];
    db.grades.unshift({ s: s, v: v });
    save(); render();
  }
""", db="{grades:[]}")))

    return P