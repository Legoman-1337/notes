package ru.momnotes.app

/**
 * Очищенный каталог полезных «Дополнительных» плагинов (без мусорных мини-игр)
 * и пакетов для вкладки «Пакеты».
 * Сгенерировано plugin-registry/extra/generate.py.
 */
internal object ExtraCatalog {

    val PLUGIN_IDS: List<String> = listOf(
        "fairy",
        "mood-diary",
        "thanks-jar",
        "ideas-box",
        "quick-sticker",
        "dinner-roulette",
        "home-tasks",
        "menu-week",
        "plants-water",
        "pet-tracker",
        "packing-list",
        "first-aid-kit",
        "documents-dates",
        "warranties",
        "sizes-memo",
        "wifi-memo",
        "car-log",
        "gifts-ideas",
        "freezer-log",
        "spend-diary",
        "income-log",
        "piggy-bank",
        "subscriptions-list",
        "discount-calc",
        "bill-split",
        "salary-plan",
        "currency-memo",
        "water-tracker",
        "pills-check",
        "sleep-diary",
        "weight-diary",
        "breathe-calm",
        "pomodoro",
        "timers-mini",
        "stopwatch",
        "date-diff",
        "age-calc",
        "unit-conv",
        "password-gen",
        "books-list",
        "movies-list",
        "series-tracker",
        "homework-list",
        "grades-log"
    )

    val PACKAGES: List<ExtraPackage> = listOf(
        ExtraPackage("home", "Дом и семья", "🏠", "Дела, меню недели, полив цветов, питомец, аптечка и важные документы", listOf("home-tasks", "menu-week", "dinner-roulette", "plants-water", "pet-tracker", "first-aid-kit", "documents-dates", "gifts-ideas", "freezer-log")),
        ExtraPackage("money", "Финансы и расчёты", "💰", "Траты дня, доходы, копилка мечты, подписки, скидки и деление счёта", listOf("spend-diary", "income-log", "piggy-bank", "subscriptions-list", "discount-calc", "bill-split", "salary-plan", "currency-memo")),
        ExtraPackage("health", "Здоровье и сон", "💧", "Вода, приём лекарств, дневник сна, вес и дыхание 4-7-8", listOf("water-tracker", "pills-check", "sleep-diary", "weight-diary", "breathe-calm")),
        ExtraPackage("study", "Учёба, книги и досуг", "📚", "Помодоро, домашка, оценки, книги, фильмы и счётчик серий", listOf("pomodoro", "homework-list", "grades-log", "books-list", "movies-list", "series-tracker")),
        ExtraPackage("tools", "Инструменты и заметки", "⏱️", "Таймер, секундомер, даты, конвертер величин, пароли, стикер и Фея", listOf("fairy", "timers-mini", "stopwatch", "date-diff", "age-calc", "unit-conv", "password-gen", "wifi-memo", "packing-list", "quick-sticker", "mood-diary", "thanks-jar", "ideas-box"))
    )
}
