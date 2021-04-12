import { NextFunction, Request, Response } from 'express';
import { requestGot } from '../utils/requestCall';
import AnimeModel, { Anime } from '../database/models/anime.model';
import GenreModel, { Genre } from '../database/models/genre.model';
import util from 'util';
import { hashStringMd5 } from '../utils/util';
import {
  animeExtraInfo,
  getAnimeVideoPromo,
  getAnimeCharacters,
  getRelatedAnimesFLV,
  getRelatedAnimesMAL,
} from '../utils/util';
import urls from '../utils/urls';
import { redisClient } from '../database/connection';

// @ts-ignore
redisClient.get = util.promisify(redisClient.get);

/*
  DirectoryController - async functions controlling the directory
  in the database of MongoDB, functions like getAllDirectory from the DB
  other functions with realation to the directory, like the season and stuff.
*/

interface TypeAnime {
  title: string;
  image: string;
  genres: string[];
}

interface Season {
  title: string;
  image: string;
  malink: string;
}

interface Archive {
  year: string;
  seasons: string[];
}

export default class DirectoryController {
  async getAllDirectory(req: Request, res: Response, next: NextFunction) {
    const { genres } = req.params;

    try {
      if (genres === 'sfw') {
        await AnimeModel.find(
          {
            genres: { $nin: ['ecchi', 'Ecchi'] },
          },
          (err: any, docs: Anime[]) => {
            let directory: any[] = [];

            for (const item of docs) {
              directory.push({
                id: item.id,
                title: item.title,
                mal_id: item.mal_id,
                poster: item.poster,
                type: item.type,
                genres: item.genres,
                state: item.state,
                score: item.score,
                source: item.source,
                description: item.description,
              });
            }

            if (directory.length > 0) {
              res.status(200).json({ directory });
            } else {
              res.status(500).json({ message: 'Aruppi lost in the shell' });
            }
          },
        );
      } else {
        await AnimeModel.find((err: any, docs: Anime[]) => {
          let directory: any[] = [];

          for (const item of docs) {
            directory.push({
              id: item.id,
              title: item.title,
              mal_id: item.mal_id,
              poster: item.poster,
              type: item.type,
              genres: item.genres,
              state: item.state,
              score: item.score,
              source: item.source,
              description: item.description,
            });
          }

          if (directory.length > 0) {
            res.status(200).json({ directory });
          } else {
            res.status(500).json({ message: 'Aruppi lost in the shell' });
          }
        });
      }
    } catch (err) {
      return next(err);
    }
  }

