import { Router, Request, Response } from 'express';
import AnimeController from './controllers/AnimeController';

const routes = Router();
const animeController = new AnimeController();

routes.get('/', (req: Request, res: Response) => {
  // We dont want to enforce the redirect storing, so just check
  res.set('Cache-Control', 'no-cache,proxy-revalidate');
  res.redirect('/api/');
});

routes.get('/api/', (req: Request, res: Response) => {
  res.set('Cache-Control', 'no-store');
  res.json({
    message: 'Aruppi API - 🎏',
    author: 'Jéluchu',
    version: '4.0.0',
    credits: 'The bitch loves APIs that offers data to Aruppi App',
    entries: [
      {
        Schedule: '/api/schedule/:day',
        Top: '/api/top/:type/:page/:subtype',
        AllAnimes: '/api/ allAnimes',
        RandomAnime: '/api/randomAnime',
        Anitakume: '/api/anitakume',
        News: '/api/news',
        Season: '/api/season/:year/:type',
        'All Seasons': '/api/allSeasons',
        'All Directory': '/api/allDirectory/:type',
        Genres: '/api/getByGenres/:genre?/:order?/:page?',
        'Futures Seasons': '/api/laterSeasons',
        LastEpisodes: '/api/lastEpisodes',
        Movies: '/api/movies/:type/:page',
        Ovas: '/api/ova/:type/:page',
        Specials: '/api/special/:type/:page',
        Tv: '/api/tv/:type/:page',
        MoreInfo: '/api/moreInfo/:title',
        GetEpisodes: '/api/getEpisodes/:title',
        GetAnimeServers: '/api/getAnimeServers/:id',
        Search: '/api/search/:title',
        Images: '/api/images/:query',
        Videos: '/api/videos/:channelId',
        'Type Videos': '/api/sectionedVideos/:type',
        Radios: '/api/radio',
        'All Themes': '/api/allThemes',
        Themes: '/api/themes/:title',
        'Year Themes': '/api/themesYear/:year?',
        'Random Theme': '/api/randomTheme',
        'Artists Theme': '/api/artists/:id?',
        'Famous Platforms': '/api/destAnimePlatforms',
        'Legal Platforms': '/api/platforms/:id?',
      },
    ],
  });
});

routes.get('/api/schedule/:day', animeController.schedule);
routes.get('/api/top/:type/:page/:subtype?/', animeController.top);
routes.get('/api/allAnimes', animeController.getAllAnimes);

export default routes;
