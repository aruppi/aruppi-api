const rss = require('rss-to-json');
const fuzzball = require('fuzzball');
const {
  homgot
} = require('../api/apiCall');
const {
  jkanimeInfo,
  animeflvInfo,
  getAnimeCharacters,
  getAnimeVideoPromo,
  animeExtraInfo,
  searchAnime,
  transformUrlServer,
  obtainPreviewNews,
  structureThemes,
  videoServersJK,
  getThemes,
  getRelatedAnimesFLV,
  getRelatedAnimesMAL,
  directoryAnimes,
  radioStations,
  animeGenres,
  animeThemes
} = require('../utils/index');

const ThemeParser = require('../utils/animetheme');
const parserThemes = new ThemeParser();

const {
  BASE_ANIMEFLV_JELU, BASE_JIKAN, BASE_IVOOX, BASE_QWANT, BASE_YOUTUBE, BASE_THEMEMOE, BASE_ANIMEFLV, BASE_ARUPPI
} = require('./urls');

const schedule = async (day) =>{
  const data = await homgot(`${BASE_JIKAN}schedule/${day.current}`, { parse: true });

  return data[day.current].map(doc =>({
      title: doc.title,
      malid: doc.mal_id,
      image: doc.image_url
  }));
};

const top = async (top) =>{
  let data;

  if (top.subtype !== undefined) {
    data = await homgot(`${BASE_JIKAN}top/${top.type}/${top.page}/${top.subtype}`, { parse: true });
  } else {
    data = await homgot(`${BASE_JIKAN}top/${top.type}/${top.page}`, { parse: true });
  }

  return data.top.map(doc =>({
    rank: doc.rank,
    title: doc.title,
    url: doc.url,
    image_url: doc.image_url,
    type: top.type,
    subtype: top.subtype,
    page: top.page,
    score: doc.score
  }));
};

const getAllAnimes = async () =>{
  let data = await homgot(`${BASE_ANIMEFLV}api/animes/list`, { parse: true })

  return data.map(item => ({
    index: item[0],
    animeId: item[3],
    title: item[1],
    id: item[2],
    type: item[4]
  }));
};

const getAllDirectory = async (genres) => {
  if (genres === 'sfw') {
    return directoryAnimes.filter(function (doc) {
      if (doc.genres.indexOf('Ecchi') === -1 && doc.genres.indexOf('ecchi') === -1) {
        return {
          id: doc.id,
          title: doc.title,
          mal_id: doc.mal_id,
          poster: doc.poster,
          type: doc.type,
          genres: doc.genres,
          state: doc.state,
          score: doc.score,
          jkanime: false,
          description: doc.description
        };
      }
    });
  }

  return directoryAnimes.map(doc => ({
    id: doc.id,
    title: doc.title,
    mal_id: doc.mal_id,
    poster: doc.poster,
    type: doc.type,
    genres: doc.genres,
    state: doc.state,
    score: doc.score,
    jkanime: false,
    description: doc.description
  }));
};

const getAnitakume = async () => {

  const promises = [];

  await rss.load(BASE_IVOOX).then(rss => {

    const body = JSON.parse(JSON.stringify(rss, null, 3)).items
    body.map(doc =>{

      let time = new Date(doc.created);
      const monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];

      let day = time.getDate()
      let month = monthNames[time.getMonth()]
      let year = time.getFullYear()
      let date

      if(month < 10){
        date = `${day} de 0${month} de ${year}`
      }else{
        date = `${day} de ${month} de ${year}`
      }

      promises.push({
        title: doc.title,
        duration: doc.itunes_duration,
        created: date,
        mp3: doc.enclosures[0].url
      });
    });

  });

  return promises;

};

const getNews = async (pageRss) =>{
  let promises = [];

  for(let i = 0; i <= pageRss.length -1; i++) {
    await rss.load(pageRss[i].url).then(rss => {

      const body = JSON.parse(JSON.stringify(rss, null, 3)).items
      body.map(doc => {

        promises.push({
          title: doc.title,
          url: doc.link,
          author: pageRss[i].author,
          thumbnail: obtainPreviewNews(doc[pageRss[i].content]),
          content: doc[pageRss[i].content]
        });
      });
    });
  }

  return promises;
};

const season = async (season) =>{
  const data = await homgot(`${BASE_JIKAN}season/${season.year}/${season.type}`, { parse: true });

  return data.anime.map(doc =>({
    title: doc.title,
    image: doc.image_url,
    genres: doc.genres.map(x => x.name)
  }));
};

