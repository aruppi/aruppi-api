import { NextFunction, Request, Response } from 'express';
import { requestGot } from '../utils/requestCall';
import { transformUrlServer } from '../utils/transformerUrl';
import urls from '../utils/urls';

/*
  AnimeController - a class to manage the schedule,
  top, all the animes, return the last episodes
  with async to return promises.
*/

interface Schedule {
  title: string;
  mal_id: number;
  image_url: any;
}

interface Anime {
  index: string;
  animeId: string;
  title: string;
  id: string;
  type: string;
}

interface Top {
  rank: string;
  title: string;
  url: string;
  image_url: string;
  type: string;
  subtype: string;
  page: number;
  score: string;
}

interface Episode {
  id: string;
  title: string;
  image: string;
  episode: number;
  servers: { id: string; url: string; direct: boolean };
}

interface Movie {
  id: string;
  title: string;
  type: string;
  page: string;
  banner: string;
  image: string;
  synopsis: string;
  status: string;
  rate: string;
  genres: string[];
  episodes: object[];
}

export default class AnimeController {
  async schedule(req: Request, res: Response, next: NextFunction) {
    const { day } = req.params;
    let data: any;

    try {
      data = await requestGot(`${urls.BASE_JIKAN}schedule/${day}`, {
        parse: true,
        scrapy: false,
      });
    } catch (err) {
      return next(err);
    }

    const animeList: Schedule[] = data[day].map((item: Schedule) => ({
      title: item.title,
      mal_id: item.mal_id,
      image: item.image_url,
    }));

    if (animeList.length > 0) {
      res.status(200).json({
        animeList,
      });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async top(req: Request, res: Response, next: NextFunction) {
    const { type, subtype, page } = req.params;

    let data: any;

    try {
      if (subtype !== undefined) {
        data = await requestGot(
          `${urls.BASE_JIKAN}top/${type}/${page}/${subtype}`,
          { parse: true, scrapy: false },
        );
      } else {
        data = await requestGot(`${urls.BASE_JIKAN}top/${type}/${page}`, {
          parse: true,
          scrapy: false,
        });
      }
    } catch (err) {
      return next(err);
    }

    const top: Top[] = data.top.map((item: Top) => ({
      rank: item.rank,
      title: item.title,
      url: item.url,
      image_url: item.image_url,
      type: item.type,
      subtype: item.subtype,
      page: item.page,
      score: item.score,
    }));

    if (top.length > 0) {
      return res.status(200).json({ top });
    } else {
      return res.status(400).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getAllAnimes(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      data = await requestGot(`${urls.BASE_ANIMEFLV}api/animes/list`, {
        parse: true,
        scrapy: false,
      });
    } catch (err) {
      return next(err);
    }

    const animes: Anime[] = data.map((item: any) => ({
      index: item[0],
      animeId: item[3],
      title: item[1],
      id: item[2],
      type: item[4],
    }));

    if (animes.length > 0) {
      res.status(200).send({ animes });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getLastEpisodes(req: Request, res: Response, next: NextFunction) {
    let data: any;
    let episodes: Episode[] = [];

    try {
      data = await requestGot(`${urls.BASE_ANIMEFLV_JELU}LatestEpisodesAdded`, {
        parse: true,
        scrapy: false,
      });
    } catch (err) {
      return next(err);
    }

    for (const episode of data.episodes) {
      const formattedEpisode: Episode = {
        id: episode.id,
        title: episode.title,
        image: episode.poster,
        episode: episode.episode,
        servers: await transformUrlServer(episode.servers),
      };

      episodes.push(formattedEpisode);
    }

    if (episodes.length > 0) {
      res.status(200).json({
        episodes,
      });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getContent(req: Request, res: Response, next: NextFunction) {
    const { type, page, url } = req.params;
    let data: any;

    try {
      data = await requestGot(
        `${urls.BASE_ANIMEFLV_JELU}${
          url.charAt(0).toUpperCase() + url.slice(1)
        }/${type}/${page}`,
        {
          parse: true,
          scrapy: false,
        },
      );
    } catch (err) {
      return next(err);
    }

    const animes: Movie[] = data[url.toLowerCase()].map((item: any) => {
      return {
        id: item.id,
        title: item.title,
        type: url.toLowerCase(),
        page: page,
        banner: item.banner,
        image: item.poster,
        synopsis: item.synopsis,
        status: item.debut,
        rate: item.rating,
        genres: item.genres.map((genre: any) => genre),
        episodes: item.episodes.map((episode: any) => episode),
      };
    });

    if (animes.length > 0) {
      res.status(200).json({
        animes,
      });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }
}
