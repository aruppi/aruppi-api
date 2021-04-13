import urls from './urls';
import { requestGot } from './requestCall';
import AnimeModel, { Anime } from '../database/models/anime.model';
import crypto from 'crypto';
import util from 'util';
import { redisClient } from '../database/connection';

// @ts-ignore
redisClient.get = util.promisify(redisClient.get);

/*
  Utils fuctions - functions to get information
  from the pages as a parsed JSON or just scrapping
  the information from the page.
*/

interface Promo {
  title: string;
  image_url: string;
  video_url: string;
}

interface Character {
  id: number;
  name: string;
  image: string;
  role: string;
}

interface RelatedAnime {
  title: string;
  type: string;
  poster: string;
}

export const animeExtraInfo = async (mal_id: number) => {
  let data: any;
  let broadcast: any;

  const airDay: any = {
    mondays: 'Lunes',
    monday: 'Lunes',
    tuesdays: 'Martes',
    tuesday: 'Martes',
    wednesdays: 'Miércoles',
    wednesday: 'Miércoles',
    thursdays: 'Jueves',
    thursday: 'Jueves',
    fridays: 'Viernes',
    friday: 'Viernes',
    saturdays: 'Sábados',
    saturday: 'Sábados',
    sundays: 'Domingos',
    sunday: 'Domingos',
    default: 'Sin emisión',
  };

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `extraInfo_${hashStringMd5(`${mal_id}`)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    data = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}`, {
      parse: true,
      scrapy: false,
    });

    broadcast = data.broadcast.split('at')[0].trim().toLowerCase() || null;
  } catch (err) {
    return err;
  }

  if (airDay.hasOwnProperty(broadcast)) {
    data.broadcast = airDay[broadcast];
  } else {
    data.broadcast = null;
  }

  const formattedObject: any = {
    titleJapanese: data.title_japanese,
    source: data.source,
    totalEpisodes: data.episodes,
    aired: {
      from: data.aired.from,
      to: data.aired.to,
    },
    duration: data.duration.split('per')[0],
    rank: data.rank,
    broadcast: data.broadcast,
    producers: data.producers.map((item: any) => item.name) || null,
    licensors: data.licensors.map((item: any) => item.name) || null,
    studios: data.studios.map((item: any) => item.name) || null,
    openingThemes: data.opening_themes || null,
    endingThemes: data.ending_themes || null,
  };

  if (formattedObject) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `extraInfo_${hashStringMd5(`${mal_id}`)}`,
        JSON.stringify(formattedObject),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `extraInfo_${hashStringMd5(`${mal_id}`)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return formattedObject;
  } else {
    return null;
  }
};

export const getAnimeVideoPromo = async (mal_id: number) => {
  let data: any;

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `promoInfo_${hashStringMd5(`${mal_id}`)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    data = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}/videos`, {
      parse: true,
      scrapy: false,
    });
  } catch (err) {
    return err;
  }

  const promo: Promo[] = data.promo.map((item: Promo) => {
    return {
      title: item.title,
      previewImage: item.image_url,
      videoURL: item.video_url,
    };
  });

  if (promo.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `promoInfo_${hashStringMd5(`${mal_id}`)}`,
        JSON.stringify(promo),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `promoInfo_${hashStringMd5(`${mal_id}`)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return promo;
  } else {
    return null;
  }
};

export const getAnimeCharacters = async (mal_id: number) => {
  let data: any;

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `charactersInfo_${hashStringMd5(`${mal_id}`)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    data = await requestGot(
      `${urls.BASE_JIKAN}anime/${mal_id}/characters_staff`,
      { parse: true, scrapy: false },
    );
  } catch (err) {
    return err;
  }

  const characters: Character[] = data.characters.map((item: any) => {
    return {
      id: item.mal_id,
      name: item.name,
      image: item.image_url,
      role: item.role,
    };
  });

  if (characters.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `charactersInfo_${hashStringMd5(`${mal_id}`)}`,
        JSON.stringify(characters),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `charactersInfo_${hashStringMd5(`${mal_id}`)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return characters;
  } else {
    return null;
  }
};

const getPosterAndType = async (
  id: string | undefined,
  mal_id: number | undefined,
) => {
  if (id) {
    const queryRes: Anime | null = await AnimeModel.findOne({
      id: { $eq: id },
    });

    return [queryRes?.poster, queryRes?.type];
  }

  if (mal_id) {
    const queryRes: Anime | null = await AnimeModel.findOne({
      mal_id: { $eq: mal_id },
    });

    return [queryRes?.poster, queryRes?.type];
  }

  return '';
};

