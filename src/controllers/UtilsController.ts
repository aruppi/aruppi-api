import { NextFunction, Request, Response } from 'express';
import Parser from 'rss-parser';
import urls from '../utils/urls';
import { obtainPreviewNews } from '../utils/obtainPreviews';
import { requestGot } from '../utils/requestCall';
import RadioStationModel, {
  RadioStation,
} from '../database/models/radiostation.model';
import ThemeModel, { Theme } from '../database/models/theme.model';
import ThemeParser from '../utils/animeTheme';
import { structureThemes } from '../utils/util';
import { getThemes } from '../utils/util';
import WaifuModel, { Waifu } from '../database/models/waifu.model';

/*
  UtilsController - controller to parse the
  feed and get news, all with scraping and
  parsing RSS.
*/

const themeParser = new ThemeParser();

type CustomFeed = {
  foo: string;
};
type CustomItem = {
  bar: number;
  itunes: { duration: string; image: string };
  'content:encoded': string;
  'content:encodedSnippet': string;
};

const parser: Parser<CustomFeed, CustomItem> = new Parser({
  customFields: {
    feed: ['foo'],
    item: ['bar'],
  },
});

interface News {
  title?: string;
  url?: string;
  author?: string;
  thumbnail?: string;
  content?: string;
}

interface Podcast {
  title?: string;
  duration?: string;
  created?: Date | string;
  mp3?: string;
}

interface rssPage {
  url: string;
  author: string;
  content: string;
}

