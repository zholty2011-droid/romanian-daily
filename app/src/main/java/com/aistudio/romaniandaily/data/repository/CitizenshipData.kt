package com.aistudio.romaniandaily.data.repository

import com.aistudio.romaniandaily.data.model.CitizenshipQA
import com.aistudio.romaniandaily.data.model.CitizenshipTopic
import com.aistudio.romaniandaily.data.model.DialogLine
import com.aistudio.romaniandaily.data.model.DialogScenario

object CitizenshipData {
    val oathTextRo = """
        Jur să fiu devotat patriei și poporului român,
        să apăr drepturile și interesele naționale,
        să respect Constituția și legile României.
    """.trimIndent()

    val oathTextRu = """
        Клянусь быть преданным родине и румынскому народу,
        защищать национальные права и интересы,
        соблюдать Конституцию и законы Румынии.
    """.trimIndent()

    val oathLines = listOf(
        Pair("Jur să fiu devotat patriei și poporului român,", "Клянусь быть преданным родине и румынскому народу,"),
        Pair("să apăr drepturile și interesele naționale,", "защищать национальные права и интересы,"),
        Pair("să respect Constituția și legile României.", "соблюдать Конституцию и законы Румынии.")
    )

    val oathPhonetics = listOf(
        "Жур сэ фиу девотат патрией ши попорулуй ромын,",
        "сэ апэр дрептуриле ши интереселе национале,",
        "сэ респект Конституция ши леджиле Ромынией."
    )