const allSeasons = async () =>{
  const data = await homgot(`${BASE_JIKAN}season/archive`, { parse: true });

  return data.archive.map(doc =>({
      year: doc.year,
      seasons: doc.seasons,
  }));
};

const laterSeasons = async () =>{
  const data = await homgot(`${BASE_JIKAN}season/later`, { parse: true });

  return data.anime.map(doc =>({
      title: doc.title,
      image: doc.image_url,
      malink: doc.url
  }));
};

const getLastEpisodes = async () =>{
  const data = await homgot(`${BASE_ANIMEFLV_JELU}LatestEpisodesAdded`, { parse: true });

  return await Promise.all(data.episodes.map(async (item) => ({
    id: item.id,
    title: item.title,
    image: item.poster,
    episode: item.episode,
    servers: await transformUrlServer(JSON.parse(JSON.stringify(item.servers)))
  })));
};

const getSpecials = async (data) =>{
  const res = await homgot(`${BASE_ANIMEFLV_JELU}${data.url}/${data.type}/${data.page}`, { parse: true });

  return res[data.prop].map(doc =>({
      id: doc.id,
      title: doc.title,
      type: data.url.toLowerCase(),
      page: data.page,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
  }));

};

const getMoreInfo = async (title) =>{
  try {
    const result = directoryAnimes.filter(x => {
      if (x.title === title) {
        return x;
      }else {
        return x.title === `${title} (TV)` ? x : undefined; 
      }
    })[0];
    
    if (!result.jkanime) {
      return {
        title: result.title || null,
        poster: result.poster || null,
        synopsis: result.description || null,
        status: result.state || null,
        type: result.type || null,
        rating: result.score || null,
        genres: result.genres || null,
        moreInfo: await animeExtraInfo(result.mal_id).then(info => info || null),
        promo: await getAnimeVideoPromo(result.mal_id).then(promo => promo || null),
        characters: await getAnimeCharacters(result.mal_id).then(characters => characters || null),
        related: await getRelatedAnimesFLV(result.id)
      };
    }else {
      return {
        title: result.title || null,
        poster: result.poster || null,
        synopsis: result.description || null,
        status: result.state || null,
        type: result.type || null,
        rating: result.score || null,
        genres: result.genres || null,
        moreInfo: await animeExtraInfo(result.mal_id).then(info => info || null),
        promo: await getAnimeVideoPromo(result.mal_id).then(promo => promo || null),
        characters: await getAnimeCharacters(result.mal_id).then(characters => characters || null),
        related: await getRelatedAnimesMAL(result.mal_id)
      };
    }
  } catch (e) {
    console.log(e);
  }
};

const getEpisodes = async (title) =>{
  try {
    const result = directoryAnimes.filter(x => {
      if (x.title === title) {
        return x;
      }else {
        return x.title === `${title} (TV)` ? x : undefined; 
      }
    })[0];

    if (!result.jkanime) {
      return await animeflvInfo(result.id).then(episodes => episodes || null);
    } else {
      return await jkanimeInfo(result.id).then(episodes => episodes || null);
    }

  } catch (e) {
    console.log(e);
  }

};

const getAnimeServers = async (id) => {
  if (isNaN(id.split('/')[0])) {
    return await videoServersJK(id);
  } else {
    const data = await homgot(`${BASE_ANIMEFLV_JELU}GetAnimeServers/${id}`, { parse: true });
    
    return await transformUrlServer(data.servers);
  }
};

const search = async (title) =>{ return await searchAnime(title); };

const getImages = async (query) => {
  try {
    const data = await homgot(`${BASE_QWANT}count=${query.count}&q=${query.title}&t=${query.type}&safesearch=${query.safesearch}&locale=${query.country}&uiv=4`, { parse: true });
    return data.data.result.items.map(doc =>({
      type: doc.thumb_type,
      thumbnail: `https:${doc.thumbnail}`,
      fullsize: `https:${doc.media_fullsize}`
    }));
  } catch (e) {
    console.log(e)
  }
};

