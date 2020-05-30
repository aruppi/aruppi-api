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
        'GetAllAnimes': '/api/v2/getAllAnimes',
        'GetAnitakume': '/api/v2/getAnitakume',
        'GetNews': '/api/v2/getNews',
        'Season': '/api/v2/season/:year/:type',
        'GetLastEpisodes': '/api/v2/getLastEpisodes'
      }
    ]
  });
});

router.use('/', routes);

module.exports = router;