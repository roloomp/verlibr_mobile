# Заметки по Verlibr Mobile (для Claude)

Рабочие пометки: что сделано, какие решения приняты, что отложено.
Обновлять при каждой итерации.

## Экран профиля (сделан, на mock-данных)

Файлы:
- `ui/screens/ProfileScreen.kt`: LazyColumn: баннер, карточка, вкладки, линия, контент, пагинация.
- `ui/components/ProfileComponents.kt`: ProfileBanner, ProfileInfoCard, ProfileChip, EmptyHint, RatedPoemCard, StatRow.
- `ui/viewmodels/ProfileViewModel.kt`: состояние и mock-данные, enum ProfileTab / ProfileSection.
- `data/Models.kt`: добавлен PoemRating (стих + 5 баллов, total считается).
- `Navigation.kt`: маршрут `profile` -> ProfileScreen.

Структура вкладок:
- ProfileTab (главные): Рецензии и оценка / Статистика / Понравилось.
- ProfileSection (подвкладки): у «Рецензии и оценка» это MyReviews, MyRatings;
  у «Понравилось» это LikedReviews, LikedPoems; у «Статистики» подвкладок нет.
- Подвкладки берутся из `ProfileTab.sections`. При смене вкладки выбирается первая секция.
- Пагинация общая: `page` сбрасывается в 1 при смене вкладки или секции, pageSize = 5.
  В статистике пагинации нет. Пустая секция показывает EmptyHint, пагинация остаётся (страница 1).

Решения:
- Лайки в профиле только отображаются: `ReviewCard(review)` без onLike, `LikePill` без onClick не кликабелен.
  Это поведение по умолчанию: лайкать можно только если передан onLike (так на экране стиха).
- «Оценки» = PoemRating: карточка со стихом слева, итогом и баллами справа (без текста рецензии).
- «Понравилось»: «Рецензии» (чужие, понравившиеся) и «Стихи». Второй раздел выбран потому, что
  лайкать можно рецензии и стихи, а оценки нельзя. Роман это не подтверждал.
- Статистика: дней с нами, рецензий, оценок, стихов в избранном, лайков поставлено, лайков получено.
  Часть значений считается из mock-списков, избранное (14) пока константа. Набор строк придуман
  по словам Романа, сверить с вебом (Site_version.docx), если понадобится.
- Баннер и аватар пока плейсхолдеры (Taupe, первая буква ника). Coil в зависимостях нет.

## Supabase: что подключено

- Схема БД (присланная Романом): authors, poems, ratings (рецензия и оценка в одной таблице, has_review),
  favorites, likes, review_likes, profile. Порядок критериев в БД: rhyme, style, structure,
  individuality, atmosphere. В приложении (ratingCriteria): рифмы, структура, стиль, ..., то есть
  стиль и структура поменяны местами: учитывать при маппинге ratings -> scores.
- `supabase/poem_previews.sql`: представление poem_previews (стих + likes_count, likes_month,
  reviews_count, security_invoker). Роман запускает его в SQL Editor. Читает likes и ratings,
  значит RLS должен разрешать читать их всем.
- `data/network/PoemService.kt`: `hot(page, pageSize)`: сортировка likes_month, затем likes_count, затем id.
  Горячее = лайки за 30 дней (решение Романа).
- `PagedPoemsViewModel` теперь с загрузкой: `load(page, pageSize)` переопределяется в наследнике,
  состояния loading/error, повтор по кнопке. HotViewModel грузит из Supabase, FavoriteViewModel пока mock.
- `PoemPreview.year` стал `Int?` (в БД year может быть null), в UI `year?.toString().orEmpty()`.
- Плагин kotlinx-serialization в build.gradle.kts есть (version 2.2.10, должна совпадать с версией Kotlin).
- storage-kt:2.4.0 в зависимостях не совпадает с BOM 3.2.6, сейчас не используется.

## Экран стиха (подключён к Supabase, не компилировал)