    val topics = listOf(
        CitizenshipTopic(
            id = "personal",
            titleRu = "О себе и семье",
            titleRo = "Despre sine și familie",
            icon = "👤",
            questions = listOf(
                CitizenshipQA(
                    id = "p1",
                    qRo = "Cum vă numiți?",
                    qRu = "Как вас зовут?",
                    qTrans = "Кум вэ нумиць?",
                    aRo = "Mă numesc [Numele]. Sunt din [Orașul].",
                    aRu = "Меня зовут [Имя]. Я из [Город].",
                    aTrans = "Мэ нумеск [Имя]. Сунт дин [Город].",
                    tip = "Отвечайте спокойно, чётко произносите фамилию и имя."
                ),
                CitizenshipQA(
                    id = "p2",
                    qRo = "Câți ani aveți?",
                    qRu = "Сколько вам лет?",
                    qTrans = "Кыць ань авець?",
                    aRo = "Am [număr] de ani.",
                    aRu = "Мне [число] лет.",
                    aTrans = "Ам [число] де ань.",
                    tip = "Не забудьте предлог 'de' после чисел от 20 (например: Am treizeci de ani)."
                ),
                CitizenshipQA(
                    id = "p3",
                    qRo = "Care este starea dumneavoastră civilă?",
                    qRu = "Каково ваше семейное положение?",
                    qTrans = "Каре есте старя думнявоастрэ чивилэ?",
                    aRo = "Sunt căsătorit(ă) / Sunt celibatar(ă) / Sunt divorțat(ă).",
                    aRu = "Я женат (замужем) / Холост (не замужем) / В разводе.",
                    aTrans = "Сунт кэсэторит(э) / Сунт челибатар(э).",
                    tip = "Мужчины говорят căsătorit, женщины — căsătorită."
                ),
                CitizenshipQA(
                    id = "p4",
                    qRo = "Aveți copii?",
                    qRu = "У вас есть дети?",
                    qTrans = "Авець копий?",
                    aRo = "Da, am un fiu și o fiică / Nu, nu am copii.",
                    aRu = "Да, у меня есть сын и дочь / Нет, у меня нет детей.",
                    aTrans = "Да, ам ун фиу ши о фийкэ / Ну, ну ам копий."
                ),
                CitizenshipQA(
                    id = "p5",
                    qRo = "Ce profesie aveți / Cu ce vă ocupați?",
                    qRu = "Какая у вас профессия / Чем занимаетесь?",
                    qTrans = "Че професие авець / Ку че вэ окупаць?",
                    aRo = "Sunt inginer / programator / medic / economist.",
                    aRu = "Я инженер / программист / врач / экономист.",
                    aTrans = "Сунт инжинер / програматор / медик."
                ),
                CitizenshipQA(
                    id = "p6",
                    qRo = "De ce doriți cetățenia română?",
                    qRu = "Почему вы хотите румынское гражданство?",
                    qTrans = "Де че дориць четэцения ромынэ?",
                    aRo = "Doresc să restabilesc dreptul istoric al familiei mele și să trăiesc în armonie cu valorile românești.",
                    aRu = "Желаю восстановить историческое право моей семьи и жить в согласии с румынскими ценностями.",
                    aTrans = "Дореск сэ рестабилеск дрептул историк ал фамилией меле..."
                )
            )
        ),
        CitizenshipTopic(
            id = "symbols",
            titleRu = "Символы Румынии",
            titleRo = "Simbolurile naționale",
            icon = "🇷🇴",
            questions = listOf(
                CitizenshipQA(
                    id = "s1",
                    qRo = "Care sunt culorile drapelului României?",
                    qRu = "Каковы цвета флага Румынии?",
                    qTrans = "Каре сунт кулориле драпелулуй Ромынией?",
                    aRo = "Culorile sunt albastru, galben și roșu (vertical, de la lance).",
                    aRu = "Цвета: синий, жёлтый и красный (вертикально от древка).",
                    aTrans = "Кулориле сунт албастру, галбен ши рошу."
                ),
                CitizenshipQA(
                    id = "s2",
                    qRo = "Care este imnul național al României?",
                    qRu = "Какой национальный гимн Румынии?",
                    qTrans = "Каре есте имнул национал ал Ромынией?",
                    aRo = "Imnul național este «Deșteaptă-te, române!» (versuri de Andrei Mureșanu).",
                    aRu = "Национальный гимн — «Deșteaptă-te, române!» (слова Андрея Мурешану).",
                    aTrans = "Имнул национал есте Дештяптэ-те, ромыне!"
                ),
                CitizenshipQA(
                    id = "s3",
                    qRo = "Când este Ziua Națională a României?",
                    qRu = "Когда Национальный день Румынии?",
                    qTrans = "Кынд есте Зиуа Националэ а Ромынией?",
                    aRo = "Ziua Națională este pe 1 Decembrie (Marea Unire din 1918).",
                    aRu = "Национальный день — 1 декабря (Великое объединение 1918 года).",
                    aTrans = "Зиуа Националэ есте пе уну дечембрие."
                ),
                CitizenshipQA(
                    id = "s4",
                    qRo = "Care este capitala României?",
                    qRu = "Какая столица Румынии?",
                    qTrans = "Каре есте капитала Ромынией?",
                    aRo = "Capitala României este municipiul București.",
                    aRu = "Столица Румынии — муниципий Бухарест.",
                    aTrans = "Капитала Ромынией есте муничипиул Букурешть."
                ),
                CitizenshipQA(
                    id = "s5",
                    qRo = "Ce monedă are România?",
                    qRu = "Какая валюта в Румынии?",
                    qTrans = "Че монедэ аре Ромыния?",
                    aRo = "Moneda națională este leul românesc (RON), divizat în bani.",
                    aRu = "Национальная валюта — румынский лей (RON), делится на баны.",
                    aTrans = "Монеда националэ есте леул ромынеск."
                )
            )
        ),
        CitizenshipTopic(
            id = "geography",
            titleRu = "География и природа",
            titleRo = "Geografia României",
            icon = "🏔️",
            questions = listOf(
                CitizenshipQA(
                    id = "g1",
                    qRo = "Ce munți traversează România?",
                    qRu = "Какие горы пересекают Румынию?",
                    qTrans = "Че мунць траверсязэ Ромыния?",
                    aRo = "Munții Carpați (Carpații Orientali, Meridionali și Occidentali).",
                    aRu = "Карпатские горы (Восточные, Южные и Западные Карпаты).",
                    aTrans = "Мунций Карпаць."
                ),
                CitizenshipQA(
                    id = "g2",
                    qRo = "Ce mare mărginește România la est?",
                    qRu = "Какое море омывает Румынию на востоке?",
                    qTrans = "Че маре мэрджинеште Ромыния ла ест?",
                    aRo = "Marea Neagră.",
                    aRu = "Чёрное море.",
                    aTrans = "Маря Нягрэ."
                ),
                CitizenshipQA(
                    id = "g3",
                    qRo = "Care este cel mai important fluviu din România?",
                    qRu = "Какая самая важная река (водная артерия) в Румынии?",
                    qTrans = "Каре есте чел май важник флувиу дин Ромыния?",
                    aRo = "Fluviul Dunărea, care se varsă în Marea Neagră prin Delta Dunării.",
                    aRu = "Река Дунай, которая впадает в Чёрное море через дельту Дуная.",
                    aTrans = "Флувиул Дунэря."
                ),
                CitizenshipQA(
                    id = "g4",
                    qRo = "Cu ce țări se învecinează România?",
                    qRu = "С какими странами граничит Румыния?",
                    qTrans = "Ку че цэрь се ынвечинязэ Ромыния?",
                    aRo = "Cu Ucraina, Republica Moldova, Bulgaria, Serbia și Ungaria.",
                    aRu = "С Украиной, Молдовой, Болгарией, Сербией и Венгрией.",
                    aTrans = "Ку Украина, Република Молдова, Булгария, Сербия ши Унгария."
                )
            )
        ),
        CitizenshipTopic(
            id = "commission",
            titleRu = "Типовые вопросы комиссии",
            titleRo = "Întrebări frecvente la comisie",
            icon = "⚖️",
            questions = listOf(
                CitizenshipQA(
                    id = "c1",
                    qRo = "Înțelegeți limba română?",
                    qRu = "Вы понимаете румынский язык?",
                    qTrans = "Ынцеледжець лимба ромынэ?",
                    aRo = "Da, înțeleg și vorbesc puțin. Învăț în fiecare zi.",
                    aRu = "Да, понимаю и немного говорю. Учу каждый день.",
                    aTrans = "Да, ынцелег ши ворбеск пуцин. Ынвэц ын фиекаре зи.",
                    tip = "Никогда не говорите просто 'Nu'. Всегда показывайте готовность говорить."
                ),
                CitizenshipQA(
                    id = "c2",
                    qRo = "Cum ați ajuns astăzi aici?",
                    qRu = "Как вы добрались сегодня сюда?",
                    qTrans = "Кум аць ажунс астэзь айчь?",
                    aRo = "Am venit cu trenul / cu mașina / cu avionul.",
                    aRu = "Я приехал на поезде / на машине / на самолёте.",
                    aTrans = "Ам венит ку тренул / ку машиина / ку авионул."
                ),
                CitizenshipQA(
                    id = "c3",
                    qRo = "Ce zi este astăzi? Cum este vremea afară?",
                    qRu = "Какой сегодня день? Какая погода на улице?",
                    qTrans = "Че зи есте астэзь? Кум есте время афарэ?",
                    aRo = "Astăzi este [luni/marți/miercuri/joi/vineri]. Vremea este frumoasă / caldă / ploioasă.",
                    aRu = "Сегодня [день недели]. Погода хорошая / теплая / дождливая.",
                    aTrans = "Астэзь есте... Время есте фрумоасэ."
                ),
                CitizenshipQA(
                    id = "c4",
                    qRo = "Vă rog să luați loc și să semnați aici.",
                    qRu = "Пожалуйста, присаживайтесь и распишитесь здесь.",
                    qTrans = "Вэ рог сэ луаць лок ши сэ семнаць айчь.",
                    aRo = "Mulțumesc frumos! Cu plăcere.",
                    aRu = "Большое спасибо! С удовольствием.",
                    aTrans = "Мульцумеск фрумос!"
                )
            )
        )
    )

