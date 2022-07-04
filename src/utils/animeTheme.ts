import cheerio from 'cheerio';
import { requestGot } from './requestCall';
import urls from './urls';

export default class ThemeParser {
  animes: any[] = [];
  $: any = '';

  async all() {
    try {
      this.animes = [];
      this.$ = await redditocall('year_index');
      return await this.parseLinks();
    } catch (err) {
      console.log(err);
    }
  }

  async allYears() {
    try {
      this.animes = [];
      this.$ = await redditocall('year_index');
      return await this.parseYears();
    } catch (err) {
      console.log(err);
    }
  }

  async serie(title: string) {
    try {
      this.animes = [];
      this.$ = await redditocall('anime_index');
      return await this.parseSerie(title);
    } catch (err) {
      console.log(err);
    }
  }

  async artists() {
    try {
      this.animes = [];
      this.$ = await redditocall('artist');
      return await this.parseArtists();
    } catch (err) {
      console.log(err);
    }
  }

  async artist(id: string) {
    try {
      this.animes = [];
      this.$ = await redditocall(`artist/${id}`);
      return await this.parseArtist();
    } catch (err) {
      console.log(err);
    }
  }

  async random() {
    try {
      this.animes = [];
      this.$ = await redditocall('anime_index');
      return await this.parseRandom();
    } catch (err) {
      console.log(err);
    }
  }

  async year(date: string) {
    let animes: any = [];

    this.$ = await redditocall(date);
    this.$('h3').each((index: number, element: cheerio.Element) => {
      let parsed = this.parseAnime(this.$(element));
      parsed.year = date;
      animes.push(parsed);
    });
    return animes;
  }

  parseRandom() {
    return new Promise(async resolve => {
      let data = this.$('p a');
      const origin: any = '1';
      let randomize = Math.round(
        Math.random() * (data.length - 1 - origin) + parseInt(origin),
      );

      this.$ = await redditocall(
        this.$('p a')
          [randomize].attribs.href.split('/r/AnimeThemes/wiki/')[1]
          .split('#wiki')[0],
      );

      let rand = Math.round(Math.random() * this.$('h3').length - 1);
      let parsed = this.parseAnime(this.$('h3')[rand]);
      resolve(parsed);
    });
  }

  /*  -ParseYears
      Get the data from the year
      get the name and the id to do the respective
      scrapping.
  */
  parseYears() {
    return new Promise(async resolve => {
      let years: any[] = [];

      this.$('h3 a').each((index: number, element: cheerio.Element) => {
        years.push({
          id: this.$(element).attr('href').split('/')[4],
          name: this.$(element).text(),
        });
      });

      resolve(years);
    });
  }

  parseArtists() {
    return new Promise(async resolve => {
      let promises = [];
      let data = this.$('p a').filter((x: any) => x > 0);

      for (let i = 0; i < data.length; i++) {
        promises.push({
          id: data[i].children[0].parent.attribs.href.split('/')[5],
          name: data[i].children[0].data,
        });

        if (i === data.length - 1) {
          resolve(promises);
        }
      }
    });
  }

  parseArtist() {
    return new Promise(async resolve => {
      let promises = [];
      let data = this.$('h3');

      for (let i = 0; i < data.length; i++) {
        let parsed = await this.parseAnime(data[i]);
        promises.push(parsed);

        if (i === data.length - 1) {
          resolve(promises);
        }
      }
    });
  }

  /*  - ParseSerie
      Parse the HTML from the redditocall
      and search for the h3 tag to be the 
      same of the title and resolve a object.
  */
  parseSerie(title: string) {
    return new Promise(async resolve => {
      let data = this.$('p a');

      for (let i = 0; i < data.length; i++) {
        let serieElement = data[i].children[0].data;

        if (serieElement.split(' (')[0] === title) {
          let year = this.$('p a')
            [i].attribs.href.split('/r/AnimeThemes/wiki/')[1]
            .split('#wiki')[0];
          this.$ = await redditocall(
            this.$('p a')
              [i].attribs.href.split('/r/AnimeThemes/wiki/')[1]
              .split('#wiki')[0],
          );

          for (let i = 0; i < this.$('h3').length; i++) {
            if (this.$('h3')[i].children[0].children[0].data === title) {
              let parsed = this.parseAnime(this.$('h3')[i]);
              parsed.year = year;
              resolve(parsed);
            }
          }
        }
      }
    });
  }

  parseLinks() {
    return new Promise(async resolve => {
      let years = this.$('h3 a');

      for (let i = 0; i < years.length; i++) {
        let yearElement = years[i];

        await this.year(this.$(yearElement).attr('href').split('/')[4]).then(
          async animes => {
            this.animes = this.animes.concat(animes);
            if (i === years.length - 1) {
              resolve(this.animes);
            }
          },
        );
      }
    });
  }

  /* - ParseAnime
      Parse the h3 tag and get the table
      for the next function to parse the table
      and get the information about the ending and
      openings.
  */
  parseAnime(element: cheerio.Element) {
    let el = this.$(element).find('a');
    let title = this.$(el).text();
    let mal_id = this.$(el).attr('href').split('/')[4];
    let next = this.$(element).next();

    let theme: any = {
      id: mal_id,
      title,
    };

    if (this.$(next).prop('tagName') === 'TABLE') {
      theme.themes = this.parseTable(this.$(next));
    } else if (this.$(next).prop('tagName') === 'P') {
      theme.themes = this.parseTable(this.$(next).next());
    }

    return theme;
  }

  /*  - ParseTable
      Parse the table tag from the HTML
      and returns a object with all the
      information.
  */
  parseTable(element: cheerio.Element): any {
    if (this.$(element).prop('tagName') !== 'TABLE') {
      return this.parseTable(this.$(element).next());
    }

    let themes: any = [];

    this.$(element)
      .find('tbody')
      .find('tr')
      .each((index: number, element: cheerio.Element) => {
        let name = replaceAll(
          this.$(element).find('td').eq(0).text(),
          '&quot;',
          '"',
        );
        let link = this.$(element).find('td').eq(1).find('a').attr('href');
        let linkDesc = this.$(element).find('td').eq(1).find('a').text();
        let episodes =
          this.$(element).find('td').eq(2).text().length > 0
            ? this.$(element).find('td').eq(2).text()
            : '';
        let notes =
          this.$(element).find('td').eq(3).text().length > 0
            ? this.$(element).find('td').eq(3).text()
            : '';

        themes.push({
          name,
          link,
          desc: linkDesc,
          type: name.startsWith('OP')
            ? `OP${name[2]}`
            : name.startsWith('ED')
            ? `ED${name[2]}`
            : 'OP/ED',
          episodes,
          notes,
        });
      });

    return themes;
  }
}

async function redditocall(href: string) {
  const resp = await requestGot(urls.REDDIT_ANIMETHEMES + href + '.json', {
    parse: true,
    scrapy: false,
    spoof: true,
  });

  return cheerio.load(getHTML(resp.data.content_html));
}

function getHTML(str: string) {
  let html = replaceAll(str, '&lt;', '<');
  html = replaceAll(html, '&gt;', '>');
  return html;
}

function replaceAll(str: string, find: string, replace: string) {
  return str.replace(new RegExp(find, 'g'), replace);
}