export default class UtilsController {
  async getAnitakume(req: Request, res: Response, next: NextFunction) {
    let feed: CustomFeed & Parser.Output<CustomItem>;

    try {
      feed = await parser.parseURL(urls.BASE_IVOOX);
    } catch (err) {
      return next(err);
    }

    const podcast: Podcast[] = [];
    const monthNames = [
      'Enero',
      'Febrero',
      'Marzo',
      'Abril',
      'Mayo',
      'Junio',
      'Julio',
      'Agosto',
      'Septiembre',
      'Octubre',
      'Noviembre',
      'Diciembre',
    ];

    feed.items.forEach((item: any) => {
      const date: Date = new Date(item.pubDate!);

      const formattedObject: Podcast = {
        title: item.title,
        duration: item.itunes.duration,
        created: `${date.getDate()} de ${
          monthNames[date.getMonth()]
        } de ${date.getFullYear()}`,
        mp3: item.enclosure?.url,
      };

      podcast.push(formattedObject);
    });

    if (podcast.length > 0) {
      res.status(200).json({ podcast });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getNews(req: Request, res: Response, next: NextFunction) {
    const news: News[] = [];
    const pagesRss: rssPage[] = [
      { url: urls.BASE_KUDASAI, author: 'Kudasai', content: 'content_encoded' },
      {
        url: urls.BASE_PALOMITRON,
        author: 'Palomitron',
        content: 'description',
      },
      {
        url: urls.BASE_RAMENPARADOS,
        author: 'Ramen para dos',
        content: 'content',
      },
      {
        url: urls.BASE_CRUNCHYROLL,
        author: 'Crunchyroll',
        content: 'content_encoded',
      },
    ];

    try {
      for (const rssPage of pagesRss) {
        const feed = await parser.parseURL(rssPage.url);

        feed.items.forEach((item: any) => {
          const formattedObject: News = {
            title: item.title,
            url: item.link,
            author: feed.title?.includes('Crunchyroll')
              ? 'Crunchyroll'
              : feed.title,
            thumbnail: obtainPreviewNews(item['content:encoded']),
            content: item['content:encoded'],
          };

          news.push(formattedObject);
        });
      }
    } catch (err) {
      return next(err);
    }

    res.json({ news });
  }

  async getImages(req: Request, res: Response, next: NextFunction) {
    const { title } = req.params;
    let data: any;

    try {
      data = await requestGot(
        `${urls.BASE_QWANT}count=51&q=${title}&t=images&safesearch=1&locale=es_ES&uiv=4`,
        { scrapy: false, parse: true },
      );
    } catch (err) {
      return next(err);
    }

    const results: any[] = data.data.result.items.map((item: any) => {
      return {
        type: item.thumb_type,
        thumbnail: `https:${item.thumbnail}`,
        fullsize: `https:${item.media_fullsize}`,
      };
    });

    if (results.length > 0) {
      res.status(200).json({ images: results });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getVideos(req: Request, res: Response, next: NextFunction) {
    const { channelId } = req.params;
    let data: any;

    try {
      data = await requestGot(
        `${urls.BASE_YOUTUBE}${channelId}&part=snippet,id&order=date&maxResults=50`,
        { scrapy: false, parse: true },
      );
    } catch (err) {
      return next(err);
    }

    const results: any[] = data.items.map((item: any) => {
      return {
        title: item.snippet.title,
        videoId: item.id.videoId,
        thumbDefault: item.snippet.thumbnails.default.url,
        thumbMedium: item.snippet.thumbnails.medium.url,
        thumbHigh: item.snippet.thumbnails.high.url,
      };
    });

    if (results.length > 0) {
      res.status(200).json({ videos: results });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getSectionVideos(req: Request, res: Response, next: NextFunction) {
    const { type } = req.params;
    let y1: any, y2: any, y3: any;
    let data: any;

    try {
      if (type === 'learn') {
        data = await requestGot(
          `${urls.BASE_YOUTUBE}UCCyQwSS6m2mVB0-H2FOFJtw&part=snippet,id&order=date&maxResults=50`,
          { parse: true, scrapy: false },
        );
      } else if (type === 'amv') {
        y1 = await requestGot(
          `${urls.BASE_YOUTUBE}UCkTFkshjAsLMKwhAe1uPC1A&part=snippet,id&order=date&maxResults=25`,
          { parse: true, scrapy: false },
        );

        y2 = await requestGot(
          `${urls.BASE_YOUTUBE}UC2cpvlLeowpqnR6bQofwNew&part=snippet,id&order=date&maxResults=25`,
          { parse: true, scrapy: false },
        );
      } else if (type === 'produccer') {
        y1 = await requestGot(
          `${urls.BASE_YOUTUBE}UC-5MT-BUxTzkPTWMediyV0w&part=snippet,id&order=date&maxResults=25`,
          { parse: true, scrapy: false },
        );

        y2 = await requestGot(
          `${urls.BASE_YOUTUBE}UCwUeTOXP3DD9DIvHttowuSA&part=snippet,id&order=date&maxResults=25`,
          { parse: true, scrapy: false },
        );

        y3 = await requestGot(
          `${urls.BASE_YOUTUBE}UCA8Vj7nN8bzT3rsukD2ypUg&part=snippet,id&order=date&maxResults=25`,
          { parse: true, scrapy: false },
        );
      }
    } catch (err) {
      return next(err);
    }

    if (data && !y1 && !y2 && !y3) {
      const results: any[] = data.items.map((item: any) => ({
        title: item.snippet.title,
        videoId: item.id.videoId,
        thumbDefault: item.snippet.thumbnails.default.url,
        thumbMedium: item.snippet.thumbnails.medium.url,
        thumbHigh: item.snippet.thumbnails.high.url,
      }));

      res.status(200).json({ videos: results });
    } else if (!data && y1 && y2 && !y3) {
      const results: any[] = y1.items.concat(y2.items).map((item: any) => ({
        title: item.snippet.title,
        videoId: item.id.videoId,
        thumbDefault: item.snippet.thumbnails.default.url,
        thumbMedium: item.snippet.thumbnails.medium.url,
        thumbHigh: item.snippet.thumbnails.high.url,
      }));

      res.status(200).json({ videos: results });
    } else if (!data && y1 && y2 && y3) {
      const results: any[] = y1.items
        .concat(y2.items.concat(y3.items))
        .map((item: any) => ({
          title: item.snippet.title,
          videoId: item.id.videoId,
          thumbDefault: item.snippet.thumbnails.default.url,
          thumbMedium: item.snippet.thumbnails.medium.url,
          thumbHigh: item.snippet.thumbnails.high.url,
        }));

      res.status(200).json({ videos: results });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getRadioStations(req: Request, res: Response, next: NextFunction) {
    let data: RadioStation[];

    try {
      data = await RadioStationModel.find();
    } catch (err) {
      return next(err);
    }

    const results: any[] = data.map((item: RadioStation) => {
      return {
        name: item.name,
        url: item.url,
      };
    });

    if (results.length > 0) {
      res.status(200).json({ stations: results });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getAllThemes(req: Request, res: Response, next: NextFunction) {
    let data: Theme[];

    try {
      data = await ThemeModel.find();
    } catch (err) {
      return next(err);
    }

    const results: any[] = data.map((item: Theme) => {
      return {
        id: item.id,
        title: item.title,
        year: item.year,
        themes: item.themes,
      };
    });

    if (results.length > 0) {
      res.status(200).json({ themes: results });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getOpAndEd(req: Request, res: Response, next: NextFunction) {
    const { title } = req.params;
    let themes: any;

    try {
      themes = await structureThemes(await themeParser.serie(title), true);
    } catch (err) {
      return next(err);
    }

    if (themes) {
      res.status(200).json({ themes });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getThemesYear(req: Request, res: Response, next: NextFunction) {
    const { year } = req.params;
    let themes: any;

    try {
      if (year === undefined) {
        themes = await themeParser.allYears();
      } else {
        themes = await structureThemes(await themeParser.year(year), false);
      }
    } catch (err) {
      return next(err);
    }

    if (themes.length > 0) {
      res.status(200).json({ themes });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async randomTheme(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      data = await requestGot(`${urls.BASE_THEMEMOE}roulette`, {
        parse: true,
        scrapy: false,
      });
    } catch (err) {
      return next(err);
    }

    const random: any[] = getThemes(data.themes);

    if (random.length > 0) {
      res.set('Cache-Control', 'no-store');
      res.status(200).json({ random });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getArtist(req: Request, res: Response, next: NextFunction) {
    const { id } = req.params;
    let artists: any;

    try {
      if (id === undefined) {
        artists = await themeParser.artists();
      } else {
        artists = await structureThemes(await themeParser.artist(id), false);
      }
    } catch (err) {
      return next(err);
    }

    if (artists.length > 0) {
      res.status(200).json({ artists });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getDestAnimePlatforms(req: Request, res: Response, next: NextFunction) {
    let data: any;

    try {
      data = await requestGot(
        `${urls.BASE_ARUPPI}res/documents/animelegal/top.json`,
        { parse: true, scrapy: false },
      );
    } catch (err) {
      return next(err);
    }

    const destPlatforms: any[] = data.map((item: any) => {
      return {
        id: item.id,
        name: item.name,
        logo: item.logo,
        link: item.link,
      };
    });

    if (destPlatforms.length > 0) {
      res.status(200).json({ destPlatforms });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getPlatforms(req: Request, res: Response, next: NextFunction) {
    const { id } = req.params;
    let data: any;

    try {
      if (id === undefined) {
        data = await requestGot(
          `${urls.BASE_ARUPPI}res/documents/animelegal/typeplatforms.json`,
          { parse: true, scrapy: false },
        );
      } else if (
        id === 'producers' ||
        id === 'apps' ||
        id === 'publishers' ||
        'events'
      ) {
        data = await requestGot(
          `${urls.BASE_ARUPPI}res/documents/animelegal/type/${id}.json`,
          { parse: true, scrapy: false },
        );
      } else {
        data = await requestGot(
          `${urls.BASE_ARUPPI}res/documents/animelegal/type/${id}.json`,
          { parse: true, scrapy: false },
        );
      }
    } catch (err) {
      return next(err);
    }

    const platforms: any[] = data.map((item: any) => {
      if (id === undefined) {
        return {
          id: item.id,
          name: item.name,
          comming: item.comming || false,
          cover: item.cover,
        };
      } else if (
        id === 'producers' ||
        id === 'apps' ||
        id === 'publishers' ||
        'events'
      ) {
        return {
          id: item.id,
          name: item.name,
          logo: item.logo,
          cover: item.cover,
          description: item.description,
          type: item.type,
          moreInfo: item.moreInfo,
          facebook: item.facebook,
          twitter: item.twitter,
          instagram: item.instagram,
          webInfo: item.webInfo,
          webpage: item.webpage,
        };
      } else {
        return {
          id: item.id,
          name: item.name,
          type: item.type,
          logo: item.logo,
          cover: item.cover,
          webpage: item.webpage,
        };
      }
    });

    if (platforms.length > 0) {
      res.status(200).json({ platforms });
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }

  async getWaifuRandom(req: Request, res: Response, next: NextFunction) {
    let waifuQuery: Waifu[] | null;
    let waifuResult: any;

    try {
      waifuQuery = await WaifuModel.aggregate([{ $sample: { size: 1 } }]);
    } catch (err) {
      return next(err);
    }

    if (waifuQuery.length > 0) {
      waifuResult = {
        id: waifuQuery[0].id,
        name: waifuQuery[0].name,
        weight: waifuQuery[0].weight,
        series: waifuQuery[0].series,
        height: waifuQuery[0].height,
        birthday: waifuQuery[0].birthday,
        likes: waifuQuery[0].likes,
        trash: waifuQuery[0].trash,
        blood_type: waifuQuery[0].blood_type,
        hip: waifuQuery[0].hip,
        bust: waifuQuery[0].bust,
        description: waifuQuery[0].description,
        display_picture: waifuQuery[0].display_picture,
        waist: waifuQuery[0].waist,
      };
    }

    if (waifuResult) {
      res.set('Cache-Control', 'no-store');
      res.status(200).json(waifuResult);
    } else {
      res.status(500).json({ message: 'Aruppi lost in the shell' });
    }
  }
}
