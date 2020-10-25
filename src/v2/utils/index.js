const {
    BASE_ANIMEFLV, BASE_JIKAN, BASE_EPISODE_IMG_URL, BASE_ARUPPI, ANIMEFLV_SEARCH
} = require('../api/urls.js');

const {
    homgot
} = require('../api/apiCall.js');

const animeflvGenres = async (id) => {

    const promises = [];

    let options = { scrapy: true }
    let $ = await homgot(`${BASE_ANIMEFLV}${id}`, options);

    $('main.Main section.WdgtCn nav.Nvgnrs a').each((index, element) => {
        const $element = $(element);
        const genre = $element.attr('href').split('=')[1] || null;
        promises.push(genre);
    });

    return promises;

}

const animeflvInfo = async (id, index) => {

    try {

        let $ = await homgot(`${BASE_ANIMEFLV}/${id}`, { scrapy: true })

        const scripts = $('script');
        const anime_info_ids = [];
        const anime_eps_data = [];
        const animeExtraInfo = [];
        const genres = [];
        let listByEps;

        const animeTitle = $('body div.Wrapper div.Body div div.Ficha.fchlt div.Container h2.Title').text();
        const poster = `${BASE_ANIMEFLV}` + $('body div div div div div aside div.AnimeCover div.Image figure img').attr('src')
        const banner = poster.replace('covers', 'banners').trim();
        const synopsis = $('body div div div div div main section div.Description p').text().trim();
        const rating = $('body div div div.Ficha.fchlt div.Container div.vtshr div.Votes span#votes_prmd').text();
        const debut = $('body div.Wrapper div.Body div div.Container div.BX.Row.BFluid.Sp20 aside.SidebarA.BFixed p.AnmStts').text();
        const type = $('body div.Wrapper div.Body div div.Ficha.fchlt div.Container span.Type').text()

        animeExtraInfo.push({
            title: animeTitle,
            poster: poster,
            banner: banner,
            synopsis: synopsis,
            rating: rating,
            debut: debut,
            type: type,
        })

        $('main.Main section.WdgtCn nav.Nvgnrs a').each((index, element) =>
            genres.push($(element).attr('href').split('=')[1] || null)
        );

        Array.from({length: scripts.length} , (v , k) =>{
            const $script = $(scripts[k]);
            const contents = $script.html();
            if((contents || '').includes('var anime_info = [')) {
                let anime_info = contents.split('var anime_info = ')[1].split(';')[0];
                let dat_anime_info = JSON.parse(JSON.stringify(anime_info));//JSON.parse(anime_info);
                anime_info_ids.push(dat_anime_info);
            }
            if((contents || '').includes('var episodes = [')) {
                let episodes = contents.split('var episodes = ')[1].split(';')[0];
                let eps_data = JSON.parse(episodes)
                anime_eps_data.push(eps_data);
            }
        });

        const AnimeThumbnailsId = index;
        const animeId = id.split('anime/')[1];

        let nextEpisodeDate
        if (JSON.parse(JSON.stringify(anime_info_ids[0])).split('\"').length === 9) {
            nextEpisodeDate = JSON.parse(JSON.stringify(anime_info_ids[0])).split("\"")[7]
        } else {
            nextEpisodeDate = null
        }

        const amimeTempList = [];
        for (const [key] of Object.entries(anime_eps_data)) {
            let episode = anime_eps_data[key].map(x => x[0]);
            let episodeId = anime_eps_data[key].map(x => x[1]);
            amimeTempList.push(episode, episodeId);
        }
        const listEps = [{nextEpisodeDate: nextEpisodeDate}];
        Array.from({length: amimeTempList[1].length}, (v, k) => {
            let data = amimeTempList.map(x => x[k]);
            let episode = data[0];
            let id = data[1];
            let imagePreview = `${BASE_EPISODE_IMG_URL}${AnimeThumbnailsId}/${episode}/th_3.jpg`
            let link = `${id}/${animeId}-${episode}`

            listEps.push({
                episode: episode,
                id: link,
                imagePreview: imagePreview
            })
        })

        listByEps = listEps

        return {listByEps , genres , animeExtraInfo};

    } catch (e) {
        console.log(e)
    }

};