Файлы:
- `data/network/PoemDetailsService.kt`: `load(poemId, userId?)` параллельно (async) берёт стих, оценки с рецензиями
  (embed `profile(name)`), число лайков (count + range 0..0), лайк и избранное текущего пользователя, лайки рецензий.
  Запись: `setLike`, `setFavorite`, `setReviewLike` (insert/delete), `submitRating` (upsert, onConflict "poem_id,user_id").
- `ui/viewmodels/PoemViewModel.kt`: `load(userId)`, состояния loading/error, одноразовое `message` (Toast),
  лайки и избранное оптимистично с откатом при ошибке, защита от двойных тапов.
- `ui/screens/PoemScreen.kt`: параметры `userId`, `onRequireLogin`. Гость при тапе на лайк, избранное, лайк рецензии,
  отправку оценки получает окно входа. После входа `LaunchedEffect(userId)` перезагружает стих.
- `Navigation.kt`: `userId = authController.profile?.id`.

Решения и нюансы:
- Одна оценка на пользователя и стих (повторная отправка заменяет, id и лайки рецензии сохраняются,
  дата created_at остаётся прежней). Форма при загрузке заполняется собственной оценкой и рецензией.
- Порядок баллов: БД rhyme, style, structure, individuality, atmosphere; приложение рифмы, структура, стиль,
  индивидуальность, атмосфера. Маппинг в `RatingDto.appScores` и в `submitRating`.
- «Средняя с рецензией» = по оценкам с has_review, «без рецензии» = по всем оценкам. Итог считается как сумма баллов,
  а не из total_score.
- `Poem.year` теперь `Int?`.
- Лайки считаются клиентом из выборок (лайки рецензий: select по review_id in (...)), без представлений.
- Для работы upsert нужен unique (poem_id, user_id) в ratings. Роман согласился выполнить ограничения
  из списка ниже, подтверждения нет.

## Backend (со слов Романа, схема и политики присланы)

- Таблицы: authors, poems (tag_type, tag_genre, tag_mood, tag_versification, tag_foot, tag_stanza, tag_rhyme,
  is_daily, is_editors), ratings, favorites, likes, review_likes, profile (id = auth.users.id).
- RLS: чтение публичное везде; запись для authenticated: favorites own_all, likes и review_likes own_insert/own_delete,
  ratings own_write (ALL), profile own_update. Условия внутри политик не видел.
- Триггер `on_auth_user_created` на auth.users есть, тело функции не видел (какое поле имени берёт, неизвестно:
  приложение шлёт `name` в метаданных). Подтверждение email выключено.
- Unique-ограничения в схеме отсутствуют. Предложено: ratings (poem_id,user_id), favorites (poem_id,user_id),
  likes (poem_id,user_id), review_likes (review_id,user_id).
- profile.read_all отдаёт email всем, включая гостей (риск, упомянуть в курсовой).

## Что дальше (порядок согласован)

Избранное и профиль на реальных данных, поиск и фильтры, главная и страница автора.
Восстановление сессии при запуске отложено по решению Романа.

## Отложено до Supabase

1. (сделано) Профиль и Избранное закрыты для гостя: `AuthRequiredScreen` с текстом, вход через кнопку
   «Вход» в шапке. После входа в шапке имя (тап открывает профиль), выход кнопкой в профиле
   (`AuthService.logout()`, затем `authController.updateProfile(null)`). Состояние «вошёл» = `AuthController.profile != null`.
   Не сделано (решение Романа: пока не нужно): восстановление сессии при запуске.
   Риск: если строки в `profile` нет (например, включено подтверждение email), `getCurrentProfile()` вернёт null
   и интерфейс будет считать пользователя гостем. Не проверено.
2. Реальные данные: Profile, рецензии, оценки, лайки, избранное, статистика (счётчики лучше
   считать запросами, а не на клиенте).
3. Аватар и баннер из Profile.avatar / Profile.banner: нужна библиотека загрузки изображений.
4. У Review нет ссылки на стих. В профиле рецензия показывается без названия стиха,
   при подключении БД добавить поле.
5. В «Понравилось» нельзя снять лайк (карточки read-only). Если нужно, добавить onLike
   и удаление из списка.

## Грабли

- Файлы с CRLF, при правках сохранять.
- Сборку здесь проверить нельзя: честно писать, что не компилировал.
