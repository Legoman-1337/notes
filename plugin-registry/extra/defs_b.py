# -*- coding: utf-8 -*-
"""Полезные плагины, группа 2: дом, семья, быт и документы."""
from templates import wrap, list_check


def custom(body, db="{ }"):
    return wrap(body, db)


def plugins():
    P = []

    P.append(dict(
        id="home-tasks", name="Домашние дела", icon="🏠", cat="Дом",
        desc="Удобный список дел по дому с прогрессом выполнения",
        js=list_check("🏠", "Что сделать?", "Когда / детали", done_label="Сделано",
                      add_btn="+ Новое дело", item_hint="Дел пока нет — можно отдыхать")))

    P.append(dict(
        id="menu-week", name="Меню недели", icon="🍲", cat="Дом",
        desc="План питания на всю неделю — вопрос «что готовить?» закрыт",
        js=custom("""
  var DAYS = ['Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота', 'Воскресенье'];
  function render() {
    if (!root) return;
    var html = '<div class="card">';
    DAYS.forEach(function (d, idx) {
      var short = ['Пн','Вт','Ср','Чт','Пт','Сб','Вс'][idx];
      html += '<div class="row" style="margin-bottom:8px">'
        + '<span class="chip" style="min-width:46px;text-align:center">' + short + '</span>'
        + '<input type="text" class="grow menu-day" data-day="' + short + '" placeholder="' + d + '" value="' + esc((db.menu || {})[short] || '') + '">'
        + '</div>';
    });
    html += '<button class="btn" data-act="save" style="width:100%;margin-top:6px">Сохранить меню 🍲</button></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'save') return;
    db.menu = db.menu || {};
    var inputs = root.querySelectorAll('.menu-day');
    for (var i = 0; i < inputs.length; i++) {
      var d = inputs[i].getAttribute('data-day');
      var v = inputs[i].value.trim();
      if (v) db.menu[d] = v; else delete db.menu[d];
    }
    save(); app.toast('Меню сохранено 🍲');
  }
""", db="{menu:{}}")))

    P.append(dict(
        id="plants-water", name="Полив цветов", icon="🌷", cat="Дом",
        desc="Учёт полива домашних растений — сразу видно, кого пора полить",
        js=custom("""
  function render() {
    if (!root) return;
    var html = '';
    if (!db.plants || !db.plants.length) {
      html += '<div class="card hero"><div class="hero-icon">🌷</div><p class="muted" style="margin:4px 0">Добавьте ваши растения, чтобы не забывать о поливе</p></div>';
    }
    (db.plants || []).forEach(function (p) {
      var days = null;
      if (p.d) {
        var d = new Date(p.d + 'T00:00:00');
        days = Math.floor((new Date(today(0) + 'T00:00:00') - d) / 86400000);
      }
      html += '<div class="card" style="padding:10px 13px"><div class="row" style="gap:10px">'
        + '<span style="font-size:24px">🌿</span>'
        + '<div class="grow" style="min-width:0"><b>' + esc(p.t) + '</b>'
        + '<div class="muted" style="font-size:12px;margin-top:2px">' + (days == null ? 'ещё не поливали' : (days === 0 ? 'полито сегодня ✓' : 'полито ' + days + ' дн. назад')) + '</div></div>'
        + '<button class="btn small' + (days === 0 ? ' secondary' : '') + '" data-act="water" data-id="' + esc(p.id) + '">💧</button>'
        + '<button class="btn danger small" data-act="del" data-id="' + esc(p.id) + '">✕</button>'
        + '</div></div>';
    });
    html += '<div class="card" style="text-align:center;padding:10px"><button class="btn" data-act="show-add" style="width:100%">+ Добавить растение</button></div>';
    if (db.showAdd) {
      html += '<div class="card" style="border-color:var(--accent)">'
        + '<input type="text" id="f-t" placeholder="Название растения" style="width:100%;margin-bottom:8px">'
        + '<div class="row"><button class="btn grow" data-act="add">Добавить</button>'
        + '<button class="btn secondary" data-act="hide-add">Отмена</button></div></div>';
    }
    root.innerHTML = html;
  }
  function onAction(el) {
    var act = el.getAttribute('data-act');
    var id = el.getAttribute('data-id');
    if (act === 'water') {
      (db.plants || []).forEach(function (p) { if (p.id === id) p.d = today(0); });
      save(); app.toast('Полито 💧'); return render();
    }
    if (act === 'del') { db.plants = (db.plants || []).filter(function (p) { return p.id !== id; }); save(); return render(); }
    if (act === 'show-add') { db.showAdd = true; return render(); }
    if (act === 'hide-add') { db.showAdd = false; return render(); }
    if (act === 'add') {
      var t = val('f-t').trim();
      if (!t) { app.toast('Введите название'); return; }
      db.plants = db.plants || [];
      db.plants.push({ id: uid(), t: t, d: '' });
      db.showAdd = false; save(); return render();
    }
  }
""", db="{plants:[],showAdd:false}")))

    P.append(dict(
        id="pet-tracker", name="Питомец", icon="🐱", cat="Дом",
        desc="Ежедневный чек-лист ухода за питомцем: корм, прогулка/игра, лоток",
        js=custom("""
  var TASKS = [['feed', '🍲', 'Покормила'], ['play', '🎾', 'Поиграла'], ['litter', '🧹', 'Уход']];
  function render() {
    if (!root) return;
    var t = today(0);
    var cur = (db.days && db.days[t]) || {};
    var doneCount = TASKS.filter(function (x) { return cur[x[0]]; }).length;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🐱</div>'
      + '<input type="text" id="f-pet" placeholder="Имя питомца" value="' + esc(db.pet || '') + '" style="width:72%;margin-bottom:12px;text-align:center">'
      + '<div class="row" style="justify-content:center;gap:8px;flex-wrap:wrap">';
    TASKS.forEach(function (task) {
      var on = !!cur[task[0]];
      html += '<button class="chip' + (on ? ' active' : '') + '" data-act="toggle" data-k="' + task[0] + '" style="padding:10px 14px">' + task[1] + ' ' + task[2] + '</button>';
    });
    html += '</div><div class="muted" style="font-size:12px;margin-top:12px">Выполнено сегодня: <b>' + doneCount + ' из 3</b></div></div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') === 'toggle') {
      var k = el.getAttribute('data-k');
      db.days = db.days || {};
      var cur = db.days[today(0)] || {};
      if (cur[k]) delete cur[k]; else cur[k] = 1;
      db.days[today(0)] = cur;
      save(); return render();
    }
  }
  function _wire() {
    var el = document.getElementById('f-pet');
    if (el) el.addEventListener('change', function () { db.pet = el.value.trim(); save(); });
  }
""", db="{pet:'',days:{}}")))

    P.append(dict(
        id="packing-list", name="Сборы в поездку", icon="🧳", cat="Дом",
        desc="Готовый чек-лист для чемодана, чтобы ничего не забыть в дорогу",
        js=list_check("🧳", "Что положить?", "Детали", done_label="Собрано",
                      add_btn="+ Добавить вещь",
                      preset=["Документы", "Зарядка и пауэрбанк", "Зубная щётка", "Косметичка",
                              "Одежда и бельё", "Обувь", "Аптечка", "Вода и перекус"],
                      item_hint="Список вещей для поездки")))

    P.append(dict(
        id="first-aid-kit", name="Аптечка", icon="🩹", cat="Дом",
        desc="Что есть в домашней аптечке, сроки годности и что пора докупить",
        js=list_check("🩹", "Препарат / средство", "Срок годности / примечание", done_label="Закончилось", has_done=True,
                      add_btn="+ В аптечку", item_hint="Добавьте лекарства и сроки годности")))

    P.append(dict(
        id="documents-dates", name="Документы", icon="📁", cat="Дом",
        desc="Сроки действия паспортов, страховок, карт и справок под рукой",
        js=list_check("📁", "Документ", "Действует до (например 15.03.2028)", done_label="", has_done=False,
                      add_btn="+ Документ", item_hint="Добавьте документы и сроки их действия")))

    P.append(dict(
        id="warranties", name="Гарантии", icon="🧾", cat="Дом",
        desc="На какую технику ещё действует гарантия и до какой даты",
        js=list_check("🧾", "Техника / покупка", "Гарантия до", done_label="", has_done=False,
                      add_btn="+ Покупка", item_hint="Добавьте покупки с гарантией")))

    P.append(dict(
        id="sizes-memo", name="Размеры", icon="📏", cat="Дом",
        desc="Шпаргалка размеров одежды и обуви для себя и близких",
        js=list_check("📏", "Кто / категория", "Размеры", done_label="", has_done=False,
                      add_btn="+ Запись", item_hint="Добавьте размеры одежды и обуви")))

    P.append(dict(
        id="wifi-memo", name="Парольчики", icon="📶", cat="Дом",
        desc="Домашний Wi-Fi и важные локальные заметки-доступы под рукой",
        js=list_check("📶", "Сеть или сервис", "Логин / пароль", done_label="", has_done=False,
                      add_btn="+ Запись", item_hint="Данные хранятся только у вас в приложении")))

    P.append(dict(
        id="car-log", name="Машина", icon="🚗", cat="Дом",
        desc="Заправки, ТО, страховки и пробег — дневник автомобиля",
        js=list_check("🚗", "Событие (ТО, заправка…)", "Дата / сумма / пробег", done_label="", has_done=False,
                      add_btn="+ Запись", item_hint="Заносите сюда обслуживание автомобиля")))

    P.append(dict(
        id="gifts-ideas", name="Идеи подарков", icon="🎁", cat="Дом",
        desc="Кому и что подарить на праздники — записывайте идеи заранее",
        js=list_check("🎁", "Кому", "Идея подарка", done_label="Подарено", has_done=True,
                      add_btn="+ Идея подарка", item_hint="Сохраняйте идеи подарков для близких")))

    P.append(dict(
        id="freezer-log", name="Морозилка", icon="❄️", cat="Дом",
        desc="Учёт заготовок в морозилке с датой заморозки",
        js=list_check("❄️", "Что заморозили", "Дата заморозки", done_label="Использовано", has_done=True,
                      add_btn="+ В морозилку", item_hint="Записывайте заготовки — всегда понятно, что внутри")))

    return P