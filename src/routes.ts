import { Router, Request, Response, NextFunction } from 'express';
import AnimeController from './controllers/AnimeController';
import DirectoryController from './controllers/DirectoryController';
import UtilsController from './controllers/UtilsController';

const routes = Router();
const animeController = new AnimeController();
const directoryController = new DirectoryController();
const utilsController = new UtilsController();

routes.get('/', (req: Request, res: Response) => {
  // We dont want to enforce the redirect storing, so just check
  res.set('Cache-Control', 'no-cache,proxy-revalidate');
  res.redirect('/api/v4/');
});

/*
  Routes - JSON
  Message with the JSON of all the routes in the
  /api, parameters and some examples, how to call the
  endpoints of the /api.
*/

routes.get('/api/v4/', (req: Request, res: Response) => {
  res.set('Cache-Control', 'no-store');
  res.json({
    message: 'Aruppi /api - 🎏',
    author: 'Jéluchu',
    version: '4.1.6',
    credits: 'The bitch loves /apis that offers data to Aruppi App',
    entries: [
      {
        Schedule: '/api/v4/schedule/:day',
        Top: '/api/v4/top/:type/:page/:subtype',
        AllAnimes: '/api/v4/allAnimes',
        RandomAnime: '/api/v4/randomAnime',
        Anitakume: '/api/v4/anitakume',
        News: '/api/v4/news',
        Season: '/api/v4/season/:year/:type',
        'All Seasons': '/api/v4/allSeasons',
        'All Directory': '/api/v4/allDirectory/:type',
        Genres: '/api/v4/getByGenres/:genre?/:order?/:page?',
        'Futures Seasons': '/api/v4/laterSeasons',
        LastEpisodes: '/api/v4/lastEpisodes',
        Movies: '/api/v4/movies/:type/:page',
        Ovas: '/api/v4/ova/:type/:page',
        Specials: '/api/v4/special/:type/:page',
        Tv: '/api/v4/tv/:type/:page',
        MoreInfo: '/api/v4/moreInfo/:title',
        GetEpisodes: '/api/v4/getEpisodes/:title',
        GetAnimeServers: '/api/v4/getAnimeServers/:id',
        Search: '/api/v4/search/:title',
        Images: '/api/v4/images/:query',
        Videos: '/api/v4/videos/:channelId',
        Playlist: '/api/v4/playlistVideos/:playlistId',
        'Type Videos': '/api/v4/sectionedVideos/:type',
        Radios: '/api/v4/radio',
        'All Themes': '/api/v4/allThemes',
        Themes: '/api/v4/themes/:title',
        'Year Themes': '/api/v4/themesYear/:year?',
        'Random Theme': '/api/v4/randomTheme',
        'Artists Theme': '/api/v4/artists/:id?',
        'Famous Platforms': '/api/v4/destAnimePlatforms',
        'Legal Platforms': '/api/v4/platforms/:id?',
      },
    ],
  });
});

/* Routes of the app below */

/* Anime Controller */
routes.get('/api/v4/schedule/:day', animeController.schedule);
routes.get('/api/v4/top/:type/:subtype?/:page', animeController.top);
routes.get('/api/v4/allAnimes', animeController.getAllAnimes);
routes.get('/api/v4/lastEpisodes', animeController.getLastEpisodes);
routes.get('/api/v4/movies/:type/:page', animeController.getContentMovie);
routes.get('/api/v4/ova/:type/:page', animeController.getContentOva);
routes.get('/api/v4/special/:type/:page', animeController.getContentSpecial);
routes.get('/api/v4/tv/:type/:page', animeController.getContentTv);
routes.get('/api/v4/getEpisodes/:title', animeController.getEpisodes);
routes.get(
  '/api/v4/getAnimeServers/:id([^/]+/[^/]+)',
  animeController.getServers,
);
routes.get('/api/v4/randomAnime', animeController.getRandomAnime);

/* Directory Controller */
routes.get(
  '/api/v4/allDirectory/:genres?',
  directoryController.getAllDirectory,
);
routes.get('/api/v4/season/:year/:type', directoryController.getSeason);
routes.get('/api/v4/allSeasons', directoryController.allSeasons);
routes.get('/api/v4/laterSeasons', directoryController.laterSeasons);
routes.get('/api/v4/moreInfo/:title', directoryController.getMoreInfo);
routes.get('/api/v4/search/:title', directoryController.search);
routes.get(
  '/api/v4/getByGenres/:genre?/:order?/:page?',
  directoryController.getAnimeGenres,
);

/* Utils Controller */
routes.get('/api/v4/anitakume', utilsController.getAnitakume);
routes.get('/api/v4/news', utilsController.getNews);
routes.get('/api/v4/images/:title', utilsController.getImages);
routes.get('/api/v4/videos/:channelId', utilsController.getVideos);
routes.get('/api/v4/playlistVideos/:playlistId', utilsController.getPlaylists);
routes.get('/api/v4/sectionedVideos/:type', utilsController.getSectionVideos);
routes.get('/api/v4/radio', utilsController.getRadioStations);
routes.get('/api/v4/allThemes', utilsController.getAllThemes);
routes.get('/api/v4/themes/:title', utilsController.getOpAndEd);
routes.get('/api/v4/themesYear/:year?', utilsController.getThemesYear);
routes.get('/api/v4/randomTheme', utilsController.randomTheme);
routes.get('/api/v4/artists/:id?', utilsController.getArtist);
routes.get('/api/v4/destAnimePlatforms', utilsController.getDestAnimePlatforms);
routes.get('/api/v4/platforms/:id?', utilsController.getPlatforms);
routes.get('/api/v4/generateWaifu/', utilsController.getWaifuRandom);

/* Routes to handling the v3 deprecated */
routes.get('/api/v3/*', (req: Request, res: Response, next: NextFunction) => {
  res.status(302).redirect('/api/v3');
});

routes.get('/api/v3', (req: Request, res: Response, next: NextFunction) => {
  res.status(200).json({
    message:
      'Sorry, version v3 is not avaiable, if you want to see content go to v4',
  });
});

/* Routes to handling the v2 deprecated */
routes.get('/api/v2/*', (req: Request, res: Response, next: NextFunction) => {
  res.status(302).redirect('/api/v2');
});

routes.get('/api/v2', (req: Request, res: Response, next: NextFunction) => {
  res.status(200).json({
    message:
      'Sorry, version v2 is not avaiable, if you want to see content go to v4',
  });
});

export default routes;
