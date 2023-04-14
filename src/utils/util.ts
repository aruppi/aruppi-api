import urls from './urls';
import {requestGot} from './requestCall';
import AnimeModel, {Anime} from '../database/models/anime.model';
import crypto from 'crypto';
import util from 'util';
import {redisClient} from '../database/connection';
import { stderr } from 'process';

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
    let info: any;
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

        info = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}`, {
            parse: true,
            scrapy: false,
        });

        if (info.data.airing) {
            broadcast = info.data.broadcast.string.split('at')[0].trim().toLowerCase() || null;
        }
    } catch (err) {
        return err;
    }

    if (airDay.hasOwnProperty(broadcast)) {
        info.data.broadcast = airDay[broadcast];
    } else {
        info.data.broadcast = null;
    }

    const formattedObject: any = {
        titleJapanese: info.data.titles.find((x: { type: string; }) => x.type === "Default").title,
        source: info.data.source,
        totalEpisodes: info.data.episodes,
        aired: {
            from: info.data.aired.from,
            to: info.data.aired.to,
        },
        duration: info.data.duration.split('per')[0],
        rank: info.data.rank,
        broadcast: info.data.broadcast,
        producers: info.data.producers.map((item: any) => item.name) || null,
        licensors: info.data.licensors.map((item: any) => item.name) || null,
        studios: info.data.studios.map((item: any) => item.name) || null,
        openingThemes: info.data.opening_themes || null,
        endingThemes: info.data.ending_themes || null,
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
    let info: any;

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

        info = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}/videos`, {
            parse: true,
            scrapy: false,
        });
    } catch (err) {
        return err;
    }

    const promo: Promo[] = info.data.promo.map((item: any) => {
        return {
            title: item.title,
            previewImage: item.trailer.images.image_url,
            videoURL: item.trailer.url,
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
    let info: any;

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

        info = await requestGot(
            `${urls.BASE_JIKAN}anime/${mal_id}/characters`,
            {parse: true, scrapy: false},
        );
    } catch (err) {
        return err;
    }

    const characters: Character[] = info.data.map((item: any) => {
        return {
            id: item.character.mal_id,
            name: item.character.name,
            image: item.character.images.jpg.image_url,
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
            id: {$eq: id},
        });

        return [queryRes?.poster, queryRes?.type];
    }

    if (mal_id) {
        const queryRes: Anime | null = await AnimeModel.findOne({
            mal_id: {$eq: mal_id},
        });

        return [queryRes?.poster, queryRes?.type];
    }

    return '';
};

export const getRelatedAnimesMAL = async (mal_id: number) => {
    let info: any;

    try {
        if (redisClient.connected) {
            const resultQueryRedis: any = await redisClient.get(
                `getRelatedMAL_${mal_id}`,
            );

            if (resultQueryRedis) {
                const resultRedis: any = JSON.parse(resultQueryRedis);

                return resultRedis;
            }
        }

        info = await requestGot(`${urls.BASE_JIKAN}anime/${mal_id}/relations`, {
            parse: true,
            scrapy: false,
        });
    } catch (err) {
        stderr.write(`Error on getRelatedAnimesMAL http on mal_id: ${mal_id}\n`)
        return err;
    }

    const relatedAnimes = []
    for (const relation_entry of info.data) {
        for (const entry of relation_entry.entry){
            if (entry.type != "anime")
                break;
            const queryRes: Anime | null = await AnimeModel.findOne({
                mal_id: {$eq: entry.mal_id}
            });
            if (queryRes == null)
                break
            
            relatedAnimes.push({
                title:  queryRes!.title,
                type:   queryRes?.type,
                poster: queryRes?.poster
            })
        }
    }

    if (relatedAnimes.length > 0) {
        if (redisClient.connected) {
            /* Set the key in the redis cache. */

            redisClient.set(
                `getRelatedMAL_${mal_id}`,
                JSON.stringify(relatedAnimes),
            );

            /* After 1hr expire the key. */

            redisClient.expire(
                `getRelatedMAL_${mal_id}`,
                3600,
            );
        }

        return relatedAnimes;
    }
};

export const jkanimeInfo = async (id: string | undefined, mal_id: number) => {
    let $: cheerio.Root;
    let extraInfo: any;
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

        /* Extra info of the anime */
        extraInfo = (await animeExtraInfo(mal_id)) || undefined;
    } catch (err) {
        console.log(err);
    }

    if ($!) {
        countEpisodes = $!('div.anime__pagination a')
            .map((index: number, element: cheerio.Element) => {
                return $!(element).text();
            })
            .get();

        const episodesCount: string = countEpisodes[countEpisodes.length - 1].split(
            '-',
        )[1];

        if (extraInfo) {
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

                    episodesList.push({
                        nextEpisodeDate: `${yyyy}-${mm}-${dd}`,
                    });
                }
            }
        }

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
            return undefined;
        }
    } else {
        return undefined;
    }
};

