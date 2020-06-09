const express = require('express');
const router = express.Router();
const api = require('../api');

const { BASE_KUDASAI, BASE_PALOMITRON, BASE_RAMENPARADOS, BASE_CRUNCHYROLL } = require('../urls');

router.get('/schedule/:day' , (req, res) =>{

    let day = {current: req.params.day}

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

    let pagesRss = [
        { url: BASE_KUDASAI,        author: 'Kudasai',          content: 'content_encoded'  },
        { url: BASE_PALOMITRON,     author: 'Palomitron',       content: 'description'      },
        { url: BASE_RAMENPARADOS,   author: 'Ramen para dos',   content: 'content'          },
        { url: BASE_CRUNCHYROLL,    author: 'Crunchyroll',      content: 'content_encoded'  }
    ];

    api.getNews(pagesRss)
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

    let type = {url: 'Movies', prop: 'movies'}
    let subType = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, subType, page)
        .then(movies =>{
            res.status(200).json({
                movies
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/ovas/:type/:page' , (req, res) =>{

    let type = {url: 'Ova', prop: 'ova'}
    let subType = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, subType, page)
        .then(ovas =>{
            res.status(200).json({
                ovas
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/specials/:type/:page' , (req, res) =>{

    let type = {url: 'Special', prop: 'special'}
    let subType = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, subType, page)
        .then(specials =>{
            res.status(200).json({
                specials
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/tv/:type/:page' , (req, res) =>{

    let type = {url: 'Tv', prop: 'tv'}
    let subType = req.params.type;
    let page = req.params.page;

    api.getSpecials(type, subType, page)
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
            res.status(200).json({
                info
            });
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

router.get('/images/:query' , (req, res) =>{

    let query = { title: req.params.query, count: '51', type: 'images', safesearch: '1', country: 'es_ES', uiv: '4'  };

    api.getImages(query)
        .then(images =>{
            res.status(200).json({
                images
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/videos/:channelId' , (req, res) =>{

    let channelId = { id: req.params.channelId, part: 'snippet,id', order: 'date', maxResults: '50', prop: 'items'  };

    api.getYoutubeVideos(channelId)
        .then(videos =>{
            res.status(200).json({
                videos
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/radio' , (req, res) =>{

    api.getRadioStations()
        .then(stations =>{
            res.status(200).json({
                stations
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/themes/:title' , (req, res) =>{

    let title = req.params.title;

    api.getOpAndEd(title)
        .then(themes =>{
            res.status(200).json({
                themes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/themeSeason/:year/:season?', (req, res) =>{

    let year = req.params.year;
    let season = req.params.season

    api.getThemesSeason(year, season)
        .then(themes =>{
            res.status(200).json({
                themes
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/randomTheme', (req, res) =>{

    api.getRandomTheme()
        .then(random =>{
            res.status(200).json({
                random
            });
        }).catch((err) =>{
        console.error(err);
    });

});

router.get('/artists/:id?', (req, res) =>{

    let id = req.params.id;

    api.getArtist(id)
        .then(artists =>{
            res.status(200).json({
                artists
            });
        }).catch((err) =>{
        console.error(err);
    });

});

module.exports = router;
