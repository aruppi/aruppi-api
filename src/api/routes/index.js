const express = require('express');
const router = express.Router();
const api = require('../api');

router.get('/schedule/:day' , (req, res) =>{

    let day = req.params.day;

    api.schedule(day)
        .then(day =>{
            res.status(200).json({
                day
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/top/:type/:subtype/:page' , (req, res) =>{

    let type = req.params.type;
    let subtype = req.params.subtype;
    let page = req.params.page;

    api.top(type, subtype, page)
        .then(top =>{
            res.status(200).json({
                top
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/getAllAnimes' , (req, res) =>{

    api.getAllAnimes()
        .then(animes =>{
            res.status(200).json({
                animes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/getAnitakume' , (req, res) =>{

    api.getAnitakume()
        .then(podcast =>{
            res.status(200).json({
                podcast
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/getNews' , (req, res) =>{

    api.getNews()
        .then(news =>{
            res.status(200).json({
                news
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/season/:year/:type' , (req, res) =>{

    let year = req.params.year;
    let type = req.params.type;

    api.season(year, type)
        .then(season =>{
            res.status(200).json({
                season
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/getLastEpisodes' , (req, res) =>{

    api.getLastEpisodes()
        .then(episodes =>{
            res.status(200).json({
                episodes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

module.exports = router;