  async getSeason(req: Request, res: Response, next: NextFunction) {
    const { year, type } = req.params;
    let data: any;

    try {
      const resultQueryRedis: any = await redisClient.get(
        `season_${hashStringMd5(`${year}:${type}`)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return res.status(200).json(resultRedis);
      } else {
        data = await requestGot(`${urls.BASE_JIKAN}season/${year}/${type}`, {
          scrapy: false,
          parse: true,
        });
      }
    } catch (err) {
      return next(err);
    }

    const season: TypeAnime[] = data.anime.map((item: any) => {
      return {
        title: item.title,
        image: item.image_url,
        genres: item.genres.map((genre: any) => genre.name),
      };
    });

    if (season.length > 0) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `season_${hashStringMd5(`${year}:${type}`)}`,
        JSON.stringify({ season }),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `season_${hashStringMd5(`${year}:${type}`)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );

      res.status(200).json({
        season,
      });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async allSeasons(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      const resultQueryRedis: any = await redisClient.get(
        `allSeasons_${hashStringMd5('allSeasons')}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return res.status(200).json(resultRedis);
      } else {
        data = await requestGot(`${urls.BASE_JIKAN}season/archive`, {
          parse: true,
          scrapy: false,
        });
      }
    } catch (err) {
      return next(err);
    }

    const archive: Archive[] = data.archive.map((item: any) => {
      return {
        year: item.year,
        seasons: item.seasons,
      };
    });

    if (archive.length > 0) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `allSeasons_${hashStringMd5('allSeasons')}`,
        JSON.stringify({ archive }),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `allSeasons_${hashStringMd5('allSeasons')}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );

      res.status(200).json({ archive });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async laterSeasons(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      const resultQueryRedis: any = await redisClient.get(
        `laterSeasons_${hashStringMd5('laterSeasons')}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return res.status(200).json(resultRedis);
      } else {
        data = await requestGot(`${urls.BASE_JIKAN}season/later`, {
          parse: true,
          scrapy: false,
        });
      }
    } catch (err) {
      return next(err);
    }

    const future: Season[] = data.anime.map((item: any) => {
      return {
        title: item.title,
        image: item.image_url,
        malink: item.url,
      };
    });

    if (future.length > 0) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `laterSeasons_${hashStringMd5('laterSeasons')}`,
        JSON.stringify({ future }),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `laterSeasons_${hashStringMd5('laterSeasons')}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );

      res.status(200).json({ future });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getMoreInfo(req: Request, res: Response, next: NextFunction) {
    const { title } = req.params;
    let resultQuery: Anime | null;
    let resultAnime: any;

    try {
      const resultQueryRedis: any = await redisClient.get(
        `moreInfo_${hashStringMd5(title)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return res.status(200).json(resultRedis);
      } else {
        resultQuery = await AnimeModel.findOne({
          $or: [{ title: { $eq: title } }, { title: { $eq: `${title} (TV)` } }],
        });

        const extraInfo: any = await animeExtraInfo(resultQuery!.mal_id);

        switch (resultQuery?.source) {
          case 'animeflv':
            resultAnime = {
              title: resultQuery?.title,
              poster: resultQuery?.poster,
              synopsis: resultQuery?.description,
              status: !extraInfo.aired.to ? 'En emisión' : 'Finalizado',
              type: resultQuery?.type,
              rating: resultQuery?.score,
              genres: resultQuery?.genres,
              moreInfo: [extraInfo],
              promo: await getAnimeVideoPromo(resultQuery!.mal_id),
              characters: await getAnimeCharacters(resultQuery!.mal_id),
              related: await getRelatedAnimesFLV(resultQuery!.id),
            };
            break;
          case 'jkanime':
            resultAnime = {
              title: resultQuery?.title,
              poster: resultQuery?.poster,
              synopsis: resultQuery?.description,
              status: !extraInfo.aired.to ? 'En emisión' : 'Finalizado',
              type: resultQuery?.type,
              rating: resultQuery?.score,
              genres: resultQuery?.genres,
              moreInfo: [extraInfo],
              promo: await getAnimeVideoPromo(resultQuery!.mal_id),
              characters: await getAnimeCharacters(resultQuery!.mal_id),
              related: await getRelatedAnimesMAL(resultQuery!.mal_id),
            };
            break;
          case 'monoschinos':
            resultAnime = {
              title: resultQuery?.title,
              poster: resultQuery?.poster,
              synopsis: resultQuery?.description,
              status: !extraInfo.aired.to ? 'En emisión' : 'Finalizado',
              type: resultQuery?.type,
              rating: resultQuery?.score,
              genres: resultQuery?.genres,
              moreInfo: [extraInfo],
              promo: await getAnimeVideoPromo(resultQuery!.mal_id),
              characters: await getAnimeCharacters(resultQuery!.mal_id),
              related: await getRelatedAnimesMAL(resultQuery!.mal_id),
            };
            break;
          default:
            resultAnime = undefined;
            break;
        }
      }
    } catch (err) {
      return next(err);
    }

    if (resultAnime) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `moreInfo_${hashStringMd5(title)}`,
        JSON.stringify(resultAnime),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `moreInfo_${hashStringMd5(title)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );

      res.status(200).json(resultAnime);
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async search(req: Request, res: Response, next: NextFunction) {
    const { title } = req.params;
    let results: Anime[] | null;

    try {
      results = await AnimeModel.find({
        title: { $regex: new RegExp(title, 'i') },
      });
    } catch (err) {
      return next(err);
    }

    const resultAnimes: any[] = results.map((item: any) => {
      return {
        id: item.id,
        title: item.title,
        type: item.type,
        image: item.poster,
      };
    });

    if (resultAnimes.length > 0) {
      res.status(200).json({ search: resultAnimes });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getAnimeGenres(req: Request, res: Response, next: NextFunction) {
    const { genre, order, page } = req.params;
    let data: Genre[];
    let resultReq: any;

    try {
      if (genre === undefined && order === undefined && page === undefined) {
        data = await GenreModel.find();
      } else {
        if (page !== undefined) {
          resultReq = await requestGot(
            `${urls.BASE_ANIMEFLV_JELU}Genres/${genre}/${order}/${page}`,
            { parse: true, scrapy: false },
          );
        } else {
          resultReq = await requestGot(
            `${urls.BASE_ANIMEFLV_JELU}Genres/${genre}/${order}/1`,
            { parse: true, scrapy: false },
          );
        }
      }
    } catch (err) {
      return next(err);
    }

    const animes: any[] = resultReq.animes.map((item: any) => {
      return {
        id: item.id,
        title: item.title.trim(),
        mention: genre,
        page: page,
        poster: item.poster,
        banner: item.banner,
        synopsis: item.synopsis,
        type: item.type,
        rating: item.rating,
        genre: item.genre,
      };
    });

    if (animes.length > 0) {
      res.status(200).json({ animes });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }
}