const getYoutubeVideos = async (channelId) => {
  const data = await homgot(`${BASE_YOUTUBE}${channelId.id}&part=${channelId.part}&order=${channelId.order}&maxResults=${channelId.maxResults}`, { parse: true });

  return data[channelId.prop].map(doc =>({
     title: doc.snippet.title,
     videoId: doc.id.videoId,
     thumbDefault: doc.snippet.thumbnails.default.url,
     thumbMedium: doc.snippet.thumbnails.medium.url,
     thumbHigh: doc.snippet.thumbnails.high.url
  }));
};

const getSectionYoutubeVideos = async (type) => {

  if (type === 'learn') {
    let data = await homgot(`${BASE_YOUTUBE}UCCyQwSS6m2mVB0-H2FOFJtw&part=snippet,id&order=date&maxResults=50`, { parse: true });
    return data.items.map(doc =>({
      title: doc.snippet.title,
      videoId: doc.id.videoId,
      thumbDefault: doc.snippet.thumbnails.default.url,
      thumbMedium: doc.snippet.thumbnails.medium.url,
      thumbHigh: doc.snippet.thumbnails.high.url
    }));
  } else if (type === 'amv') {
    let yt1 = await homgot(`${BASE_YOUTUBE}UCkTFkshjAsLMKwhAe1uPC1A&part=snippet,id&order=date&maxResults=25`, { parse: true });
    let yt2 = await homgot(`${BASE_YOUTUBE}UC2cpvlLeowpqnR6bQofwNew&part=snippet,id&order=date&maxResults=25`, { parse: true });
    return yt1.items.concat(yt2.items).map(doc =>({
      title: doc.snippet.title,
      videoId: doc.id.videoId,
      thumbDefault: doc.snippet.thumbnails.default.url,
      thumbMedium: doc.snippet.thumbnails.medium.url,
      thumbHigh: doc.snippet.thumbnails.high.url
    }));
  } else if (type === 'produccer'){
    let yt1 = await homgot(`${BASE_YOUTUBE}UC-5MT-BUxTzkPTWMediyV0w&part=snippet,id&order=date&maxResults=25`, { parse: true });
    let yt2 = await homgot(`${BASE_YOUTUBE}UCwUeTOXP3DD9DIvHttowuSA&part=snippet,id&order=date&maxResults=25`, { parse: true });
    let yt3 = await homgot(`${BASE_YOUTUBE}UCA8Vj7nN8bzT3rsukD2ypUg&part=snippet,id&order=date&maxResults=25`, { parse: true });
    return yt1.items.concat(yt2.items.concat(yt3.items)).map(doc =>({
      title: doc.snippet.title,
      videoId: doc.id.videoId,
      thumbDefault: doc.snippet.thumbnails.default.url,
      thumbMedium: doc.snippet.thumbnails.medium.url,
      thumbHigh: doc.snippet.thumbnails.high.url
    }));
  }

};

const getRadioStations = async () => radioStations;

const getOpAndEd = async (title) => await structureThemes(await parserThemes.serie(title), true);

const getThemesYear = async (year) => {
  let data = [];

  if (year === undefined) {
    return await parserThemes.allYears();
  } else {
    data = await parserThemes.year(year);
    return await structureThemes(data, false);
  }
};

const getRandomTheme = async () => {
  let data = await homgot(`${BASE_THEMEMOE}roulette`, { parse: true });
  let themes = await getThemes(data.themes);

  return themes.map(doc =>({
    name: data.name,
    title: doc.name,
    link: doc.video
  }));
};

const getArtist = async (id) => {
  if (id === undefined) {
    return await parserThemes.artists();
  } else {
    return await structureThemes(await parserThemes.artist(id), false)
  }
};

const getAnimeGenres = async(genres) => {
  let res;
  let promises = [];

  if (genres.genre === undefined && genres.page === undefined && genres.order === undefined)  {
    return animeGenres;
  } else {

    if (genres.page !== undefined) {
      res = await homgot(`${BASE_ANIMEFLV_JELU}Genres/${genres.genre}/${genres.order}/${genres.page}`,{ parse: true })
    } else {
      res = await homgot(`${BASE_ANIMEFLV_JELU}Genres/${genres.genre}/${genres.order}/1`,{ parse: true })
    }

    let data = res.animes

    for (let i = 0; i <= data.length - 1; i++) {
      promises.push({
        id: data[i].id || null,
        title: data[i].title.trim() || null,
        mention: genres.genre,
        page: genres.page,
        poster: data[i].poster || null,
        banner: data[i].banner || null,
        synopsis: data[i].synopsis || null,
        type: data[i].type || null,
        rating: data[i].rating || null,
        genre: data[i].genres
      })
    }

    return promises;
  }
};