export const getRelatedAnimesFLV = async (id: string) => {
  let $: cheerio.Root;

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `relatedFLV_${hashStringMd5(id)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_ANIMEFLV}/anime/${id}`, {
      parse: false,
      scrapy: true,
    });
  } catch (err) {
    return err;
  }

  let listRelated: any = {};
  let relatedAnimes: RelatedAnime[] = [];

  $('ul.ListAnmRel li a').each((index: number, element: any) => {
    listRelated[$(element).text()] = $(element).attr('href');
  });

  for (const related in listRelated) {
    let posterUrl: any = await getPosterAndType(
      listRelated[related].split('/')[2],
      undefined,
    );

    if (posterUrl !== '') {
      relatedAnimes.push({
        title: related,
        type: posterUrl[1],
        poster: posterUrl[0],
      });
    }
  }

  if (relatedAnimes.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `relatedFLV_${hashStringMd5(id)}`,
        JSON.stringify(relatedAnimes),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `relatedFLV_${hashStringMd5(id)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return relatedAnimes;
  } else {
    return [];
  }
};

export const getRelatedAnimesMAL = async (mal_id: number) => {
  let $: cheerio.Root;

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `getRelatedMAL_${hashStringMd5(`${mal_id}`)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`https://myanimelist.net/anime/${mal_id}`, {
      parse: false,
      scrapy: true,
    });
  } catch (err) {
    return err;
  }

  let listRelated: any = {};
  let relatedAnimes: RelatedAnime[] = [];

  if ($('table.anime_detail_related_anime').length > 0) {
    $('table.anime_detail_related_anime')
      .find('tbody tr')
      .each((index: number, element: any) => {
        if ($(element).find('td').eq(0).text() !== 'Adaptation:') {
          listRelated[$(element).find('td').eq(1).text()] = $(element)
            .find('td')
            .children('a')
            .attr('href');
        }
      });

    for (const related in listRelated) {
      let posterUrl: any = await getPosterAndType(
        undefined,
        listRelated[related].split('/')[2],
      );

      if (posterUrl !== '') {
        relatedAnimes.push({
          title: related,
          type: posterUrl[1],
          poster: posterUrl[0],
        });
      }
    }

    if (relatedAnimes.length > 0) {
      if (redisClient.connected) {
        /* Set the key in the redis cache. */

        redisClient.set(
          `getRelatedMAL_${hashStringMd5(`${mal_id}`)}`,
          JSON.stringify(relatedAnimes),
        );

        /* After 24hrs expire the key. */

        redisClient.expireat(
          `getRelatedMAL_${hashStringMd5(`${mal_id}`)}`,
          parseInt(`${+new Date() / 1000}`, 10) + 7200,
        );
      }

      return relatedAnimes;
    }
  } else {
    return [];
  }
};

export const animeFlvInfo = async (id: string | undefined) => {
  let $: cheerio.Root;
  let anime_info: string[] = [];
  let anime_eps: string[] = [];
  let nextEpisodeDate: string | null;
  let episodes: any[] = [];

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `animeflvInfo_${hashStringMd5(id!)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_ANIMEFLV}/anime/${id}`, {
      scrapy: true,
      parse: false,
    });
  } catch (err) {
    return err;
  }

  const scripts: cheerio.Element[] = $('script').toArray();

  for (const script of scripts) {
    if ($(script).html()!.includes('anime_info')) {
      anime_info = JSON.parse(
        $(script).html()!.split('var anime_info = ')[1].split(';\n')[0],
      );

      anime_eps = JSON.parse(
        $(script).html()!.split('var episodes = ')[1].split(';')[0],
      );
    }
  }

  if (anime_info.length === 4) {
    nextEpisodeDate = anime_info[3];
  } else {
    nextEpisodeDate = null;
  }

  episodes.push({ nextEpisodeDate });

  for (const episode of anime_eps) {
    episodes.push({
      episode: episode[0],
      id: `${episode[1]}/${id}-${episode[0]}`,
    });
  }

  if (episodes.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `animeflvInfo_${hashStringMd5(id!)}`,
        JSON.stringify(episodes),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `animeflvInfo_${hashStringMd5(id!)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return episodes;
  } else {
    return null;
  }
};

export const jkanimeInfo = async (id: string | undefined) => {
  let $: cheerio.Root;
  let nextEpisodeDate: string | null;
  let imageLink: string | undefined;
  let episodesList: any[] = [];
  let countEpisodes: string[] = [];

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `jkanimeInfo_${hashStringMd5(id!)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_JKANIME}${id}`, {
      scrapy: true,
      parse: false,
    });
  } catch (err) {
    return err;
  }

  countEpisodes = $('div.anime__pagination a')
    .map((index: number, element: cheerio.Element) => {
      return $(element).text();
    })
    .get();

  const episodesCount: string = countEpisodes[countEpisodes.length - 1].split(
    '-',
  )[1];

  nextEpisodeDate = $('div.proxep p').text() || null;

  episodesList.push({ nextEpisodeDate });

  for (let i = 1; i <= parseInt(episodesCount); i++) {
    episodesList.push({
      episode: i,
      id: `${id}/${i}`,
    });
  }

  if (episodesList.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `jkanimeInfo_${hashStringMd5(id!)}`,
        JSON.stringify(episodesList),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `jkanimeInfo_${hashStringMd5(id!)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return episodesList;
  } else {
    return null;
  }
};

