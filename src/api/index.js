const express = require('express');
const routes = require('./routes/index');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    message: 'Aruppi API - 🎏',
    author: 'Jéluchu',
    version: '2.2.4',
    credits: 'The bitch loves APIs that offers data to Aruppi App',
    entries: [
      {
        'Schedule': '/api/v2/schedule/:day',
        'Top': '/api/v2/top/:type/:subtype/:page',
        'AllAnimes': '/api/v2/allAnimes',
        'Anitakume': '/api/v2/anitakume',
        'News': '/api/v2/news',
        'Season': '/api/v2/season/:year/:type',
        'All Seasons': '/api/v2/allSeasons',
        'Futures Seasons': '/api/v2/laterSeasons',
        'LastEpisodes': '/api/v2/lastEpisodes',
        'Movies': '/api/v2/movies/:type/:page',
        'Ovas': '/api/v2/ovas/:type/:page',
        'Specials': '/api/v2/specials/:type/:page',
        'Tv': '/api/v2/tv/:type/:page',
        'MoreInfo': '/api/v2/moreInfo/:title',
        'GetAnimeServers': '/api/v2/getAnimeServers/:id',
        'Search': '/api/v2/search/:title',
        'Images': '/api/v2/images/:query',
        'Videos': '/api/v2/videos/:channelId',
        'Radios': '/api/v2/radio',
        'Themes': '/api/v2/themes/:title',
        'Season Themes': '/api/v2/themeSeason/:year/:season?',
        'Random Theme': '/api/v2/randomTheme',
        'Artists Theme': '/api/v2/artists/:id?'
      }
    ]
  });
});

router.use('/', routes);

module.exports = router;
