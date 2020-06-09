const cloudscraper = require('cloudscraper')
const cheerio = require('cheerio');
const base64 = require('node-base64-image');

const {
    BASE_ANIMEFLV, BASE_JIKAN, BASE_EPISODE_IMG_URL, SEARCH_URL, BASE_ARUPPI, BASE_THEMEMOE
} = require('../api/urls');

const animeflvInfo = async (id, index) => {
    
    let poster = ""
    let banner = ""
    let synopsis = ""
    let rating = ""
    let debut = ""
    let type = ""

    do {

        try {

            const res = await cloudscraper(`${BASE_ANIMEFLV}anime/${id}`);
            const body = await res;
            const $ = cheerio.load(body);
            const scripts = $('script');
            const anime_info_ids = [];
            const anime_eps_data = [];
            const animeExtraInfo = [];
            const genres = [];
            let listByEps;

            poster = `${BASE_ANIMEFLV}` + $('body div div div div div aside div.AnimeCover div.Image figure img').attr('src')
            banner = poster.replace('covers', 'banners').trim();
            synopsis = $('body div div div div div main section div.Description p').text().trim();
            rating = $('body div div div.Ficha.fchlt div.Container div.vtshr div.Votes span#votes_prmd').text();
            debut = $('body div.Wrapper div.Body div div.Container div.BX.Row.BFluid.Sp20 aside.SidebarA.BFixed p.AnmStts').text();
            type = $('body div.Wrapper div.Body div div.Ficha.fchlt div.Container span.Type').text()

            animeExtraInfo.push({
                poster: poster,
                banner: banner,
                synopsis: synopsis,
                rating: rating,
                debut: debut,
                type: type,
            })

            $('main.Main section.WdgtCn nav.Nvgnrs a').each((index, element) => {
                const $element = $(element);
                const genre = $element.attr('href').split('=')[1] || null;
                genres.push(genre);
            });


            Array.from({length: scripts.length}, (v, k) => {
                const $script = $(scripts[k]);
                const contents = $script.html();
                if ((contents || '').includes('var anime_info = [')) {
                    let anime_info = contents.split('var anime_info = ')[1].split(';\n')[0];
                    let dat_anime_info = JSON.parse(anime_info);
                    anime_info_ids.push(dat_anime_info);
                }
                if ((contents || '').includes('var episodes = [')) {
                    let episodes = contents.split('var episodes = ')[1].split(';')[0];
                    let eps_data = JSON.parse(episodes)
                    anime_eps_data.push(eps_data);
                }
            });
            const AnimeThumbnailsId = index;
            const animeId = id;
            let nextEpisodeDate

            if (anime_info_ids.length > 0) {
                if (anime_info_ids[0].length === 4) {
                    nextEpisodeDate = anime_info_ids[0][3]
                } else {
                    nextEpisodeDate = null
                }
            }

            const amimeTempList = [];
            for (const [key, value] of Object.entries(anime_eps_data)) {
                let episode = anime_eps_data[key].map(x => x[0]);
                let episodeId = anime_eps_data[key].map(x => x[1]);
                amimeTempList.push(episode, episodeId);
            }
            const animeListEps = [{nextEpisodeDate: nextEpisodeDate}];
            Array.from({length: amimeTempList[1].length}, (v, k) => {
                let data = amimeTempList.map(x => x[k]);
                let episode = data[0];
                let id = data[1];
                let imagePreview = `${BASE_EPISODE_IMG_URL}${AnimeThumbnailsId}/${episode}/th_3.jpg`
                let link = `${id}/${animeId}-${episode}`

                animeListEps.push({
                    episode: episode,
                    id: link,
                    imagePreview: imagePreview
                })
            })

            listByEps = animeListEps;

            return {listByEps, genres, animeExtraInfo};

        } catch (err) {
            console.error(err)
        }

    } while (poster === "" && banner === "" && synopsis === "" && rating === "" && debut === "" && type === "")


};