export const monoschinosInfo = async (
    id: string | undefined,
    mal_id: number,
) => {
    let info;
    let episodeList: any[] = [];
    let extraInfo: any;

    try {
        if (redisClient.connected) {
            const resultQueryRedis: any = await redisClient.get(
                `monoschinosInfo_${hashStringMd5(id!)}`,
            );

            if (resultQueryRedis) {
                const resultRedis: any = JSON.parse(resultQueryRedis);

                return resultRedis;
            }
        }

        info = await requestGot(`${urls.BASE_ARUPPI_MONOSCHINOS}anime/${id}`, {
            scrapy: false,
            parse: true,
        });

        /* Extra info of the anime */
        extraInfo = (await animeExtraInfo(mal_id)) || undefined;
    } catch (err) {
        console.log(err);
    }

    if (extraInfo) {
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
                    nextEpisodeDate: `${yyyy}-${mm}-${dd}`,
                });
            }
        }
    }

    for (const anime of info.episodes) {
        episodeList.push({
            episode: anime.no,
            id: `ver/${anime.id}`,
        });
    }

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
        return undefined;
    }

};

export const tioanimeInfo = async (id: string | undefined, mal_id: number) => {
    let $: cheerio.Root;
    let episodesList: any[] = [];
    let anime_eps: string[] = [];
    let extraInfo: any;

    try {
        if (redisClient.connected) {
            const resultQueryRedis: any = await redisClient.get(
                `tioanimeInfo_${hashStringMd5(id!)}`,
            );

            if (resultQueryRedis) {
                const resultRedis: any = JSON.parse(resultQueryRedis);

                return resultRedis;
            }
        }

        $ = await requestGot(`${urls.BASE_TIOANIME}anime/${id}`, {
            scrapy: true,
            parse: false,
            spoof: true,
        });

        /* Extra info of the anime */
        extraInfo = (await animeExtraInfo(mal_id)) || undefined;
    } catch (err) {
        console.log(err);
    }

    if ($!) {
        if (extraInfo) {
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

                    episodesList.push({
                        nextEpisodeDate: `${yyyy}-${mm}-${dd}`,
                    });
                }
            }
        }

        const scripts: cheerio.Element[] = $!('script').toArray();

        for (const script of scripts) {
            if ($!(script).html()!.includes('anime_info')) {
                anime_eps = JSON.parse(
                    $!(script).html()!.split('var episodes = ')[1].split(';')[0],
                );
            }
        }

        for (const episode of anime_eps) {
            episodesList.push({
                episode: episode,
                id: `ver/${id}-${episode}`,
            });
        }

        if (redisClient.connected) {
            /* Set the key in the redis cache. */

            redisClient.set(
                `tioanimeInfo_${hashStringMd5(id!)}`,
                JSON.stringify(episodesList),
            );

            /* After 24hrs expire the key. */

            redisClient.expireat(
                `tioanimeInfo_${hashStringMd5(id!)}`,
                parseInt(`${+new Date() / 1000}`, 10) + 7200,
            );
        }

        return episodesList;
    } else {
        return undefined;
    }
};

export const videoServersMonosChinos = async (id: string) => {
    let $;
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

        $ = await requestGot(`${urls.BASE_ARUPPI_MONOSCHINOS}${id}`, {
            scrapy: false,
            parse: true,
        });
    } catch (err) {
        return err;
    }

    for (const server of $.videos) {
        videoServers.push({
            id: server.title,
            url: server.url.replace("https://monoschinos2.com/reproductor?url=", ""),
            direct: false,
        });
    }

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

export const videoServersTioAnime = async (id: string) => {
    let $: cheerio.Root;
    let servers: any;
    let videoServers: any[] = [];

    try {
        if (redisClient.connected) {
            const resultQueryRedis: any = await redisClient.get(
                `videoServersTioAnime_${hashStringMd5(id)}`,
            );

            if (resultQueryRedis) {
                const resultRedis: any = JSON.parse(resultQueryRedis);

                return resultRedis;
            }
        }

        $ = await requestGot(`${urls.BASE_TIOANIME}${id}`, {
            scrapy: true,
            parse: false,
            spoof: true,
        });
    } catch (err) {
        return err;
    }

    const scripts: cheerio.Element[] = $('script').toArray();

    for (const script of scripts) {
        if ($(script).html()!.includes('var videos =')) {
            servers = JSON.parse(
                $(script).html()!.split('var videos = ')[1].split(';')[0],
            );
        }
    }

    for (const server of servers) {
        videoServers.push({
            id: server[0].toLowerCase(),
            url: server[1],
            direct: false,
        });
    }

    if (videoServers.length > 0) {
        if (redisClient.connected) {
            /* Set the key in the redis cache. */

            redisClient.set(
                `videoServersTioAnime_${hashStringMd5(id)}`,
                JSON.stringify(videoServers),
            );

            /* After 24hrs expire the key. */

            redisClient.expireat(
                `videoServersTioAnime_${hashStringMd5(id)}`,
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

        $ = await requestGot(url, {scrapy: true, parse: false, spoof: true});
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
    let img: any = await requestGot(url,{spoof:false});
    return img.rawBody.toString('base64');
};

export function hashStringMd5(string: string) {
    return crypto.createHash('md5').update(string).digest('hex');
}
