const express = require('express');
const routes = require('./routes/index');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    message: 'Aruppi API - 🎏',
    author: 'Jéluchu',
    version: '2.0.0',
    credits: 'The bitch loves APIs that offers data to Aruppi App',
    entries: [
      {
        'Schedule': '/api/v2/schedule/:day',
        'Top': '/api/v2/top/:type/:subtype/:page',
        'AllAnimes': '/api/v2/allAnimes',
        'Anitakume': '/api/v2/anitakume',
        'News': '/api/v2/news',
        'Season': '/api/v2/season/:year/:type',
        'LastEpisodes': '/api/v2/lastEpisodes',
        'Movies': '/api/v2/movies',
        'Ovas': '/api/v2/ovas',
        'Specials': '/api/v2/specials',
        'Tv': '/api/v2/tv'
      }
    ]
  });
});

router.use('/', routes);

module.exports = router;