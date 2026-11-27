# -*- coding: utf-8 -*-
"""Полезные плагины, группа 1: настроение, заметки и бытовые идеи."""
from templates import wrap, list_check, user_random


def custom(body, db="{ }"):
    return wrap(body, db)


def plugins():
    P = []

    P.append(dict(
        id="fairy", name="Фея", icon="🧚", cat="Настроение",
        desc="Тёплый комплимент, мудрый совет, интересный факт или доброе дело по кнопке",
        js=custom("""
  var KINDS = [
    ['Комплимент', '💐', [
      'Вы замечательная — и это не обсуждается!',
      'Ваш смех — лучшая музыка в доме',
      'Вы справляетесь с тем, что другим не по силам',
      'С вами уютно даже в понедельник',
      'Ваше терпение — это суперсила',
      'Вы делаете обычные дни праздником']],
    ['Доброе дело', '🌼', [
      'Скажите близкому человеку комплимент прямо сейчас',
      'Выпейте чаю медленно, отложив телефон',
      'Отправьте тёплое сообщение старому другу',
      'Уберите одну вещь на своё место — уже порядок!',
      'Улыбнитесь себе в зеркало']],
    ['Интересный факт', '🧠', [
      'Мёд не портится — даже спустя 3000 лет.',
      'У осьминогов три сердца, а кровь голубая.',
      'Кошки спят около 70% своей жизни.',
      'Банан — это ягода, а земляника — нет.']],
    ['Совет дня', '🧭', [
      'Сделайте самое неприятное дело первым — день сразу станет легче',
      'Планируйте три главных дела, а не тридцать',
      'Выпейте стакан воды прямо сейчас',
      'Проветрите комнату — и мысли сразу прояснятся',
      'Отложите телефон на 10 минут и просто отдохните']]
  ];
  function render() {
    if (!root) return;
    var html = '<div class="card hero">'
      + '<div class="hero-icon">🧚</div>'
      + '<div class="muted" style="font-size:11px;margin-bottom:8px;text-transform:uppercase;letter-spacing:1px">' + (db.kind ? esc(db.kind) : 'волшебство по кнопке') + '</div>'
      + '<div style="font-size:17px;font-weight:600;min-height:60px;display:flex;align-items:center;justify-content:center;line-height:1.45">'
      + (db.last ? esc(db.last) : '<span class="muted" style="font-weight:400">Нажмите кнопку — Фея подарит идею или тёплое слово</span>') + '</div>'
      + '<button class="btn" data-act="go" style="min-width:180px;margin-top:14px">Подарить магию ✨</button>'
      + '</div>'
      + '<div class="muted" style="text-align:center;font-size:12px">чудес подарено: ' + (db.count || 0) + '</div>';
    root.innerHTML = html;
  }
  function onAction(el) {
    if (el.getAttribute('data-act') !== 'go') return;
    var k = KINDS[Math.floor(Math.random() * KINDS.length)];
    db.kind = k[0];
    db.last = k[2][Math.floor(Math.random() * k[2].length)];
    db.count = (db.count || 0) + 1;
    save(); render();
  }
""", db="{kind:'',last:'',count:0}")))

    P.append(dict(
        id="mood-diary", name="Дневник настроения", icon="🌈", cat="Настроение",
        desc="Отмечайте настроение дня эмодзи и короткой заметкой",
        js=custom("""
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
""", db="{days:{}}")))

    P.append(dict(
        id="thanks-jar", name="Банка благодарностей", icon="🫙", cat="Настроение",
        desc="Копите тёплые моменты: за что вы благодарны сегодня",
        js=list_check("🫙", "За что вы благодарны?", "Кому / детали", done_label="", has_done=False,
                      add_btn="+ Добавить момент",
                      item_hint="Добавьте хотя бы один хороший момент — день станет теплее")))

    P.append(dict(
        id="ideas-box", name="Шкатулка идей", icon="💡", cat="Настроение",
        desc="Куда складывать внезапные идеи, чтобы они не потерялись",
        js=list_check("💡", "Идея", "Заметка", done_label="", has_done=False,
                      add_btn="+ Новая идея", item_hint="Идеи приходят внезапно — складывайте сюда")))

    P.append(dict(
        id="quick-sticker", name="Стикер", icon="📝", cat="Настроение",
        desc="Быстрый черновик под рукой — сохраняется сам при вводе",
        js=custom("""
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
""", db="{text:''}")))

    P.append(dict(
        id="dinner-roulette", name="Что на ужин", icon="🍽️", cat="Дом",
        desc="Не знаете, что приготовить? Выберите случайную идею из своего списка",
        js=user_random("🍽️", "Выбрать блюдо", "Добавьте своё блюдо",
            preset=["Плов", "Макароны с сыром", "Борщ", "Пельмени", "Гречка с котлетой",
                    "Рис с овощами", "Запечённая картошка", "Куриный суп", "Домашняя пицца",
                    "Блины", "Салат и рыба", "Творожная запеканка"],
            hint="Добавьте свои любимые блюда")))

    return P