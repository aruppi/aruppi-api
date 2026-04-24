# ARUPPI API - Endpoints disponibles

Este documento resume **todos los endpoints expuestos actualmente** por el enrutado de Ktor.

- Prefijo base: `/api/v5`
- Método HTTP: `GET` en todos los casos
- Fuente: rutas en `src/main/kotlin/com/jeluchu/core/configuration/Routes.kt` y `src/main/kotlin/com/jeluchu/features/**/routes/*Routes.kt`

## Documentacion

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | `/api/v5/docs` | ReDoc (UI principal) |
| GET | `/api/v5/swagger` | Swagger UI (fallback interactivo) |
| GET | `/api/v5/openapi.yaml` | Especificacion OpenAPI en YAML |

## News

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | `/api/v5/news/es` | Noticias en espanol |
| GET | `/api/v5/news/en` | Noticias en ingles |

## Anime

| Metodo | Endpoint | Descripcion | Query params |
|---|---|---|---|
| GET | `/api/v5/anime` | Listado filtrado por tipo/estado | `type`, `status`, `nsfw` |
| GET | `/api/v5/anime/{id}` | Detalle por MAL ID | - |
| GET | `/api/v5/anime/random` | Anime aleatorio | `nsfw` (opcional) |
| GET | `/api/v5/anime/lastEpisodes` | Ultimos episodios por dia actual | - |
| GET | `/api/v5/anime/suggestions` | Sugerencias por tags | `tags` (csv), `nsfw` (opcional) |
| GET | `/api/v5/anime/season` | Anime por temporada | `year`, `station` |
| GET | `/api/v5/anime/season/yearIndex` | Indice de years/seasons disponibles | - |
| GET | `/api/v5/anime/directory` | Directorio general | `type` (opcional), `page`, `size` |
| GET | `/api/v5/anime/directory/{type}` | Directorio por tipo en path | `page`, `size` |

## Themes

| Metodo | Endpoint | Descripcion | Query params |
|---|---|---|---|
| GET | `/api/v5/themes/anime` | Listado de animes con themes | `page`, `size` |
| GET | `/api/v5/themes/anime/{slug}` | Detalle de themes por anime | - |
| GET | `/api/v5/themes/anime/{slug}/random` | Theme aleatorio de un anime | - |
| GET | `/api/v5/themes/artists` | Listado de artistas | `page`, `size` |
| GET | `/api/v5/themes/artists/{slug}` | Detalle de artista | - |
| GET | `/api/v5/themes/songs` | Busqueda/listado de canciones | `q` (opcional), `page`, `size` |
| GET | `/api/v5/themes/songs/random` | Cancion aleatoria | - |

## Gallery

| Metodo | Endpoint | Descripcion | Query params |
|---|---|---|---|
| GET | `/api/v5/gallery` | Imagenes por busqueda/tag | `query`, `page` |
| GET | `/api/v5/gallery/lastPosts` | Ultimos posts de galeria | `page` |

## Rankings

| Metodo | Endpoint | Descripcion | Query params |
|---|---|---|---|
| GET | `/api/v5/top/anime` | Ranking de anime | `type`, `filter`, `page`, `size` |
| GET | `/api/v5/top/anime/topTen` | Top ten de anime | `type`, `filter` |
| GET | `/api/v5/top/manga` | Ranking de manga | `type`, `filter`, `page`, `size` |
| GET | `/api/v5/top/people` | Ranking de people | `page`, `size` |
| GET | `/api/v5/top/characters` | Ranking de characters | `page`, `size` |

## Schedule

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | `/api/v5/schedule` | Programacion semanal |
| GET | `/api/v5/schedule/{day}` | Programacion por dia |

## Anitakume

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | `/api/v5/anitakume` | Episodios/podcast de Anitakume |

## Radio

| Metodo | Endpoint | Descripcion |
|---|---|---|
| GET | `/api/v5/radio` | Listado de radios |

## Nota rapida

Si agregas/modificas rutas, actualiza este archivo junto con las rutas en `src/main/kotlin/com/jeluchu/features/**/routes/*Routes.kt` para mantenerlo sincronizado.