const getAllThemes = async () => animeThemes;

const getDestAnimePlatforms = async () => {
  let data = await homgot(`${BASE_ARUPPI}res/documents/animelegal/top.json`, { parse: true });

  return data.map(doc =>({
    id: doc.id,
    name: doc.name,
    logo: doc.logo
  }));
};

const getPlatforms = async (id) => {
  let data;

  if (id === undefined) {

    data = await homgot(`${BASE_ARUPPI}res/documents/animelegal/typeplatforms.json`, { parse: true });

    return data.map(doc =>({
      id: doc.id,
      name: doc.name,
      comming: doc.comming || false,
      cover: doc.cover
    }));

  } if (id === "producers" || id === "apps" || id === "publishers") {

        data = await homgot(`${BASE_ARUPPI}res/documents/animelegal/type/${id}.json`, { parse: true });

        return data.map(doc =>({
            id: doc.id,
            name: doc.name,
            logo: doc.logo,
            cover: doc.cover,
            description: doc.description,
            type: doc.type,
            moreInfo: doc.moreInfo,
            facebook: doc.facebook,
            twitter: doc.twitter,
            instagram: doc.instagram,
            webInfo: doc.webInfo,
            webpage: doc.webpage
        }));

  } else {

    data = await homgot(`${BASE_ARUPPI}res/documents/animelegal/type/${id}.json`, { parse: true });

    return data.map(doc =>({
      id: doc.id,
      name: doc.name,
      type: doc.type,
      logo: doc.logo,
      cover: doc.cover,
      webpage: doc.webpage,

    }));
  }
};

const getProfilePlatform = async (id) => {
  let data = await homgot(`${BASE_ARUPPI}res/documents/animelegal/platforms/${id}.json`, { parse: true });
  let channelId = { id: data[0].youtubeId, part: 'snippet,id', order: 'date', maxResults: '50', prop: 'items'  };
  let videos = await getYoutubeVideos(channelId)

  return data.map(doc =>({
    id: doc.id,
    name: doc.name,
    logo: doc.logo,
    cover: doc.cover,
    category: doc.category,
    description: doc.description,
    facebook: doc.facebook,
    twitter: doc.twitter,
    instagram: doc.instagram,
    webpage: doc.webpage,
    simulcast: doc.simulcast,
    paid: doc.paid,
    shop: doc.shop,
    faq: doc.faq,
    videos: videos
  }));
};

async function getRandomAnime() {
  const randomNumber = Math.floor(Math.random() * directoryAnimes.length);
  let result = directoryAnimes[randomNumber];

  if (!result.jkanime) {
    return {
      title: result.title || null,
      poster: result.poster || null,
      synopsis: result.description || null,
      status: result.state || null,
      type: result.type || null,
      rating: result.score || null,
      genres: result.genres || null,
      moreInfo: await animeExtraInfo(result.mal_id).then(info => info || null),
      promo: await getAnimeVideoPromo(result.mal_id).then(promo => promo || null),
      characters: await getAnimeCharacters(result.mal_id).then(characters => characters || null),
      related: await getRelatedAnimesFLV(result.id)
    };
  }else {
    return {
      title: result.title || null,
      poster: result.poster || null,
      synopsis: result.description || null,
      status: result.state || null,
      type: result.type || null,
      rating: result.score || null,
      genres: result.genres || null,
      moreInfo: await animeExtraInfo(result.mal_id).then(info => info || null),
      promo: await getAnimeVideoPromo(result.mal_id).then(promo => promo || null),
      characters: await getAnimeCharacters(result.mal_id).then(characters => characters || null),
      related: await getRelatedAnimesMAL(result.mal_id)
    };
  }
}

module.exports = {
  schedule,
  top,
  getAllAnimes,
  getAllDirectory,
  getAnitakume,
  getNews,
  season,
  allSeasons,
  laterSeasons,
  getLastEpisodes,
  getSpecials,
  getMoreInfo,
  getEpisodes,
  getAnimeServers,
  search,
  getImages,
  getYoutubeVideos,
  getRadioStations,
  getOpAndEd,
  getThemesYear,
  getRandomTheme,
  getArtist,
  getAnimeGenres,
  getAllThemes,
  getDestAnimePlatforms,
  getPlatforms,
  getSectionYoutubeVideos,
  getProfilePlatform,
  getRandomAnime
};
