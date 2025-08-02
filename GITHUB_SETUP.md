# 🚀 Загрузка на GitHub

## 📋 Следующие шаги для загрузки проекта на GitHub:

### 1. 🌐 Создание репозитория на GitHub

1. Перейдите на [GitHub.com](https://github.com)
2. Нажмите зеленую кнопку **"New"** или **"+"** → **"New repository"**
3. Заполните данные:
   - **Repository name**: `NerrikaTG`
   - **Description**: `🤖 Minecraft Telegram Bridge - Server-side mod for seamless chat integration`
   - ✅ **Public** (или Private если хотите приватный репозиторий)
   - ❌ **Не создавайте** README, .gitignore, license (у нас уже есть)

4. Нажмите **"Create repository"**

### 2. 🔗 Подключение локального репозитория

После создания GitHub покажет команды. Выполните:

```bash
# Добавить удаленный репозиторий
git remote add origin https://github.com/YOUR_USERNAME/NerrikaTG.git

# Переименовать ветку в main (современный стандарт)
git branch -M main

# Загрузить код на GitHub
git push -u origin main
```

**Замените `YOUR_USERNAME` на ваше имя пользователя GitHub!**

### 3. ✅ Проверка

После успешной загрузки вы увидите:
- 📁 Все файлы проекта на GitHub
- 📋 Красивый README с описанием
- 📝 Changelog и документацию
- 🏷️ Лицензию MIT

### 4. 🎯 Рекомендации

#### Настройка репозитория:
- ✅ Включите **Issues** для багрепортов
- ✅ Включите **Discussions** для общения
- ✅ Добавьте **Topics**: `minecraft`, `forge`, `telegram`, `mod`, `java`
- ✅ Настройте **Releases** для версий

#### Защита ветки main:
1. Settings → Branches
2. Add rule для `main`
3. ✅ Require pull request reviews
4. ✅ Require status checks

### 5. 📦 Создание первого Release

1. Перейдите в **Releases** → **"Create a new release"**
2. Tag: `v1.0.0`
3. Title: `🎉 NerrikaTG v1.0.0 - Initial Release`
4. Описание из CHANGELOG.md
5. Приложите скомпилированный JAR файл

---

## 🔧 Команды Git для текущего проекта:

Репозиторий уже готов! Осталось только:

```bash
# 1. Добавить удаленный репозиторий (замените USERNAME)
git remote add origin https://github.com/YOUR_USERNAME/NerrikaTG.git

# 2. Переименовать ветку
git branch -M main

# 3. Загрузить на GitHub
git push -u origin main
```

**Готово! Ваш мод теперь будет на GitHub! 🎉**