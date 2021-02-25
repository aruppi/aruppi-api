import { Request, Response } from 'express';
import { requestGot } from '../utils/requestCall';
import urls from '../utils/urls';

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

export default class AnimeController {
  async schedule(req: Request, res: Response) {
    const { day } = req.params;

    const data: any = await requestGot(`${urls.BASE_JIKAN}schedule/${day}`, {
      parse: true,
      scrapy: false,
    });

    const animeList: Schedule[] = data[day].map((doc: Schedule) => ({
      title: doc.title,
      mal_id: doc.mal_id,
      image: doc.image_url,
    }));

    if (animeList.length > 0) {
      res.status(200).json({
        animeList,
      });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async top(req: Request, res: Response) {
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
    } catch (error) {
      console.log(error);
      return res.status(404);
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

  async getAllAnimes(req: Request, res: Response) {
    const data: any = await requestGot(`${urls.BASE_ANIMEFLV}api/animes/list`, {
      parse: true,
      scrapy: false,
    });

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
}
