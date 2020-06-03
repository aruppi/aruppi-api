const rss = require('rss-to-json');
const cloudscraper = require('cloudscraper');
const animeflv = require("animeflv-scrapper");

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
  BASE_ANIMEFLV, BASE_ANIMEFLV_JELU, BASE_JIKAN, BASE_IVOOX, BASE_KUDASAI, BASE_PALOMITRON, BASE_RAMENPARADOS
} = require('./urls');

const schedule = async (day) =>{

  const data = await cloudscraper.get(`${BASE_JIKAN}schedule/${day}`);
  const promises = []
  let body;

  switch (day) {
    case "monday":
      body = JSON.parse(data).monday;
      break;
    case "tuesday":
      body = JSON.parse(data).tuesday;
      break;
    case "wednesday":
      body = JSON.parse(data).wednesday;
      break;
    case "thursday":
      body = JSON.parse(data).thursday;
      break;
    case "friday":
      body = JSON.parse(data).friday;
      break;
    case "saturday":
      body = JSON.parse(data).saturday;
      break;
    case "sunday":
      body = JSON.parse(data).sunday;
      break;
    default:
      body = JSON.parse(data).monday;
  }

  body.map(doc =>{

    promises.push({
      title: doc.title,
      malid: doc.mal_id,
      image: doc.image_url
    });
  });

  return Promise.all(promises);

};

const top = async (type, subtype, page) =>{

  const data = await cloudscraper.get(`${BASE_JIKAN}top/${type}/${page}/${subtype}`);
  let body = JSON.parse(data).top;

  return Promise.all(body);

};

const getAllAnimes = async () =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV}api/animes/list`);
  let body = JSON.parse(data);

  return Promise.all(body);

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

  return Promise.all(promises);

};

const getNews = async () =>{

  const promises = []

  await rss.load(BASE_KUDASAI).then(rss => {

    const body = JSON.parse(JSON.stringify(rss, null, 3)).items
    body.map(doc =>{

      promises.push({
        title: doc.title,
        url: doc.link,
        author: 'Kudasai',
        content: doc.content_encoded
      });
    });

  });

  await rss.load(BASE_PALOMITRON).then(rss => {

    const body = JSON.parse(JSON.stringify(rss, null, 3)).items
    body.map(doc =>{

      promises.push({
        title: doc.title,
        url: doc.link,
        author: 'Palomitron',
        content: doc.description
      });
    });

  });

  await rss.load(BASE_RAMENPARADOS).then(rss => {

    const body = JSON.parse(JSON.stringify(rss, null, 3)).items
    body.map(doc =>{
      console.log(doc)
      promises.push({
        title: doc.title,
        url: doc.link,
        author: 'Ramen para dos',
        content: doc.content
      });
    });

  });

  return Promise.all(promises);

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

  return Promise.all(promises);

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

  return Promise.all(promises);

};

const getMovies = async (type, page) =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}Movies/${type}/${page}`);
  let body = JSON.parse(data).movies;
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
    });
  });

  return Promise.all(promises);

};

const getOvas = async (type, page) =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}Ova/${type}/${page}`);
  let body = JSON.parse(data).ova;
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
    });
  });

  return Promise.all(promises);

};

const getSpecials = async (type, page) =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}Special/${type}/${page}`);
  let body = JSON.parse(data).special;
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
    });
  });

  return Promise.all(promises);

};

const getTv = async (type, page) =>{

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}Tv/${type}/${page}`);
  let body = JSON.parse(data).tv;
  const promises = []

  body.map(doc =>{

    promises.push({
      id: doc.id,
      title: doc.title,
      banner: doc.banner,
      image: doc.poster,
      synopsis: doc.synopsis,
      status: doc.debut,
      rate: doc.rating,
      genres: doc.genres.map(x => x),
      episodes: doc.episodes.map(x => x)
    });
  });

  return Promise.all(promises);

};

const getMoreInfo = async (title) =>{

  const promises = []
  let animeTitle =""
  let animeId = ""
  let animeType = ""

  await animeflv.searchAnime(title).then(data => {
    data.forEach(function (anime) {
          if (anime.label.split('\t')[0] === title.split('\t')[0] || anime.label === `${title} (TV)`) {
            if (anime.label.includes('(TV)', 0)) { animeTitle = anime.label.split('\t')[0].replace(' (TV)', '') }
            else { animeTitle = anime.label.split('\t')[0] }
            animeId = anime.animeId
            animeType = anime.type.toLowerCase()
          }
        }
    )
  });

  try{

    switch (animeType) {

      case "anime":
          promises.push(await animeflvInfo(animeId).then(async extra => ({
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
      case "película":
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

  return Promise.all(promises);
};

const getAnimeServers = async (id) => {

  const data = await cloudscraper.get(`${BASE_ANIMEFLV_JELU}GetAnimeServers/${id}`);
  let body = JSON.parse(data).servers;

  return Promise.all(await transformUrlServer(body));

};

const search = async (title) =>{
  return await searchAnime(title);
};

module.exports = {
  schedule,
  top,
  getAllAnimes,
  getAnitakume,
  getNews,
  season,
  getLastEpisodes,
  getMovies,
  getOvas,
  getSpecials,
  getTv,
  getMoreInfo,
  getAnimeServers,
  search
};
