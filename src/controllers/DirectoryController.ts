import { NextFunction, Request, Response } from 'express';
import { requestGot } from '../utils/requestCall';
import AnimeModel, { Anime } from '../database/models/anime.model';
import {
  animeExtraInfo,
  getAnimeVideoPromo,
  getAnimeCharacters,
  getRelatedAnimesFLV,
  getRelatedAnimesMAL,
} from '../utils/util';
import urls from '../utils/urls';

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
        res.status(200).json(
          await AnimeModel.find({
            genres: { $nin: ['ecchi', 'Ecchi'] },
          }),
        );
      } else {
        res.status(200).json(await AnimeModel.find());
      }
    } catch (err) {
      return next(err);
    }
  }

  async getSeason(req: Request, res: Response, next: NextFunction) {
    const { year, type } = req.params;
    let data: any;

    try {
      data = await requestGot(`${urls.BASE_JIKAN}season/${year}/${type}`, {
        scrapy: false,
        parse: true,
      });
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
      data = await requestGot(`${urls.BASE_JIKAN}season/archive`, {
        parse: true,
        scrapy: false,
      });
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
      res.status(200).json({ archive });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async laterSeasons(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      data = await requestGot(`${urls.BASE_JIKAN}season/later`, {
        parse: true,
        scrapy: false,
      });
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
      res.status(200).json({ future });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getMoreInfo(req: Request, res: Response, next: NextFunction) {
    const { title } = req.params;
    let resultAnime: Anime | null;

    try {
      resultAnime = await AnimeModel.findOne({
        $or: [{ title: { $eq: title } }, { title: { $eq: `${title} (TV)` } }],
      });
    } catch (err) {
      return next(err);
    }

    if (resultAnime) {
      if (!resultAnime?.jkanime) {
        res.status(200).json({
          title: resultAnime.title || null,
          poster: resultAnime.poster || null,
          synopsis: resultAnime.description || null,
          status: resultAnime.state || null,
          type: resultAnime.type || null,
          rating: resultAnime.score || null,
          genres: resultAnime.genres || null,
          moreInfo: await animeExtraInfo(resultAnime.mal_id),
          promo: await getAnimeVideoPromo(resultAnime.mal_id),
          characters: await getAnimeCharacters(resultAnime.mal_id),
          related: await getRelatedAnimesFLV(resultAnime.id),
        });
      } else {
        res.status(200).json({
          title: resultAnime.title || null,
          poster: resultAnime.poster || null,
          synopsis: resultAnime.description || null,
          status: resultAnime.state || null,
          type: resultAnime.type || null,
          rating: resultAnime.score || null,
          genres: resultAnime.genres || null,
          moreInfo: await animeExtraInfo(resultAnime.mal_id),
          promo: await getAnimeVideoPromo(resultAnime.mal_id),
          characters: await getAnimeCharacters(resultAnime.mal_id),
          related: await getRelatedAnimesMAL(resultAnime.mal_id),
        });
      }
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
}
