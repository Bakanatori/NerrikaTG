# 🤖 NerrikaTG - Minecraft Telegram Bridge

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-green.svg)](https://minecraft.net)
[![Forge](https://img.shields.io/badge/Forge-58.0.2-orange.svg)](https://files.minecraftforge.net)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Мощный server-side мод для интеграции Minecraft сервера с Telegram каналом.

## ✨ Основные функции

### 🔗 **Telegram ↔ Minecraft чат**
- **Двусторонняя синхронизация** чата между Telegram и Minecraft
- **Цветное форматирование** в Minecraft (синий ник + белый текст)
- **Markdown форматирование** в Telegram (**жирный ник** + обычный текст)
- **Автоматическое определение** имени пользователя из Telegram

### 📊 **Мониторинг сервера**
- `/list` - список игроков онлайн
- `/status` - статус и загрузка сервера
- **Уведомления** о запуске/остановке сервера
- **Кэширование** для быстрых ответов

### ⚡ **Производительность**
- **Server-side only** - игрокам не нужно устанавливать мод
- **Асинхронная обработка** сообщений
- **Система очередей** с retry механизмом
- **Оптимизированное** сетевое взаимодействие

## 🚀 Установка

### Требования
- Minecraft Server 1.21.8
- Minecraft Forge 58.0.2+
- Telegram Bot Token

### Шаги установки

1. **Скачайте** последнюю версию мода из [Releases](../../releases)

2. **Поместите** JAR файл в папку `mods/` вашего сервера

3. **Создайте Telegram бота:**
   - Напишите [@BotFather](https://t.me/BotFather) в Telegram
   - Выполните команду `/newbot`
   - Получите Bot Token

4. **Настройте конфигурацию** в `config/nerrikatg-common.toml`:
   ```toml
   [telegram]
   bot_token = "YOUR_BOT_TOKEN"
   chat_id = YOUR_CHAT_ID
   topic_id = 0  # Опционально для топиков
   ```

5. **Запустите** сервер

## 📋 Команды Telegram

| Команда | Описание |
|---------|----------|
| `/list` | Показать список игроков онлайн |
| `/status` | Статус сервера и производительность |

## 🎨 Форматирование сообщений

### Telegram → Minecraft
```
[nickname]: сообщение
```
- `[nickname]:` отображается синим цветом
- `сообщение` отображается белым цветом

### Minecraft → Telegram
```
**[nickname]:** сообщение
```
- `[nickname]:` отображается жирным шрифтом
- `сообщение` отображается обычным шрифтом

## 🔧 Конфигурация

### Основные настройки

```toml
[telegram]
# Токен вашего Telegram бота
bot_token = "123456789:ABCdefGHIjklMNOpqrsTUVwxyz"

# ID чата/канала для отправки сообщений
chat_id = -1001234567890

# ID топика (опционально, для групп с топиками)
topic_id = 0
```

### Получение Chat ID

1. Добавьте бота в чат/канал
2. Отправьте любое сообщение
3. Откройте: `https://api.telegram.org/bot<BOT_TOKEN>/getUpdates`
4. Найдите `chat.id` в ответе

## 🏗️ Архитектура

### Модульная структура
```
com.shindaq.NerrikaTG/
├── commands/          # Обработчики команд
├── telegram/          # Telegram API интеграция
├── utils/             # Утилиты и хелперы
└── events/            # Обработчики игровых событий
```

### Ключевые компоненты
- **UpdateProcessor** - обработка входящих сообщений
- **MessageSender** - отправка сообщений с retry логикой
- **CommandRegistry** - регистрация и выполнение команд
- **ChatUtils** - форматирование сообщений

## 🛠️ Разработка

### Требования для разработки
- Java 17+
- IntelliJ IDEA или Eclipse
- Git

### Сборка из исходного кода

```bash
git clone https://github.com/YOUR_USERNAME/NerrikaTG.git
cd NerrikaTG
./gradlew build
```

### Запуск тестового сервера

```bash
./gradlew runServer
```

## 📝 Логирование

Мод использует стандартную систему логирования Minecraft:

```
[INFO] [NerrikaTG] Telegram polling started
[INFO] [CommandRegistry] Зарегистрирована команда: /list
[INFO] [UpdateProcessor] UpdateProcessor инициализирован с сервером
```

## 🤝 Вклад в проект

1. **Fork** репозиторий
2. **Создайте** feature ветку (`git checkout -b feature/amazing-feature`)
3. **Внесите** изменения и протестируйте
4. **Commit** изменения (`git commit -m 'Add amazing feature'`)
5. **Push** в ветку (`git push origin feature/amazing-feature`)
6. **Создайте** Pull Request

## 📄 Лицензия

Этот проект лицензирован под MIT License - см. файл [LICENSE](LICENSE) для деталей.

## 🙏 Благодарности

- **Minecraft Forge** - за отличный API для модификаций
- **Telegram Bot API** - за простой и мощный API
- **Сообщество Minecraft** - за вдохновение и поддержку

## 📞 Поддержка

Если у вас есть вопросы или проблемы:

1. Проверьте [Issues](../../issues) на наличие похожих проблем
2. Создайте новый [Issue](../../issues/new) с подробным описанием
3. Убедитесь, что приложили логи сервера

---

**Сделано с ❤️ для Minecraft сообщества**