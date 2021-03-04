import urls from './urls';
import { requestGot } from './requestCall';
import AnimeModel, { Anime } from '../database/models/anime.model';

/*
  Utils fuctions - functions to get information
  from the pages as a parsed JSON or just scrapping
  the information from the page.
*/

interface Promo {
  title: string;
  previewImage: string;
  videoUrl: string;
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
    data = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}`, {
      parse: true,
      scrapy: false,
    });
  } catch (err) {
    return err;
  }

  if (
    airDay.hasOwnProperty(data.broadcast.split('at')[0].trim().toLowerCase())
  ) {
    data.broadcast = airDay[data.broadcast.split('at')[0].trim().toLowerCase()];
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

  return formattedObject;
};

export const getAnimeVideoPromo = async (mal_id: number) => {
  let data: any;

  try {
    data = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}/videos`, {
      parse: true,
      scrapy: false,
    });
  } catch (err) {
    return err;
  }

  const promo: Promo[] = data.promo.map((item: Promo) => item);

  return promo;
};

export const getAnimeCharacters = async (mal_id: number) => {
  let data: any;

  try {
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

  return characters;
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
  const $ = await requestGot(`${urls.BASE_ANIMEFLV}/anime/${id}`, {
    parse: false,
    scrapy: true,
  });
  let listRelated: any = {};
  let relatedAnimes: RelatedAnime[] = [];

  if ($('ul.ListAnmRel').length) {
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

    return relatedAnimes;
  } else {
    return [];
  }
};

export const getRelatedAnimesMAL = async (mal_id: number) => {
  const $ = await requestGot(`https://myanimelist.net/anime/${mal_id}`, {
    parse: false,
    scrapy: true,
  });

  let listRelated: any = {};
  let relatedAnimes: RelatedAnime[] = [];

  if ($('table.anime_detail_related_anime').length > 0) {
    $('table.anime_detail_related_anime')
      .find('tbody tr')
      .each((index: number, element: any) => {
        if ($(element).find('td').eq(0) !== 'Adaptation:') {
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

    return relatedAnimes;
  } else {
    return [];
  }
};