const getAnimeCharacters = async (title) => {
    const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
    const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
    const malId = matchAnime[0].mal_id;

    if (typeof matchAnime[0].mal_id === 'undefined') return null;

    const jikanCharactersURL = `${BASE_JIKAN}anime/${malId}/characters_staff`;
    const data = await cloudscraper.get(jikanCharactersURL);
    let body = JSON.parse(data).characters;

    if (typeof body === 'undefined') return null;

    const charactersId = body.map(doc => {
        return doc.mal_id
    })
    const charactersNames = body.map(doc => {
        return doc.name;
    });
    const charactersImages = body.map(doc => {
        return doc.image_url
    });

    let characters = [];
    Array.from({length: charactersNames.length}, (v, k) => {
        const id = charactersId[k];
        let name = charactersNames[k];
        let characterImg = charactersImages[k];
        characters.push({
            id: id,
            name: name,
            image: characterImg
        });
    });

    return Promise.all(characters);
};

const getAnimeVideoPromo = async (title) => {
    const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
    const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
    const malId = matchAnime[0].mal_id;

    if (typeof matchAnime[0].mal_id === 'undefined') return null;

    const jikanCharactersURL = `${BASE_JIKAN}anime/${malId}/videos`;
    const data = await cloudscraper.get(jikanCharactersURL);
    const body = JSON.parse(data).promo;
    const promises = [];

    body.map(doc => {
        promises.push({
            title: doc.title,
            previewImage: doc.image_url,
            videoURL: doc.video_url
        });
    });

    return Promise.all(promises);
};

const animeExtraInfo = async (title) => {
    const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
    const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
    const malId = matchAnime[0].mal_id;

    if (typeof matchAnime[0].mal_id === 'undefined') return null;

    const animeDetails = `${BASE_JIKAN}anime/${malId}`;
    const data = await cloudscraper.get(animeDetails);
    const body = Array(JSON.parse(data));
    const promises = [];

    body.map(doc => {

        let airDay

        switch (doc.broadcast.split('at')[0].replace(" ", "").toLowerCase()) {
            case "mondays":
                airDay = "Lunes";
                break;
            case "monday":
                airDay = "Lunes";
                break;
            case "tuesdays":
                airDay = "Martes";
                break;
            case "tuesday":
                airDay = "Martes";
                break;
            case "wednesdays":
                airDay = "Miércoles";
                break;
            case "wednesday":
                airDay = "Miércoles";
                break;
            case "thursdays":
                airDay = "Jueves";
                break;
            case "thursday":
                airDay = "Jueves";
                break;
            case "fridays":
                airDay = "Viernes";
                break;
            case "friday":
                airDay = "Viernes";
                break;
            case "saturdays":
                airDay = "Sábados";
                break;
            case "saturday":
                airDay = "Sábados";
                break;
            case "sundays":
                airDay = "Domingos";
                break;
            case "sunday":
                airDay = "Domingos";
                break;
            default:
                airDay = "Sin emisión";
        }

        promises.push({
            titleJapanese: doc.title_japanese,
            source: doc.source,
            totalEpisodes: doc.episodes,
            aired: {
                from: doc.aired.from,
                to: doc.aired.to
            },
            duration: doc.duration.split('per')[0].replace(" ", ""),
            rank: doc.rank,
            broadcast: airDay,
            producers: doc.producers.map(x => x.name) || null,
            licensors: doc.licensors.map(x => x.name) || null,
            studios: doc.studios.map(x => x.name) || null,
            openingThemes: doc.opening_themes || null,
            endingThemes: doc.ending_themes || null
        });
    });
    return Promise.all(promises);
};

const imageUrlToBase64 = async (url) => {
    return await base64.encode(url, {string: true});
};

const search = async () => {
}

const searchAnime = async (query) => {

    const res = await cloudscraper(`${SEARCH_URL}${query}`);
    const body = await res;
    const $ = cheerio.load(body);
    const promises = [];

    $('div.Container ul.ListAnimes li article').each((index, element) => {
        const $element = $(element);
        const id = $element.find('div.Description a.Button').attr('href').slice(1);
        const title = $element.find('a h3').text();
        let poster = $element.find('a div.Image figure img').attr('src') || $element.find('a div.Image figure img').attr('data-cfsrc');
        const type = $element.find('div.Description p span.Type').text();

        promises.push(search().then(async extra => ({
            id: id || null,
            title: title || null,
            type: type || null,
            image: await imageUrlToBase64(poster) || null
        })));

    })

    return Promise.all(promises);

};

