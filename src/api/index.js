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
        'Top': '/top/:type/:subtype/:page',
        'GetAllAnimes': '/getAllAnimes'
      }
    ]
  });
});

router.use('/', routes);

module.exports = router;