export const monoschinosInfo = async (
  id: string | undefined,
  mal_id: number,
) => {
  let $: cheerio.Root;
  let episodeList: any[] = [];
  let extraInfo: any;

  try {
    /* Extra info of the anime */
    extraInfo = await animeExtraInfo(mal_id);

    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `monoschinosInfo_${hashStringMd5(id!)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_MONOSCHINOS}anime/${id}`, {
      scrapy: true,
      parse: false,
    });
  } catch (err) {
    return err;
  }

  let broadCastDate = new Date();
  let dd: number, mm: string | number, yyyy: number;

  const airDay: any = {
    Lunes: 1,
    Martes: 2,
    Miércoles: 3,
    Jueves: 4,
    Viernes: 5,
    Sábados: 6,
    Domingos: 7,
    'Sin emisión': 'default',
  };

  if (!extraInfo.aired.to) {
    if (airDay.hasOwnProperty(extraInfo.broadcast)) {
      if (broadCastDate.getDay() < airDay[extraInfo.broadcast]) {
        for (
          let i = broadCastDate.getDay();
          i < airDay[extraInfo.broadcast];
          i++
        ) {
          broadCastDate.setDate(broadCastDate.getDate() + 1);
        }
      } else {
        let counter = broadCastDate.getDay() + 1;

        /* Adding one because of the day */
        broadCastDate.setDate(broadCastDate.getDate() + 1);

        while (counter !== airDay[extraInfo.broadcast]) {
          if (counter === 7) {
            counter = 0;
          }
          broadCastDate.setDate(broadCastDate.getDate() + 1);
          counter++;
        }
      }

      dd = broadCastDate.getDate();
      mm =
        broadCastDate.getMonth() + 1 < 10
          ? `0${broadCastDate.getMonth() + 1}`
          : broadCastDate.getMonth() + 1;
      yyyy = broadCastDate.getFullYear();

      episodeList.push({
        nextEpisodeDate: `${yyyy}/${mm}/${dd}`,
      });
    }
  }

  $('.SerieCaps a').each((index: number, element: cheerio.Element) => {
    let episode: number;

    $(element)
      .attr('href')
      ?.split('-')
      .forEach((item: any) => {
        if (!isNaN(item)) {
          episode = parseInt(item);
        }
      });

    episodeList.push({
      episode: episode!,
      id: `${$(element).attr('href')?.split('/')[3]}/${
        $(element).attr('href')?.split('/')[4]
      }`,
    });
  });

  if (episodeList.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `monoschinosInfo_${hashStringMd5(id!)}`,
        JSON.stringify(episodeList),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `monoschinosInfo_${hashStringMd5(id!)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return episodeList;
  } else {
    return null;
  }
};

export const videoServersMonosChinos = async (id: string) => {
  let $: cheerio.Root;
  let videoServers: any[] = [];

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `videoServersMonosChinos_${hashStringMd5(id)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_MONOSCHINOS}${id}`, {
      scrapy: true,
      parse: false,
    });
  } catch (err) {
    return err;
  }

  let videoNames: string[] = $('.TPlayerNv li')
    .map((index: number, element: cheerio.Element) => {
      return $(element).attr('title');
    })
    .get();

  videoServers.push({
    id: videoNames[0].toLowerCase(),
    url: decodeURIComponent(
      $('.TPlayer div iframe').attr('src')?.split('url=')[1]!,
    ).split('&id')[0],
    direct: false,
  });

  const videoContainer: any = $('.TPlayer div').text();

  $(videoContainer).each((index: number, element: cheerio.Element) => {
    let video: any = $(element).attr('src');

    if (video) {
      video = video.split('url=')[1];
      video = decodeURIComponent(video);
      video = video.split('&id')[0];
    }

    if (video) {
      videoNames.forEach((value: string) => {
        if (video.includes(value.toLowerCase())) {
          videoServers.push({
            id: value.toLowerCase(),
            url: video,
            direct: false,
          });
        }
      });
    }
  });

  if (videoServers.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `videoServersMonosChinos_${hashStringMd5(id)}`,
        JSON.stringify(videoServers),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `videoServersMonosChinos_${hashStringMd5(id)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return videoServers;
  } else {
    return null;
  }
};