const transformUrlServer = async (urlReal) => {

    let res
    let body
    const promises = []

    for (i = 0; i <= urlReal.length - 1; i++) {
        switch (urlReal[i].server) {
            case "amus":
                res = await cloudscraper(urlReal[i].code.replace("embed", "check"));
                body = await res;
                urlReal[i].code = JSON.parse(body).file
                urlReal[i].direct = true
                break;
            case "natsuki":
                res = await cloudscraper(urlReal[i].code.replace("embed", "check"));
                body = await res;
                urlReal[i].code = JSON.parse(body).file
                urlReal[i].direct = true
                break;
            default:
                urlReal[i].direct = false
                break;
        }
    }

    urlReal.map(doc => {
        promises.push({
            id: doc.title.toLowerCase(),
            url: doc.code,
            direct: doc.direct
        });
    });

    return promises;
}

const obtainPreviewNews = (encoded) => {

    let image;

    if (encoded.includes('<img title=')) {
        image = encoded.substring(encoded.indexOf("<img title=\""), encoded.indexOf("\" alt")).split('src=\"')[1]
    } else if (encoded.includes('<img src=')) {
        image = encoded
                  .substring(encoded.indexOf("<img src=\""), encoded.indexOf("\" alt"))
                  .substring(10).replace("http", "https")
                  .replace("httpss", "https")
    } else if (encoded.includes('<img')) {
        image = encoded.split("src=")[1].split(" class=")[0].replace("\"", '').replace('\"','')
    } else if (encoded.includes('https://www.youtube.com/embed/')) {
        let getSecondThumb = encoded.split('https://www.youtube.com/embed/')[1].split('?feature')[0]
        image = `https://img.youtube.com/vi/${getSecondThumb}/0.jpg`
    } else if (encoded.includes('https://www.dailymotion.com/')) {
        let getDailymotionThumb = encoded
                                    .substring(encoded.indexOf("\" src=\""), encoded.indexOf("\" a"))
                                    .substring(47)
        image = `https://www.dailymotion.com/thumbnail/video/${getDailymotionThumb}`
    } else {
        let number = Math.floor(Math.random() * 30);
        image = `${BASE_ARUPPI}news/${number}.png`
    }

    return image;
}

const structureThemes = async (body, indv, task) => {

    const promises = []
    let themes
    let respFinal

    if (task === 0) {
        for(let i = 0; i <= body.length -1; i++) {

            if (indv === true) {
                const data = await cloudscraper.get(`${BASE_THEMEMOE}themes/${body[i]}`);
                respFinal = JSON.parse(data)
                themes = await getThemes(respFinal[0].themes)
            } else {
                respFinal = body
                themes = await getThemes(body[0].themes)
            }

            respFinal.map(doc => {

                promises.push({
                    title: doc.name,
                    season: doc.season,
                    year: doc.year,
                    themes: themes,
                });

            });
        }
    } else if (task === 1) {
        respFinal = body
        themes = await getHeaderTheme(respFinal.themes)

        promises.push({
            title: respFinal.artistName,
            season: respFinal.season,
            year: respFinal.year,
            series: themes,
        });

    } else {

        respFinal = body
        themes = await getThemes(respFinal.themes)

        promises.push({
            title: respFinal.name,
            season: respFinal.season,
            year: respFinal.year,
            themes: themes,
        });

    }

    return promises;

};

const getHeaderTheme = async (series) => {

    let promises = []
    let data

    for(let i = 0; i <= series.length -1; i++) {

        data = await getThemes(series[i].themes)

        promises.push({
            title: series[i].name,
            season: series[i].season,
            year: series[i].year,
            themes: data,
        });

    }

    return promises;

};

const getThemes = async (themes) => {

    let promises = []

    themes.map(doc => {

        promises.push({
            name: doc.themeName,
            type: doc.themeType,
            video: doc.mirror.mirrorURL
        });

    });

    return promises;

};

module.exports = {
    animeflvInfo,
    getAnimeCharacters,
    getAnimeVideoPromo,
    animeExtraInfo,
    imageUrlToBase64,
    searchAnime,
    transformUrlServer,
    obtainPreviewNews,
    structureThemes,
    getThemes
}
