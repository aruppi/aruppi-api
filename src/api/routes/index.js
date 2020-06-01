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

router.get('/allAnimes' , (req, res) =>{

    api.getAllAnimes()
        .then(animes =>{
            res.status(200).json({
                animes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/anitakume' , (req, res) =>{

    api.getAnitakume()
        .then(podcast =>{
            res.status(200).json({
                podcast
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/news' , (req, res) =>{

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

router.get('/lastEpisodes' , (req, res) =>{

    api.getLastEpisodes()
        .then(episodes =>{
            res.status(200).json({
                episodes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/movies/:type/:page' , (req, res) =>{

    let type = req.params.type;
    let page = req.params.page;

    api.getMovies(type, page)
        .then(movies =>{
            res.status(200).json({
                movies
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/ovas/:type/:page' , (req, res) =>{

    let type = req.params.type;
    let page = req.params.page;

    api.getOvas(type, page)
        .then(ovas =>{
            res.status(200).json({
                ovas
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/specials/:type/:page' , (req, res) =>{

    let type = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, page)
        .then(specials =>{
            res.status(200).json({
                specials
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/tv/:type/:page' , (req, res) =>{

    let type = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, page)
        .then(tv =>{
            res.status(200).json({
                tv
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/moreInfo/:title' , (req, res) =>{

    let title = req.params.title;

    api.getMoreInfo(title)
        .then(info =>{
            if (info.length > 0) {
                res.status(200).json({
                    info
                });
            } else { res.status(404) }
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/getAnimeServers/:id([^/]+/[^/]+)' , (req, res) =>{

    let id = req.params.id;

    api.getAnimeServers(id)
        .then(servers =>{
            res.status(200).json({
                servers
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/search/:title' , (req, res) =>{

    let title = req.params.title;

    api.search(title)
        .then(search =>{
            res.status(200).json({
                search
            });
        }).catch((err) =>{
        console.error(err);
    });

});

module.exports = router;