    val dialogs = listOf(
        DialogScenario(
            id = "cafe",
            titleRu = "В кафе / Ресторане",
            titleRo = "La cafenea și restaurant",
            icon = "☕",
            description = "Заказ кофе, напитков и десерта с оплатой картой или наличными.",
            lines = listOf(
                DialogLine("Chelner", false, "Bună ziua! Cu ce vă pot ajuta?", "Добрый день! Чем могу вам помочь?", "Бунэ зиуа! Ку че вэ пот ажута?"),
                DialogLine("Dumneavoastră", true, "Bună ziua! O cafea cu lapte și un croissant, vă rog.", "Добрый день! Кофе с молоком и круассан, пожалуйста.", "Бунэ зиуа! О кафеа ку лапте ши ун круасан, вэ рог."),
                DialogLine("Chelner", false, "Doriți zahăr la cafea?", "Желаете сахар к кофе?", "Дориць захэр ла кафеа?"),
                DialogLine("Dumneavoastră", true, "Fără zahăr, mulțumesc. Cât costă?", "Без сахара, спасибо. Сколько стоит?", "Фэрэ захэр, мульцумеск. Кыт костэ?"),
                DialogLine("Chelner", false, "Costă cincisprezece lei în total.", "Пятнадцать лей всего.", "Костэ чинчиспрезече лей ын тотал."),
                DialogLine("Dumneavoastră", true, "Pot plăti cu cardul?", "Могу я оплатить картой?", "Пот плэти ку кардул?"),
                DialogLine("Chelner", false, "Da, desigur! Apropiați cardul aici. Poftă bună!", "Да, конечно! Приложите карту сюда. Приятного аппетита!", "Да, десигур! Пофтэ бунэ!")
            )
        ),
        DialogScenario(
            id = "airport",
            titleRu = "В аэропорту и на границе",
            titleRo = "La aeroport și vamă",
            icon = "✈️",
            description = "Паспортный контроль, цель визита и багаж.",
            lines = listOf(
                DialogLine("Polițist de frontieră", false, "Bună ziua. Pașaportul dumneavoastră, vă rog.", "Добрый день. Ваш паспорт, пожалуйста.", "Бунэ зиуа. Пашапортул думнявоастрэ, вэ рог."),
                DialogLine("Dumneavoastră", true, "Bună ziua. Poftim pașaportul și actele.", "Добрый день. Вот паспорт и документы.", "Бунэ зиуа. Пофтим пашапортул ши актеле."),
                DialogLine("Polițist de frontieră", false, "Care este scopul călătoriei dumneavoastră?", "Какова цель вашей поездки?", "Каре есте скопул кэлэторией?"),
                DialogLine("Dumneavoastră", true, "Turism și vizitarea familiei. Voi sta două săptămâni.", "Туризм и посещение семьи. Я пробуду две недели.", "Туризм ши визитаря фамилией."),
                DialogLine("Polițist de frontieră", false, "Aveți ceva de declarat la vamă?", "Есть что-то для таможенной декларации?", "Авець чева де декларат ла вамэ?"),
                DialogLine("Dumneavoastră", true, "Nu, doar lucruri personale.", "Нет, только личные вещи.", "Ну, доар лукрурь персонале."),
                DialogLine("Polițist de frontieră", false, "Totul este în regulă. Ședere plăcută în România!", "Всё в порядке. Приятного пребывания в Румынии!", "Тотул есте ын регулэ. Шедере плэкутэ!")
            )
        ),
        DialogScenario(
            id = "shop",
            titleRu = "В магазине и аптеке",
            titleRo = "La magazin și farmacie",
            icon = "🛍️",
            description = "Поиск нужного товара, размер, цена и чек.",
            lines = listOf(
                DialogLine("Vânzător", false, "Bună ziua, căutați ceva anume?", "Добрый день, ищете что-то конкретное?", "Бунэ зиуа, кэутаць чева ануме?"),
                DialogLine("Dumneavoastră", true, "Bună ziua! Unde pot găsi apă plată și fructe?", "Добрый день! Где я могу найти негазированную воду и фрукты?", "Унде пот гэси апэ платэ ши фрукте?"),
                DialogLine("Vânzător", false, "Apa este pe rândul trei, iar fructele la intrare.", "Вода в третьем ряду, а фрукты у входа.", "Апа есте пе рындул трей..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc! Aveți și o pungă, vă rog?", "Спасибо! Есть ли у вас пакет, пожалуйста?", "Мульцумеск! Авець ши о пунгэ?"),
                DialogLine("Vânzător", false, "Desigur, una mare sau mică?", "Конечно, большой или маленький?", "Десигур, уна маре сау микэ?"),
                DialogLine("Dumneavoastră", true, "O pungă mare. Iată banii.", "Большой пакет. Вот деньги.", "О пунгэ маре. Ятэ баний."),
                DialogLine("Vânzător", false, "Iată restul și bonul fiscal. O zi bună!", "Вот сдача и фискальный чек. Хорошего дня!", "Ятэ рестул ши бонул фискал.")
            )
        ),
        DialogScenario(
            id = "doctor",
            titleRu = "У врача / В аптеке",
            titleRo = "La medic și sănătate",
            icon = "🩺",
            description = "Объяснить симптомы, самочувствие и получить рекомендации.",
            lines = listOf(
                DialogLine("Medic", false, "Bună ziua! Ce vă supără? Ce simptome aveți?", "Добрый день! Что вас беспокоит? Какие симптомы?", "Бунэ зиуа! Че вэ супэрэ?"),
                DialogLine("Dumneavoastră", true, "Mă doare capul și am febră de ieri.", "У меня болит голова и температура со вчерашнего дня.", "Мэ доаре капул ши ам фебрэ де йерь."),
                DialogLine("Medic", false, "Aveți tuse sau dureri în gât?", "Есть кашель или боли в горле?", "Авець тусе сау дурерь ын гыт?"),
                DialogLine("Dumneavoastră", true, "Da, mă doare și gâtul când înghit.", "Да, болит и горло, когда глотаю.", "Да, мэ доаре ши гытул кынд ынгхит."),
                DialogLine("Medic", false, "Luați acest medicament de două ori pe zi după masă.", "Принимайте это лекарство два раза в день после еды.", "Луаць ачест медикамент..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc mult, domnule doctor!", "Большое спасибо, доктор!", "Мульцумеск мулт!")
            )
        ),
        DialogScenario(
            id = "hotel",
            titleRu = "Отель и аренда жилья",
            titleRo = "La hotel și cazare",
            icon = "🏨",
            description = "Заселение, ключ от номера, Wi-Fi и завтрак.",
            lines = listOf(
                DialogLine("Recepționer", false, "Bună ziua, bine ați venit! Aveți o rezervare?", "Добрый день, добро пожаловать! У вас есть бронь?", "Бунэ зиуа, бине аць венит!"),
                DialogLine("Dumneavoastră", true, "Bună ziua! Da, am rezervat o cameră pentru două nopți pe numele Popescu.", "Добрый день! Да, я забронировал номер на две ночи на фамилию Попеску.", "Ам резерват о камерэ пентру доуэ нопць..."),
                DialogLine("Recepționer", false, "Perfect. Iată cheia dumneavoastră, camera 305 la etajul trei.", "Отлично. Вот ваш ключ, номер 305 на третьем этаже.", "Перфект. Ятэ кея думнявоастрэ..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc. La ce oră se servește micul dejun?", "Спасибо. В какое время подаётся завтрак?", "Ла че орэ се сервеште микул дежун?"),
                DialogLine("Recepționer", false, "Micul dejun este între orele șapte și zece dimineața.", "Завтрак с семи до десяти утра.", "Микул дежун есте ынтре ореле шапте ши зече..."),
                DialogLine("Dumneavoastră", true, "Care este parola de la Wi-Fi, vă rog?", "Какой пароль от Wi-Fi, пожалуйста?", "Каре есте парола де ла Вай-Фай?"),
                DialogLine("Recepționer", false, "Parola este scrisă pe cartonașul cheii. Sejur plăcut!", "Пароль написан на вкладыше ключа. Приятного пребывания!", "Парола есте скрисэ... Сежур плэкут!")
            )
        ),
        DialogScenario(
            id = "taxi",
            titleRu = "В такси и ориентирование в городе",
            titleRo = "În taxi și pe stradă",
            icon = "🚕",
            description = "Адрес поездки, просьба остановиться и уточнение маршрута.",
            lines = listOf(
                DialogLine("Șofer de taxi", false, "Bună ziua! Unde mergem?", "Добрый день! Куда едем?", "Бунэ зиуа! Унде мерджем?"),
                DialogLine("Dumneavoastră", true, "Bună ziua! La Gara de Nord, vă rog. Mă grăbesc puțin.", "Добрый день! До Северного вокзала, пожалуйста. Я немного спешу.", "Ла Гара де Норд, вэ рог. Мэ грэбеск пуцин."),
                DialogLine("Șofer de taxi", false, "Nicio problemă, ajungem în cincisprezece minute.", "Без проблем, доедем за 15 минут.", "Ничо проблемэ, ажунджем..."),
                DialogLine("Dumneavoastră", true, "Puteți opri aici la colț, lângă farmacie?", "Можете остановиться здесь на углу, возле аптеки?", "Путець опри айчь ла колц?"),
                DialogLine("Șofer de taxi", false, "Sigur, am oprit. Cursa costă douăzeci și cinci de lei.", "Конечно, остановился. Поездка стоит 25 лей.", "Сигур, ам оприт. Курса костэ..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc! Păstrați restul. O zi bună!", "Спасибо! Сдачи не надо (оставьте сдачу). Хорошего дня!", "Мульцумеск! Пэстраць рестул.")
            )
        ),
        DialogScenario(
            id = "train",
            titleRu = "На вокзале (Gara CFR)",
            titleRo = "La gara de tren",
            icon = "🚆",
            description = "Покупка билета на поезд, выбор вагона и время отправления.",
            lines = listOf(
                DialogLine("Casier", false, "Bună ziua, cu ce vă pot ajuta?", "Добрый день, чем могу помочь?", "Бунэ зиуа, ку че вэ пот ажута?"),
                DialogLine("Dumneavoastră", true, "Aș dori un bilet până la Brașov pentru astăzi, vă rog.", "Я хотел бы билет до Брашова на сегодня, пожалуйста.", "Аш дори ун билет пынэ ла Брашов..."),
                DialogLine("Casier", false, "La clasa întâi sau a doua? Trenul InterRegio pleacă la paisprezece treizeci.", "В первый или второй класс? Поезд InterRegio отправляется в 14:30.", "Ла класа ынтый сау а доуа?"),
                DialogLine("Dumneavoastră", true, "La clasa a doua, la fereastră, dacă este posibil.", "Во второй класс, у окна, если возможно.", "Ла класа а доуа, ла фереастрэ..."),
                DialogLine("Casier", false, "Da, locul patruzeci și doi. De pe ce peron pleacă este afișat pe tabelă.", "Да, место 42. С какого перрона отправляется — показано на табло.", "Да, локул патрузечь ши дой..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc frumos! Călătorie plăcută!", "Большое спасибо!", "Мульцумеск фрумос!")
            )
        ),
        DialogScenario(
            id = "bank",
            titleRu = "В банке и обмен валюты",
            titleRo = "La bancă și schimb valutar",
            icon = "💳",
            description = "Открытие счёта, обмен валюты и снятие в банкомате.",
            lines = listOf(
                DialogLine("Funcționar bancar", false, "Bună ziua! Cu ce vă pot fi de folos?", "Добрый день! Чем могу быть полезен?", "Бунэ зиуа! Ку че вэ пот фи де фолос?"),
                DialogLine("Dumneavoastră", true, "Bună ziua. Doresc să deschid un cont bancar în lei.", "Добрый день. Я хочу открыть банковский счёт в леях.", "Дореск сэ дескид ун конт банкар ын лей."),
                DialogLine("Funcționar bancar", false, "Aveți pașaportul și permisul de ședere sau CNP-ul?", "У вас есть паспорт и вид на жительство или CNP?", "Авець пашапортул ши пермисул...?"),
                DialogLine("Dumneavoastră", true, "Da, am toate actele originale aici.", "Да, все оригиналы документов у меня здесь.", "Да, ам тоате актеле оригинале айчь."),
                DialogLine("Funcționar bancar", false, "Vă rog să completați această cerere și să semnați la final.", "Пожалуйста, заполните это заявление и распишитесь в конце.", "Вэ рог сэ комплетаць ачастэ черере..."),
                DialogLine("Dumneavoastră", true, "Mulțumesc! Când va fi gata cardul bancar?", "Спасибо! Когда будет готова банковская карта?", "Кынд ва фи гата кардул банкар?"),
                DialogLine("Funcționar bancar", false, "Cardul va fi gata în cinci zile lucrătoare.", "Карта будет готова через пять рабочих дней.", "Кардул ва фи гата ын чинчь зиле...")
            )
        )
    )
}
