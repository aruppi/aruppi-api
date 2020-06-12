const rss = require('rss-to-json');
const html = require('got');

const {
  animeflvInfo,
  imageUrlToBase64,
  getAnimeCharacters,
  getAnimeVideoPromo,
  animeExtraInfo,
  searchAnime,
  transformUrlServer,
  obtainPreviewNews,
  structureThemes,
  getAnimes,
  helper
} = require('../utils/index');

const {
  BASE_ANIMEFLV_JELU, BASE_JIKAN, BASE_IVOOX, BASE_QWANT, BASE_YOUTUBE, BASE_THEMEMOE
} = require('./urls');

const schedule = async (day) =>{

  const data = await html(`${BASE_JIKAN}schedule/${day.current}`).json();
  const body = data[day.current];
  const promises = []

  body.map(doc =>{

    promises.push({
      title: doc.title,
      malid: doc.mal_id,
      image: doc.image_url
    });

  });

  return promises;

};

const top = async (type, subtype, page) =>{
  const data = await html(`${BASE_JIKAN}top/${type}/${page}/${subtype}`).json();
  return data.top;
};

const getAllAnimes = async () =>{

  let data = await getAnimes()

  return data.map(item => ({
    index: item[0],
    animeId: item[3],
    title: item[1],
    id: item[2],
    type: item[4]
  }));

};

const getAnitakume = async () =>{

  const promises = []

  await rss.load(BASE_IVOOX).then(rss => {

    const body = JSON.parse(JSON.stringify(rss, null, 3)).items
    body.map(doc =>{

      let time = new Date(doc.created)
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
        mp3: doc.enclosures.map(x => x.url)
      });
    });

  });

  return promises;

};

const getNews = async (pageRss) =>{

  const promises = []

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

const season = async (year, type) =>{

  const data = await html(`${BASE_JIKAN}season/${year}/${type}`).json();
  let body = data.anime;
  const promises = []

  body.map(doc =>{

    promises.push({
      title: doc.title,
      malid: doc.mal_id,
      image: doc.image_url,
      genres: doc.genres.map(x => x.name)
    });
  });

  return promises;

};

const allSeasons = async () =>{

  const data = await html(`${BASE_JIKAN}season/archive`).json();
  let body = data.archive;
  const promises = []

  body.map(doc =>{

    promises.push({
      year: doc.year,
      seasons: doc.seasons,
    });
  });

  return promises;

};

const laterSeasons = async () =>{

  const data = await html(`${BASE_JIKAN}season/later`).json();
  let body = data.anime;
  const promises = []

  body.map(doc =>{

    promises.push({
      malid: doc.mal_id,
      title: doc.title,
      image: doc.image_url
    });
  });

  return promises;

};

const getLastEpisodes = async () =>{

  const data = await html(`${BASE_ANIMEFLV_JELU}LatestEpisodesAdded`).json();
  let body = data.episodes;
  const promises = []

  body.map(doc => {

    promises.push(helper().then(async () => ({
       id: doc.id,
       title: doc.title,
       image: doc.poster,
       episode: doc.episode,
       servers: await transformUrlServer(JSON.parse(JSON.stringify(doc.servers)))
     })));

  });

  return Promise.all(promises);

};

const getSpecials = async (type, subType, page) =>{

  const data = await html(`${BASE_ANIMEFLV_JELU}${type.url}/${subType}/${page}`).json();
  let body = data[type.prop];
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      type: doc.type,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
    });
  });

  return promises;

};

