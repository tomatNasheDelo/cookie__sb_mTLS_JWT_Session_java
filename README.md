# Cookie, Session, JWT & mTLS Lab — Java

Учебный проект: React/Vite 7 + Spring Boot/Java с тремя отдельными темами аутентификации и транспорта: серверная сессия через cookie, подписанный JWT и mTLS для backend-вызова внутреннего сервиса.

Сохранены исходная структура репозитория и Gradle Kotlin DSL; Kotlin-код backend-сервисов переписан на Java 17. Подробные инструкции приведены в [`start.txt`](start.txt).

Быстрый старт из корня:

```bash
./scripts/generate-certs.sh
docker compose up --build
```

Откройте http://localhost:5173. Demo login: `student / change-me-local`.

Это учебный стенд без базы данных. Не используйте демонстрационные учётные данные, ключи или значения cookies в production.
