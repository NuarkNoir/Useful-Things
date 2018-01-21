package xyz.nuark.trashbox;

import java.util.HashMap;
import java.util.Map;

public class Globals {
    public static class Statics {
        static String mainUrl = "https://trashbox.ru/",
                progsUrl = "apps/android/",
                gamesUrl = "games/android/",
                ajaxUrl = "ajax.php",
                userURL = "users/";

        public static String getMainUrl() {
            return mainUrl;
        }

        public static String getProgsUrl() {
            return progsUrl;
        }

        public static String getGamesUrl() {
            return gamesUrl;
        }

        public static String getUserURL() {
            return userURL;
        }
    }

    public static class Tagger {
        public static final Map<String, String> Tag = new HashMap<String, String>(){{
            put("Диктофоны", "/apps/android/dictaphone/");
            put("Заметки", "/apps/android/notes/");
            put("Календари и списки дел", "/apps/android/todo-calendar/");
            put("Калькуляторы и конвертеры", "/apps/android/calculator-converter/");
            put("Облако и резервное копирование", "/apps/android/cloud-backup/");
            put("Офис", "/apps/android/office/");
            put("Финансы", "/apps/android/finance/");
            put("Часы и будильники", "/apps/android/clock-alarm/");
            put("Бизнес", "/apps/android/business/");
            put("Для детей", "/apps/android/apps-for-kids/");
            put("Еда и напитки", "/apps/android/food-drink/");
            put("Жилье и дом", "/apps/android/house-home/");
            put("Здоровье и фитнес", "/apps/android/health-fitness/");
            put("Знакомства", "/apps/android/dating/");
            put("Изучение языков", "/apps/android/language-learning/");
            put("Искусство и дизайн", "/apps/android/art-design/");
            put("Карты и навигация", "/apps/android/maps-navigation/");
            put("Книги и справочники", "/apps/android/books-directory/");
            put("Красота", "/apps/android/beauty/");
            put("Медицина", "/apps/android/medical/");
            put("Новости", "/apps/android/news-apps/");
            put("Образование", "/apps/android/education/");
            put("Переводчики", "/apps/android/translator/");
            put("Погода", "/apps/android/weather/");
            put("Покупки", "/apps/android/shopping/");
            put("Путешествия", "/apps/android/travel/");
            put("Развлечения", "/apps/android/entertainment/");
            put("Спорт", "/apps/android/sport/");
            put("Хобби", "/apps/android/hobbie/");
            put("Читалки", "/apps/android/reader/");
            put("E-mail", "/apps/android/email/");
            put("SMS", "/apps/android/sms/");
            put("VoIP", "/apps/android/voip/");
            put("Звонилки", "/apps/android/dialer/");
            put("Контакты", "/apps/android/contacts-apps/");
            put("Мессенджеры", "/apps/android/messenger/");
            put("Сотовая связь", "/apps/android/cellular/");
            put("Социальные сети", "/apps/android/social-network/");
            put("Аудио и Видеоредакторы", "/apps/android/audio-video-editor/");
            put("Видеоплееры", "/apps/android/video-player/");
            put("Галерея", "/apps/android/gallery/");
            put("Камера", "/apps/android/camera/");
            put("Музыка и плееры", "/apps/android/music-audio/");
            put("ТВ и Радио", "/apps/android/tv-radio/");
            put("Фоторедакторы", "/apps/android/photo-editor/");
            put("Виджеты", "/apps/android/widget/");
            put("Лаунчеры", "/apps/android/launcher/");
            put("Обои", "/apps/android/wallpaper/");
            put("Темы", "/apps/android/themes/");
            put("Экраны блокировки", "/apps/android/lock-screen/");
            put("Bluetooth", "/apps/android/bluetooth/");
            put("OEM приложения", "/apps/android/oem-apps/");
            put("QR-ридеры", "/apps/android/qr-reader/");
            put("Root", "/apps/android/root/");
            put("VPN", "/apps/android/vpn/");
            put("Wi-Fi", "/apps/android/wi-fi/");
            put("Автомобили и транспорт", "/apps/android/auto-vehicles/");
            put("Антивирусы", "/apps/android/antivirus/");
            put("Архиваторы", "/apps/android/archiver/");
            put("Блокировка приложений и файлов", "/apps/android/app-file-lock/");
            put("Браузеры", "/apps/android/browser/");
            put("Клавиатуры", "/apps/android/keyboard/");
            put("Менеджеры загрузок", "/apps/android/download-manager/");
            put("Модификаторы игр", "/apps/android/game-modifier/");
            put("Очистка памяти и ускорение", "/apps/android/memory-cleaner-optimizer/");
            put("Плагины и библиотеки", "/apps/android/plugins-libraries/");
            put("Утилиты", "/apps/android/utilities/");
            put("Файловые менеджеры", "/apps/android/file-manager/");
            put("Фонарики", "/apps/android/flashlight/");
            put("Экономия батареи", "/apps/android/battery-saver/");
            put("Эмуляторы PS и приставок", "/apps/android/console-emulator/");
            put("Online", "/games/android/online/");
            put("RPG", "/games/android/rpg/");
            put("Азартные", "/games/android/gambling/");
            put("Аркады", "/games/android/arcades/");
            put("Викторины", "/games/android/trivia/");
            put("Выживание", "/games/android/survival/");
            put("Головоломки", "/games/android/logic/");
            put("Гонки", "/games/android/racing/");
            put("Для детей", "/games/android/kids-games/");
            put("Казуальные", "/games/android/casual/");
            put("Карточные", "/games/android/card-games/");
            put("Квесты", "/games/android/quest/");
            put("Музыкальные", "/games/android/music-games/");
            put("Настольные", "/games/android/board-games/");
            put("Обучающие", "/games/android/educational-games/");
            put("Приключения", "/games/android/adventure/");
            put("Семейные", "/games/android/family/");
            put("Симуляторы", "/games/android/simulator/");
            put("Словесные", "/games/android/word-games/");
            put("Спорт", "/games/android/sport-games/");
            put("Стратегии", "/games/android/strategy/");
            put("Стрелялки", "/games/android/shooter/");
            put("Ужастики", "/games/android/horror/");
            put("Файтинги", "/games/android/fighting/");
            put("Экшены", "/games/android/action/");
        }};

        public static String getTag(String tag){
            return Tag.get(tag);
        }
    }
}
