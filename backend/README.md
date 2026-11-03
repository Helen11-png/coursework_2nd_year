# Бекенд
Кобзев А.А. - бекенд бизнес/API
Никитинская А.А. - бекенд с уклоном в DevOps

### СЛЕДУЮЩЕЕ - ВРЕМЕННОЕ README, БУДЕТ МЕНЯТЬСЯ
# Быстрый старт backend

Проект уже создан, заново генерировать его в Spring Initializr не нужно.

## Что понадобится
- Git
- JDK 21 или новее (`java -version`)
- Docker Desktop (запущенный)
- IDE (IntelliJ IDEA или другая). Maven ставить не нужно, в проекте есть `mvnw`.

## 1. Клонировать репозиторий
```bash
git clone https://github.com/Helen11-png/coursework_2nd_year.git
cd coursework_2nd_year
git checkout -b feature/<твоя-задача>
```
Работаем в своих ветках, в `main` напрямую не пушим, изменения идут через pull request.

## 2. Поднять базу данных
```bash
cd infra
cp .env.example .env        # в PowerShell: copy .env.example .env
docker compose up -d db
docker compose ps           # статус db должен быть healthy
```
Файл `.env` в git не попадает, в репозитории лежит только `.env.example`.

Если порт 5432 на компьютере уже занят (например, локальный PostgreSQL),
поменяйте в `infra/.env` значение `DB_PORT` (например, на 5433) и перезапустите `docker compose up -d db`.
Тогда при запуске приложения нужно указать тот же порт (см. ниже).

## 3. Запустить приложение
Из папки `backend/`:
```bash
./mvnw spring-boot:run          # Windows PowerShell: .\mvnw spring-boot:run
```
Если порт базы не 5432, перед запуском задайте адрес (PowerShell):
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5433/mailforge"
```
Пароль и пользователь по умолчанию: `mailforge` / `change_me`. В `infra/.env` должен быть тот же `POSTGRES_PASSWORD`.

## 4. Проверить
- http://localhost:8080/actuator/health должен вернуть `{"status":"UP"}`
- таблицы в БД видны через DBeaver или `psql`, подключение к `localhost:<DB_PORT>`, база `mailforge`

## Типичные проблемы
| Ошибка | Причина и решение |
|---|---|
| `password authentication failed` | пароль в базе не совпадает с паролем приложения. Пароль запоминается при первом создании тома: пересоздайте базу командой `docker compose down -v`, затем `up -d db` в `infra/` |
| `Connection refused` | контейнер не запущен или порт другой: `docker compose ps` |
| `no configuration file provided` | `docker compose` нужно запускать из папки `infra/` |
| порт 5432 занят | см. пункт 2 |

## Структура
- `backend/`: Spring Boot приложение (Java, Maven)
- `infra/`: docker-compose и переменные окружения
- `docs/`: документация
- `frontend/`, `1c/`, `bruno/`: фронтенд, 1С, тестовые запросы к API

## Правила работы
- Ветка на каждую задачу: `feature/<кратко>`
- Изменения в базе только через миграции Flyway: `backend/src/main/resources/db/migration/V<номер>__<описание>.sql`. Уже закоммиченные миграции не правим, добавляем новые.
- Пакеты `common` и `config` ведёт Алёна. Нужна правка там, пишите ей.