const cloudscraper = require('cloudscraper')
const imageToBase64 = require("image-to-base64");
const cheerio = require('cheerio');

const {
  BASE_ANIMEFLV, BASE_JIKAN, BASE_EPISODE_IMG_URL
} = require('../api/urls');

const animeflvInfo = async(id) =>{
  try{
    const res = await cloudscraper(`${BASE_ANIMEFLV}anime/${id}`);
    const body = await res;
    const $ = cheerio.load(body);
    const scripts = $('script');
    const anime_info_ids = [];
    const anime_eps_data = [];
    const animeExtraInfo = [];
    const genres = [];
    let listByEps;

    let animeTitle = $('body div.Wrapper div.Body div div.Ficha.fchlt div.Container h2.Title').text();
    let poster = `${BASE_ANIMEFLV}` + $('body div div div div div aside div.AnimeCover div.Image figure img').attr('src')
    const banner = poster.replace('covers' , 'banners').trim();
    let synopsis = $('body div div div div div main section div.Description p').text().trim();
    let rating = $('body div div div.Ficha.fchlt div.Container div.vtshr div.Votes span#votes_prmd').text();
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

    $('main.Main section.WdgtCn nav.Nvgnrs a').each((index , element) =>{
      const $element = $(element);
      const genre = $element.attr('href').split('=')[1] || null;
      genres.push(genre);
    });


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
    const AnimeThumbnailsId = anime_info_ids[0].split(',')[0].split('"')[1];
    const animeId = id;
    let nextEpisodeDate = anime_info_ids[0][3] || null
    const amimeTempList = [];
    for(const [key , value] of Object.entries(anime_eps_data)){
      let episode = anime_eps_data[key].map(x => x[0]);
      let episodeId = anime_eps_data[key].map(x => x[1]);
      amimeTempList.push(episode , episodeId);
    }
    const animeListEps = [{nextEpisodeDate: nextEpisodeDate}];
    Array.from({length: amimeTempList[1].length} , (v , k) =>{
      let data = amimeTempList.map(x => x[k]);
      let episode = data[0];
      let id = data[1];
      let imagePreview = `${BASE_EPISODE_IMG_URL}${AnimeThumbnailsId}/${episode}/th_3.jpg`
      let link = `${id}/${animeId}-${episode}`
      // @ts-ignore
      animeListEps.push({
        episode: episode,
        id: link,
        imagePreview: imagePreview
      })
    })

    listByEps = animeListEps;

    return {listByEps , genres , animeExtraInfo};
  }catch(err){
    console.error(err)
  }
};

const getAnimeCharacters = async(title) =>{
  const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
  const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
  const malId = matchAnime[0].mal_id;

  if(typeof matchAnime[0].mal_id === 'undefined') return null;

  const jikanCharactersURL = `${BASE_JIKAN}anime/${malId}/characters_staff`;
  const data = await cloudscraper.get(jikanCharactersURL);
  let body = JSON.parse(data).characters;

  if(typeof body === 'undefined') return null;

  const charactersId = body.map(doc =>{
    return doc.mal_id
  })
  const charactersNames = body.map(doc => {
    return doc.name;
  });
  const charactersImages = body.map(doc =>{
    return doc.image_url
  });

  let characters = [];
  Array.from({length: charactersNames.length} , (v , k) =>{
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

const getAnimeVideoPromo = async(title) =>{
  const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
  const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
  const malId = matchAnime[0].mal_id;

  if(typeof matchAnime[0].mal_id === 'undefined') return null;

  const jikanCharactersURL = `${BASE_JIKAN}anime/${malId}/videos`;
  const data = await cloudscraper.get(jikanCharactersURL);
  const body = JSON.parse(data).promo;
  const promises = [];

  body.map(doc =>{
    promises.push({
      title: doc.title,
      previewImage: doc.image_url,
      videoURL: doc.video_url
    });
  });

  return Promise.all(promises);
};

const animeExtraInfo = async(title) =>{
  const res = await cloudscraper(`${BASE_JIKAN}search/anime?q=${title}`);
  const matchAnime = JSON.parse(res).results.filter(x => x.title === title);
  const malId = matchAnime[0].mal_id;

  if(typeof matchAnime[0].mal_id === 'undefined') return null;

  const animeDetails = `${BASE_JIKAN}anime/${malId}`;
  const data = await cloudscraper.get(animeDetails);
  const body = Array(JSON.parse(data));
  const promises = [];

  body.map(doc =>{

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
      aired:{
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

const imageUrlToBase64 = async(url) => {
  let res = await cloudscraper({
    url,
    method: "GET",
    encoding: null
  });

  return Buffer.from(res).toString("base64");
};

const MergeRecursive = (obj1 , obj2) => {
  for(var p in obj2) {
    try{
      // Property in destination object set; update its value.
      if(obj2[p].constructor == Object){
        obj1[p] = MergeRecursive(obj1[p], obj2[p]);
      }else{
        obj1[p] = obj2[p];
      }
    }catch(e){
      // Property in destination object not set; create it and set its value.
      obj1[p] = obj2[p];
    }
  }
  return obj1;
}

const urlify = async(text) =>{
  const urls = [];
  const urlRegex = /(https?:\/\/[^\s]+)/g;
  text.replace(urlRegex , (url) =>{
    urls.push(url)
  });
  return Promise.all(urls);
};


module.exports = {
  animeflvInfo,
  getAnimeCharacters,
  getAnimeVideoPromo,
  animeExtraInfo,
  imageUrlToBase64,
  MergeRecursive,
  urlify
}