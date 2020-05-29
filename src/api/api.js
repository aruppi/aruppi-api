const cheerio = require('cheerio');
const cheerioTableparser = require('cheerio-tableparser');
const cloudscraper = require('cloudscraper');
const {
  BASE_ANIMEFLV         , BASE_JIKAN
} = require('./urls');

const schedule = async (day) =>{

  const data = await cloudscraper.get(`${BASE_JIKAN}schedule/${day}`);
  let body = "";

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

  return Promise.all(body);

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

module.exports = {
  schedule,
  top,
  getAllAnimes
};
