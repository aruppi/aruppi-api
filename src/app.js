const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const bodyParser = require('body-parser');

const middlewares = require('./middlewares/index').middleware;
const api = require('./api');

const app = express();

app.use(helmet());
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: false }));


app.get('/', (req, res) => {
  res.redirect('/api/')
});

app.get('/api/', (req, res) => {
  res.json({
    message: 'Tu~tu~ruuu! You have traveled to the API Black Hole'
  });
});

app.get('/api/v1', (req, res) => {
  res.json({
    message: 'Sorry, version v1 is deprecated, if you want to see content go to v2'
  });
});

app.use('/api/v2', api);

app.use(middlewares);

module.exports = app;