const getMoreInfo = async (title) =>{

  const promises = []
  let animeTitle = ''
  let animeId = ''
  let animeType = ''
  let animeIndex = ''

  await getAllAnimes().then(data => {
    data.forEach(function (anime) {
          if (anime.title.split('\t')[0] === title.split('\t')[0] || anime.title === `${title} (TV)`) {
            if (anime.title.includes('(TV)', 0)) { animeTitle = anime.title.split('\t')[0].replace(' (TV)', '') }
            else { animeTitle = anime.title.split('\t')[0] }
            animeId = anime.id
            animeIndex = anime.index
            animeType = anime.type.toLowerCase()
          }
        }
    )
  });

  try{

    if (animeType === 'tv') {
        promises.push(await animeflvInfo(animeId, animeIndex).then(async extra => ({
          title: animeTitle || null,
          poster: await imageUrlToBase64(extra.animeExtraInfo[0].poster) || null,
          synopsis: extra.animeExtraInfo[0].synopsis || null,
          status: extra.animeExtraInfo[0].debut || null,
          type: extra.animeExtraInfo[0].type || null,
          rating: extra.animeExtraInfo[0].rating || null,
          genres: extra.genres || null,
          episodes: extra.listByEps || null,
          moreInfo: await animeExtraInfo(title).then(info =>{
            return info || null
          }),
          promo: await getAnimeVideoPromo(title).then(promo =>{
            return promo || null
          }),
          characters: await getAnimeCharacters(animeTitle).then(characters =>{
            return characters || null
          })
        })));
    } else {
      promises.push(await animeflvInfo(animeId).then(async extra => ({
        title: animeTitle || null,
        poster: await imageUrlToBase64(extra.animeExtraInfo[0].poster) || null,
        synopsis: extra.animeExtraInfo[0].synopsis || null,
        status: extra.animeExtraInfo[0].debut || null,
        type: extra.animeExtraInfo[0].type || null,
        rating: extra.animeExtraInfo[0].rating || null,
        genres: extra.genres || null,
        episodes: extra.listByEps || null,
      })));
    }

  }catch(err){
    console.log(err)
  }

  return promises;

};

const getAnimeServers = async (id) => {

  const data = await html(`${BASE_ANIMEFLV_JELU}GetAnimeServers/${id}`).json();
  let body = data.servers;

  return await transformUrlServer(body);

};

const search = async (title) =>{ return await searchAnime(title); };

const getImages = async (query) => {

  const data = await html(`${BASE_QWANT}count=${query.count}&q=${query.title}&t=${query.type}&safesearch=${query.safesearch}&locale=${query.country}&uiv=4`).json();
  const body = data.data.result.items;
  const promises = []

  body.map(doc =>{

    promises.push({
      type: doc.thumb_type,
      thumbnail: `https:${doc.thumbnail}`,
      fullsize: `https:${doc.media_fullsize}`
    });

  });

  return promises;

};

const getYoutubeVideos = async (channelId) => {

  const data = await html(`${BASE_YOUTUBE}${channelId.id}&part=${channelId.part}&order=${channelId.order}&maxResults=${channelId.maxResults}`).json();
  const body = data[channelId.prop];
  const promises = []

  body.map(doc =>{

    promises.push({
      title: doc.snippet.title,
      videoId: doc.id.videoId,
      thumbDefault: doc.snippet.thumbnails.default.url,
      thumbMedium: doc.snippet.thumbnails.medium.url,
      thumbHigh: doc.snippet.thumbnails.high.url
    });

  });

  return promises;

};

const getRadioStations = async () => {
  return require('../assets/radiostations.json');
}

const getOpAndEd = async (title) => {
  const data = await html(`${BASE_THEMEMOE}anime/search/${title}`).json();
  return await structureThemes(data, true, 0)
};

const getThemesSeason = async (year, season) => {

  let data

  if (season === undefined) {
    data = await html(`${BASE_THEMEMOE}seasons/${year}`).json();
  } else {
    data = await html(`${BASE_THEMEMOE}seasons/${year}/${season}`).json();
  }

  return await structureThemes(data, false, 0)

};

const getRandomTheme = async () => {
  const data = await html(`${BASE_THEMEMOE}roulette`).json();
  return await structureThemes(data, true)
};

const getArtist = async (id) => {

  let data
  let promises = []

  if (id === undefined) {

    data = await html(`${BASE_THEMEMOE}artists`).json();
    data.map(doc => {

      promises.push({
        id: doc.artistID,
        name: doc.artistName
      })

    });

    return promises;

  } else {
    data = await html(`${BASE_THEMEMOE}artists/${id}`).json();
    return await structureThemes(data, false, 1)
  }

};

module.exports = {
  schedule,
  top,
  getAllAnimes,
  getAnitakume,
  getNews,
  season,
  allSeasons,
  laterSeasons,
  getLastEpisodes,
  getSpecials,
  getMoreInfo,
  getAnimeServers,
  search,
  getImages,
  getYoutubeVideos,
  getRadioStations,
  getOpAndEd,
  getThemesSeason,
  getRandomTheme,
  getArtist
};