const getAnimeCharacters = async(title) =>{

    const matchAnime = await getMALid(title)

    try {
        if(matchAnime !== null) {
            const data = await homgot(`${BASE_JIKAN}anime/${matchAnime.mal_id}/characters_staff`, { parse: true });
            return data.characters.map(doc => ({
                id: doc.mal_id,
                name: doc.name,
                image: doc.image_url,
                role: doc.role
            }));
        }
    } catch (err) {
        console.log(err)
    }

};

const getAnimeVideoPromo = async(title) =>{

    const matchAnime = await getMALid(title)

    try {
        if(matchAnime !== null) {
            const data = await homgot(`${BASE_JIKAN}anime/${matchAnime.mal_id}/videos`, {parse: true})
            return data.promo.map(doc => ({
                title: doc.title,
                previewImage: doc.image_url,
                videoURL: doc.video_url
            }));
        }
    } catch (err) {
        console.log(err)
    }

};

const animeExtraInfo = async (title) => {

    const matchAnime = await getMALid(title)

    try {

        if(matchAnime !== null) {

            const data = await homgot(`${BASE_JIKAN}anime/${matchAnime.mal_id}`, {parse: true})
            const promises = [];
            let broadcast = ''

            Array(data).map(doc => {

                let airDay = {
                    'mondays': 'Lunes',
                    'monday': 'Lunes',
                    'tuesdays': 'Martes',
                    'tuesday': 'Martes',
                    'wednesdays': 'Miércoles',
                    'wednesday': 'Miércoles',
                    'thursdays': 'Jueves',
                    'thursday': 'Jueves',
                    'fridays': 'Viernes',
                    'friday': 'Viernes',
                    'saturdays': 'Sábados',
                    'saturday': 'Sábados',
                    'sundays': 'Domingos',
                    'sunday': 'Domingos',
                    'default': 'Sin emisión'
                };

                if (doc.broadcast === null) {
                    broadcast = null
                } else {
                    broadcast = airDay[doc.broadcast.split('at')[0].replace(" ", "").toLowerCase()]
                }

                promises.push({
                    titleJapanese: doc.title_japanese,
                    source: doc.source,
                    totalEpisodes: doc.episodes,
                    aired: {
                        from: doc.aired.from,
                        to: doc.aired.to
                    },
                    duration: doc.duration.split('per')[0],
                    rank: doc.rank,
                    broadcast: broadcast,
                    producers: doc.producers.map(x => x.name) || null,
                    licensors: doc.licensors.map(x => x.name) || null,
                    studios: doc.studios.map(x => x.name) || null,
                    openingThemes: doc.opening_themes || null,
                    endingThemes: doc.ending_themes || null
                });
            });
            return Promise.all(promises);
        }

    } catch (err) {
        console.log(err)
    }

};

const getMALid = async(title) =>{

    const res = await homgot(`${BASE_JIKAN}search/anime?q=${title}`,{ parse: true })
    const matchAnime = res.results.find(x => x.title === title);

    if(typeof matchAnime === 'undefined') {
        return null;
    } else {
        return matchAnime;
    }

};

const imageUrlToBase64 = async (url) => {
    let img = await homgot(url)
    return img.rawBody.toString('base64');
};

const searchAnime = async (query) => {
    let $ = await homgot(`${ANIMEFLV_SEARCH}q=${query}`, { scrapy: true });
    return Promise.all(await obtainAnimeSeries($));
};

