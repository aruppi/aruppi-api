# ARUPPI API - Endpoints disponibles

Este documento resume todos los endpoints expuestos actualmente por el enrutado de Ktor.

- Prefijo base: `/api/v5`
- Metodo HTTP: `GET` en todos los casos
- Fuente: rutas en `src/main/kotlin/com/jeluchu/core/configuration/Routes.kt` y `src/main/kotlin/com/jeluchu/features/**/routes/*Routes.kt`

## Documentacion

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/docs` | `/api/v5/docs` | Ninguno | Ninguno |
| `/api/v5/swagger` | `/api/v5/swagger` | Ninguno | Ninguno |
| `/api/v5/openapi.yaml` | `/api/v5/openapi.yaml` | Ninguno | Ninguno |

## News

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/news/es` | `/api/v5/news/es` | Ninguno | Ninguno |
| `/api/v5/news/en` | `/api/v5/news/en` | Ninguno | Ninguno |

## Anime

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/anime` | `/api/v5/anime?type=tv&status=finished&nsfw=false` | `type`, `status` | `nsfw` (default: `false`) |
| `/api/v5/anime/{id}` | `/api/v5/anime/1` | `id` (path, MAL ID) | Ninguno |
| `/api/v5/anime/random` | `/api/v5/anime/random?nsfw=false` | Ninguno | `nsfw` (default: `false`) |
| `/api/v5/anime/lastEpisodes` | `/api/v5/anime/lastEpisodes` | Ninguno | Ninguno |
| `/api/v5/anime/suggestions` | `/api/v5/anime/suggestions?tags=action,comedy&nsfw=false` | `tags` (csv) | `nsfw` (default: `false`) |
| `/api/v5/anime/season` | `/api/v5/anime/season?year=2026&station=spring` | Ninguno | `year` (default: actual), `station` (default: temporada actual) |
| `/api/v5/anime/season/yearIndex` | `/api/v5/anime/season/yearIndex` | Ninguno | Ninguno |
| `/api/v5/anime/directory` | `/api/v5/anime/directory?type=tv&page=1&size=10` | Ninguno | `type`, `page` (default: `1`), `size` (default: `10`) |
| `/api/v5/anime/directory/{type}` | `/api/v5/anime/directory/tv?page=1&size=10` | `type` (path) | `page` (default: `1`), `size` (default: `10`) |

## Themes

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/themes/anime` | `/api/v5/themes/anime?page=1&size=25` | Ninguno | `page` (default: `1`), `size` (default: `25`) |
| `/api/v5/themes/anime/{slug}` | `/api/v5/themes/anime/cowboy_bebop` | `slug` (path) | Ninguno |
| `/api/v5/themes/anime/{slug}/random` | `/api/v5/themes/anime/cowboy_bebop/random` | `slug` (path) | Ninguno |
| `/api/v5/themes/artists` | `/api/v5/themes/artists?page=1&size=25` | Ninguno | `page` (default: `1`), `size` (default: `25`) |
| `/api/v5/themes/artists/{slug}` | `/api/v5/themes/artists/yoko_kanno` | `slug` (path) | Ninguno |
| `/api/v5/themes/songs` | `/api/v5/themes/songs?q=tank&page=1&size=25` | Ninguno | `q`, `page` (default: `1`), `size` (default: `25`) |
| `/api/v5/themes/songs/random` | `/api/v5/themes/songs/random` | Ninguno | Ninguno |

## Gallery

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/gallery` | `/api/v5/gallery?query=hatsune%20miku&page=1` | Ninguno | `query` (default: vacio), `page` (default: `1`) |
| `/api/v5/gallery/lastPosts` | `/api/v5/gallery/lastPosts?page=1` | Ninguno | `page` (default: `1`) |

## Rankings

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/top/anime` | `/api/v5/top/anime?type=tv&filter=airing&page=1&size=25` | `type` | `filter` (default: `airing`), `page` (default: `1`), `size` (default: `25`, max: `25`) |
| `/api/v5/top/anime/topTen` | `/api/v5/top/anime/topTen?type=tv&filter=airing` | `type` | `filter` (default: `airing`) |
| `/api/v5/top/manga` | `/api/v5/top/manga?type=manga&filter=publishing&page=1&size=25` | `type` | `filter` (default: `publishing`), `page` (default: `1`), `size` (default: `25`, max: `25`) |
| `/api/v5/top/people` | `/api/v5/top/people?page=1&size=25` | Ninguno | `page` (default: `1`), `size` (default: `25`, max: `25`) |
| `/api/v5/top/characters` | `/api/v5/top/characters?page=1&size=25` | Ninguno | `page` (default: `1`), `size` (default: `25`, max: `25`) |

## Schedule

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/schedule` | `/api/v5/schedule` | Ninguno | Ninguno |
| `/api/v5/schedule/{day}` | `/api/v5/schedule/monday` | `day` (path) | Ninguno |

## Anitakume

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/anitakume` | `/api/v5/anitakume` | Ninguno | Ninguno |

## Radio

| Endpoint | Ejemplo | Parametros obligatorios | Parametros opcionales |
|---|---|---|---|
| `/api/v5/radio` | `/api/v5/radio` | Ninguno | Ninguno |

## Nota rapida

Si agregas/modificas rutas, actualiza este archivo junto con las rutas en `src/main/kotlin/com/jeluchu/features/**/routes/*Routes.kt` para mantenerlo sincronizado.
