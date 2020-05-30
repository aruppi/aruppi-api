const express = require('express');
const routes = require('./routes/index');

const router = express.Router();

router.get('/', (req, res) => {
  res.json({
    message: 'Aruppi API - 🎏',
    author: 'Jéluchu',
    version: '1.0.0',
    credits: 'The bitch loves APIs that offers data to Aruppi App',
    entries: [
      {
        'Schedule': '/api/v1/schedule/:id',
        'Top': '/api/v1/top/:type/:subtype/:page',
        'GetAllAnimes': '/api/v1/getAllAnimes',
        'GetAnitakume': '/api/v1/getAnitakume',
        'GetNews': '/api/v1/getNews'
      }
    ]
  });
});

router.use('/', routes);

module.exports = router;