const obtainAnimeSeries = async ($) => {

    let promises = []

    await asyncForEach($('div.Container ul.ListAnimes li article'), async (element) => {

        const $element = $(element);
        const id = $element.find('div.Description a.Button').attr('href').slice(1);
        const title = $element.find('a h3').text();
        let poster =$element.find('a div.Image figure img').attr('src') || $element.find('a div.Image figure img').attr('data-cfsrc');
        const type = $element.find('div.Description p span.Type').text();

        promises.push({
            id: id || null,
            title: title || null,
            type: type || null,
            image: await imageUrlToBase64(poster) || null
        });

    })

    return promises;
}

async function asyncForEach(array, callback) {
    for (let index = 0; index < array.length; index++) {
        await callback(array[index], index, array);
    }
}

const transformUrlServer = async (urlReal) => {

    let res
    const promises = []

    for (const index in urlReal) {
        if (urlReal[index].server === 'amus' || urlReal[index].server === 'natsuki') {

            let options = { parse: true }
            res = await homgot(urlReal[index].code.replace("embed", "check"), options);

            urlReal[index].code = res.file || null
            urlReal[index].direct = true
        }

    }

    urlReal.map(doc => {
        promises.push({
            id: doc.title.toLowerCase(),
            url: doc.code,
            direct: doc.direct || false
        });
    });

    return promises;
}

const obtainPreviewNews = (encoded) => {

    let image;

    if (encoded.includes('src="https://img1.ak.crunchyroll.com/')) {
        if (encoded.split('https://img1.ak.crunchyroll.com/')[1].includes('.jpg')) {
            image = `https://img1.ak.crunchyroll.com/${encoded.split('https://img1.ak.crunchyroll.com/')[1].split('.jpg')[0]}.jpg`
        } else {
            image = `https://img1.ak.crunchyroll.com/${encoded.split('https://img1.ak.crunchyroll.com/')[1].split('.png')[0]}.png`
        }
    } else if (encoded.includes('<img title=')) {
        image = encoded.substring(encoded.indexOf("<img title=\""), encoded.indexOf("\" alt")).split('src=\"')[1]
    } else if (encoded.includes('<img src=')) {
        image = encoded
            .substring(encoded.indexOf("<img src=\""), encoded.indexOf("\" alt"))
            .substring(10).replace("http", "https")
            .replace("httpss", "https")
    } else if (encoded.includes('<img')) {
        image = encoded.split("src=")[1].split(" class=")[0].replace("\"", '').replace('\"', '')
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

const structureThemes = async (body, indv) => {

    const promises = []
    let themes

    if (indv === true) {
        themes = await getThemesData(body.themes)

        promises.push({
            title: body.title,
            year: body.year,
            themes: themes,
        });

    } else {

        for (let i = 0; i <= body.length - 1; i++) {

            themes = await getThemesData(body[i].themes)

            promises.push({
                title: body[i].title,
                year: body[i].year,
                themes: themes,
            });

        }

    }

    return promises;

};


const getThemesData = async (themes) => {

    let promises = []

    for (let i = 0; i <= themes.length - 1; i++) {

        promises.push({
            title: themes[i].name.split('"')[1] || 'Remasterización',
            type: themes[i].name.split('"')[0] || 'OP/ED',
            video: themes[i].link
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

const getAnimes = async () => await homgot(`${BASE_ANIMEFLV}api/animes/list`, { parse: true });

const getDirectory = async () => {

    let data = JSON.parse(JSON.stringify(require('../../assets/directory.json')));
    return data.map(doc => ({
        id: doc.id,
        title: doc.title,
        mal_title: doc.mal_title,
        poster: doc.poster,
        type: doc.type,
        genres: doc.genres,
        state: doc.state,
        score: doc.score,
        jkanime: false,
        description: doc.description
    }));

};

module.exports = {
    animeflvGenres,
    animeflvInfo,
    getAnimeCharacters,
    getAnimeVideoPromo,
    animeExtraInfo,
    getMALid,
    imageUrlToBase64,
    searchAnime,
    transformUrlServer,
    obtainPreviewNews,
    structureThemes,
    getThemes,
    getAnimes,
    getDirectory
}
