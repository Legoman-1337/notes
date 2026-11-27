/* Плагин «Фея» (fairy) — «Удобные заметки».
 * activate(app): app.load() / app.save(json) / app.toast(text) / app.close()
 */
(function () {
  var app = null, root = null;
  var db = {kind:'',last:'',count:0};

  function esc(v){return String(v==null?'':v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;').replace(/'/g,'&#39;');}
  function today(o){var d=new Date();d.setDate(d.getDate()-(o||0));return d.getFullYear()+'-'+('0'+(d.getMonth()+1)).slice(-2)+'-'+('0'+d.getDate()).slice(-2);}
  function dlabel(o){var n=['Вс','Пн','Вт','Ср','Чт','Пт','Сб'];var d=new Date();d.setDate(d.getDate()-(o||0));return n[d.getDay()]+' '+d.getDate();}
  function uid(){return 'i'+Date.now().toString(36)+Math.random().toString(36).slice(2,6);}
  function money(n){n=Math.round(n);return String(n).replace(/\B(?=(\d{3})+(?!\d))/g,' ');}
  function save(){try{app.save(JSON.stringify(db));}catch(e){if(app&&app.toast)app.toast('Не удалось сохранить');}}
  function val(id){var el=document.getElementById(id);return el?el.value:'';}


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