export const videoServersJK = async (id: string) => {
  let $: cheerio.Root;
  let servers: any = {};
  let script: string | null = '';

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `videoServersJK_${hashStringMd5(id)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(`${urls.BASE_JKANIME}${id}`, {
      scrapy: true,
      parse: false,
    });
  } catch (err) {
    return err;
  }

  const serverNames: string[] = $('div.bg-servers a')
    .map((index: number, element: cheerio.Element) => {
      return $(element).text();
    })
    .get();

  console.log(serverNames);

  $('script').each((index: number, element: cheerio.Element) => {
    if ($(element).html()!.includes('var video = [];')) {
      script = $(element).html();
    }
  });

  try {
    let videoUrls = script.match(/(?<=src=").*?(?=[\*"])/gi);

    for (let i = 0; i < serverNames.length; i++) {
      servers[serverNames[i]] = videoUrls![i];
    }
  } catch (err) {
    return null;
  }

  let serverList = [];

  for (let server in servers) {
    if (serverNames[serverNames.indexOf(server)].toLowerCase() === 'desu') {
      serverList.push({
        id: serverNames[serverNames.indexOf(server)].toLowerCase(),
        url:
          (await desuServerUrl(servers[server])) !== null
            ? await desuServerUrl(servers[server])
            : servers[server],
        direct: false,
      });
    } else {
      serverList.push({
        id: serverNames[serverNames.indexOf(server)].toLowerCase(),
        url: servers[server],
        direct: false,
      });
    }
  }

  serverList = serverList.filter(x => x.id !== 'xtreme s' && x.id !== 'desuka');

  if (serverList.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `videoServersJK_${hashStringMd5(id!)}`,
        JSON.stringify(serverList),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `videoServersJK_${hashStringMd5(id!)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return serverList;
  } else {
    return null;
  }
};

async function desuServerUrl(url: string) {
  let $: cheerio.Root;

  try {
    if (redisClient.connected) {
      const resultQueryRedis: any = await redisClient.get(
        `desuServerUrl_${hashStringMd5(url)}`,
      );

      if (resultQueryRedis) {
        const resultRedis: any = JSON.parse(resultQueryRedis);

        return resultRedis;
      }
    }

    $ = await requestGot(url, { scrapy: true, parse: false });
  } catch (err) {
    return err;
  }

  let script: string | null = '';

  $('script').each((index: number, element: cheerio.Element) => {
    if ($(element).html()!.includes('var parts = {')) {
      if ($(element).html()) {
        script = $(element).html();
      } else {
        return null;
      }
    }
  });

  let result = script
    .match(/swarmId: '(https:\/\/\S+)'/gi)!
    .toString()
    .split("'")[1];

  if (result.length > 0) {
    if (redisClient.connected) {
      /* Set the key in the redis cache. */

      redisClient.set(
        `desuServerUrl_${hashStringMd5(url)}`,
        JSON.stringify(result),
      );

      /* After 24hrs expire the key. */

      redisClient.expireat(
        `desuServerUrl_${hashStringMd5(url)}`,
        parseInt(`${+new Date() / 1000}`, 10) + 7200,
      );
    }

    return result;
  } else {
    return null;
  }
}

export const structureThemes = async (body: any, indv: boolean) => {
  let themes: any[] = [];

  if (indv === true) {
    return {
      title: body.title,
      year: body.year,
      themes: await getThemesData(body.themes),
    };
  } else {
    for (let i = 0; i <= body.length - 1; i++) {
      themes.push({
        title: body[i].title,
        year: body[i].year,
        themes: await getThemesData(body[i].themes),
      });
    }

    return themes;
  }
};

function getThemesData(themes: any[]): any {
  let items: any[] = [];

  for (let i = 0; i <= themes.length - 1; i++) {
    items.push({
      title: themes[i].name.split('"')[1] || 'Remasterización',
      type: themes[i].type,
      episodes: themes[i].episodes !== '' ? themes[i].episodes : null,
      notes: themes[i].notes !== '' ? themes[i].notes : null,
      video: themes[i].link,
    });
  }

  return items.filter(x => x.title !== 'Remasterización');
}

export function getThemes(themes: any[]) {
  return themes.map((item: any) => ({
    name: item.themeName,
    title: item.themeType,
    link: item.mirror.mirrorURL,
  }));
}

export const imageUrlToBase64 = async (url: string) => {
  let img: any = await requestGot(url);
  return img.rawBody.toString('base64');
};

export function hashStringMd5(string: string) {
  return crypto.createHash('md5').update(string).digest('hex');
}
