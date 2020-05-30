const rss = require('rss-to-json');
const cloudscraper = require('cloudscraper');
const {
  BASE_ANIMEFLV, BASE_ANIMEFLV_JELU, BASE_JIKAN, BASE_IVOOX, BASE_KUDASAI, BASE_PALOMITRON
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
      image: doc.image_url
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

module.exports = {
  schedule,
  top,
  getAllAnimes,
  getAnitakume,
  getNews,
  season,
  getLastEpisodes
};
