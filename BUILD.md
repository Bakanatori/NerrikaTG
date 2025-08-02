# Инструкции по сборке NerrikaTG

## Требования

- Java 17 или выше
- Gradle 8.0 или выше
- Minecraft Forge MDK для версии 1.21.8

## Сборка

### 1. Подготовка окружения

Убедитесь, что у вас установлена Java 17+:

```bash
java -version
```

### 2. Сборка мода

В корневой папке проекта выполните:

```bash
# Для Windows
gradlew build

# Для Linux/Mac
./gradlew build
```

### 3. Результат сборки

После успешной сборки JAR файл будет находиться в:
```
build/libs/nerrikatg-1.0.0.jar
```

## Разработка

### Структура проекта

```
src/main/java/com/shindaq/NerrikaTG/
├── NerrikaCore.java          # Основной класс мода
├── Config.java               # Конфигурация
├── TelegramService.java      # Сервис для работы с Telegram API
├── commands/                 # Команды
│   ├── ListCommand.java
│   └── ServerInfoCommand.java
└── utils/                    # Утилиты
    ├── ChatUtils.java
    ├── JsonUtils.java
    ├── MessageUtils.java
    └── PlayerUtils.java
```

### Добавление новых команд

1. Создайте новый класс в папке `commands/`
2. Реализуйте метод `register()` для регистрации команды
3. Добавьте импорт и регистрацию в `NerrikaCore.java`

Пример:

```java
public class MyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mycommand")
            .requires(source -> source.hasPermission(0))
            .executes(context -> {
                return executeMyCommand(context.getSource());
            })
        );
    }
    
    private static int executeMyCommand(CommandSourceStack source) {
        // Ваша логика команды
        return 1;
    }
}
```

### Добавление новых утилит

1. Создайте новый класс в папке `utils/`
2. Сделайте методы статическими для удобства использования
3. Добавьте документацию к методам

### Тестирование

1. Соберите мод: `gradlew build`
2. Скопируйте JAR файл в папку `mods` тестового сервера
3. Настройте конфигурацию
4. Запустите сервер и проверьте функциональность

## Отладка

### Логирование

Мод использует SLF4J для логирования. Логи можно найти в:
- `logs/latest.log` - основной лог сервера
- `logs/debug.log` - отладочная информация

### Уровни логирования

- `INFO` - основная информация о работе мода
- `WARN` - предупреждения о неправильной настройке
- `ERROR` - ошибки при работе с Telegram API
- `DEBUG` - детальная информация для отладки

### Включение отладочного режима

Добавьте в `log4j2.xml`:

```xml
<Logger name="com.shindaq.NerrikaTG" level="debug" additivity="false">
    <AppenderRef ref="File"/>
    <AppenderRef ref="Console"/>
</Logger>
```

## Публикация

### Подготовка к релизу

1. Обновите версию в `build.gradle`
2. Проверьте все импорты и зависимости
3. Протестируйте на разных версиях Minecraft
4. Обновите документацию

### Создание релиза

1. Соберите финальную версию: `gradlew build`
2. Создайте тег в Git: `git tag v1.0.0`
3. Загрузите JAR файл на платформу распространения
4. Обновите README с информацией о новой версии

## Советы по разработке

### Производительность

- Используйте асинхронные операции для сетевых запросов
- Ограничивайте размер кэшей и коллекций
- Избегайте блокирующих операций в основном потоке

### Безопасность

- Всегда проверяйте входные данные
- Используйте try-catch для обработки исключений
- Не храните чувствительную информацию в коде

### Совместимость

- Тестируйте на разных версиях Forge
- Используйте только стабильные API Minecraft
- Избегайте прямого доступа к внутренним классам

## Полезные ссылки

- [Forge Documentation](https://mcforge.readthedocs.io/)
- [Minecraft Wiki](https://minecraft.wiki/)
- [Telegram Bot API](https://core.telegram.org/bots/api) 