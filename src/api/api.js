const rss = require('rss-to-json');
const cloudscraper = require('cloudscraper');

const {
  animeflvInfo,
  imageUrlToBase64,
  getAnimeCharacters,
  getAnimeVideoPromo,
  animeExtraInfo,
  searchAnime,
  transformUrlServer
} = require('../utils/index');

const {
  BASE_ANIMEFLV, BASE_ANIMEFLV_JELU, BASE_JIKAN, BASE_IVOOX, BASE_QWANT, BASE_YOUTUBE
} = require('./urls');

const schedule = async (day) =>{

  const data = await cloudscraper.get(`${BASE_JIKAN}schedule/${day.current}`);
  const body = JSON.parse(data)[day.current]
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

  const data = await cloudscraper.get(`${BASE_JIKAN}top/${type}/${page}/${subtype}`);

  return JSON.parse(data).top;

};

const getAllAnimes = async () =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV}api/animes/list`);
  const body = JSON.parse(data);
  return body.map(item => ({
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
          content: doc[pageRss[i].content]
        });

      });

    });

  }

  return promises;

};

const season = async (year, type) =>{

  const data = await cloudscraper.get(`${BASE_JIKAN}season/${year}/${type}`);
  let body = JSON.parse(data).anime;
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

const getLastEpisodes = async () =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}LatestEpisodesAdded`);
  let body = JSON.parse(data).episodes;
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      image: doc.poster,
      episode: doc.episode,
      servers: doc.servers.map(x => x)
    });
  });

  return promises;

};

const getSpecials = async (type, subType, page) =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}${type.url}/${subType}/${page}`);
  let body = JSON.parse(data)[type.prop];
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

    switch (animeType) {

      case "tv":
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
        break;
      case "movie":
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
        break;
      case "ova":
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
        break;
      default:
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

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}GetAnimeServers/${id}`);
  let body = JSON.parse(data).servers;

  return await transformUrlServer(body);

};

const search = async (title) =>{ return await searchAnime(title); };

const getImages = async (query) => {

  const data = await cloudscraper.get(`${BASE_QWANT}count=${query.count}&q=${query.title}&t=${query.type}&safesearch=${query.safesearch}&locale=${query.country}&uiv=4`);
  const body = JSON.parse(data).data.result.items;
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

  const data = await cloudscraper.get(`${BASE_YOUTUBE}${channelId.id}&part=${channelId.part}&order=${channelId.order}&maxResults=${channelId.maxResults}`);
  const body = JSON.parse(data)[channelId.prop];
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

module.exports = {
  schedule,
  top,
  getAllAnimes,
  getAnitakume,
  getNews,
  season,
  getLastEpisodes,
  getSpecials,
  getMoreInfo,
  getAnimeServers,
  search,
  getImages,
  getYoutubeVideos
};
