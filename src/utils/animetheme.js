const cheerio = require('cheerio');

const {
    homgot
} = require('../api/apiCall');

const {
    REDDIT_ANIMETHEMES
} = require('../api/urls');

class ThemeParser {

    constructor() {}

    async all() {
        try {
            this.animes = [];
            this.$ = await redditocall('year_index');
            return await this.parseLinks();
        }
        catch(err) {
            console.log(err);
        }
    }

    async allYears() {
        try {
            this.animes = [];
            this.$ = await redditocall('year_index');
            return await this.parseYears();
        }
        catch(err) {
            console.log(err);
        }
    }

    async serie(title) {
        try {
            this.animes = [];
            this.$ = await redditocall('anime_index');
            return await this.parseSerie(title);
        }
        catch(err) {
            console.log(err);
        }
    }

    async artists() {
        try {
            this.animes = [];
            this.$ = await redditocall('artist');
            return await this.parseArtists();
        }
        catch(err) {
            console.log(err);
        }
    }

    async artist(id) {
        try {
            this.animes = [];
            this.$ = await redditocall(`artist/${id}`);
            return await this.parseArtist();
        }
        catch(err) {
            console.log(err);
        }
    }

    async random(query) {
        try {
            this.animes = [];
            this.$ = await redditocall('anime_index');
            return await this.parseRandom(query);
        }
        catch(err) {
            console.log(err);
        }
    }

    async year(date) {
        let animes = [];

        this.$ = await redditocall(date);
        this.$('h3').each((index, element) => {
            let parsed = this.parseAnime(this.$(element));
            parsed.year = date;
            animes.push(parsed);
        });
        return animes;
    }

    parseRandom() {
        return new Promise(async resolve => {

            let data = this.$('p a');
            const origin = '1'
            let randomize = Math.round(Math.random()*((data.length-1)-origin)+parseInt(origin));

            this.$ = await redditocall(this.$('p a')[randomize].attribs.href.split('/r/AnimeThemes/wiki/')[1].split('#wiki')[0]);

            let rand = Math.round(Math.random()*this.$('h3').length - 1);
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
            let years = [];

            this.$('h3 a').each((index, element) => {
                years.push(
                    {
                        id: this.$(element).attr('href').split('/')[4],
                        name: this.$(element).text()
                    }
                );
            });

            resolve(years);
        });
    }

    parseArtists() {
        return new Promise(async resolve => {

            let promises = []
            let data = this.$('p a').filter(x => x > 0);

            for (let i = 0; i < data.length; i++) {

                promises.push({
                    id: data[i].children[0].parent.attribs.href.split('/')[5],
                    name: data[i].children[0].data
                })

                if (i === data.length - 1) {
                    resolve(promises)
                }

            }

        })
    }

    parseArtist(){
        return new Promise(async resolve => {

            let promises = []
            let data = this.$('h3');

            for (let i = 0; i < data.length; i++) {

                let parsed = await this.parseAnime(data[i])
                promises.push(parsed)

                if (i === data.length - 1) {
                    resolve(promises)
                }

            }

        })
    }

    /*  - ParseSerie
        Parse the HTML from the redditocall
        and search for the h3 tag to be the 
        same of the title and resolve a object.
    */
    parseSerie(title) {
        return new Promise(async resolve => {

            let data = this.$('p a');

            for (let i = 0; i < data.length; i++) {

                let serieElement = data[i].children[0].data;
            
                if (serieElement.split(" (")[0] === title) {

                    let year = this.$('p a')[i].attribs.href.split('/r/AnimeThemes/wiki/')[1].split('#wiki')[0];
                    this.$ = await redditocall(this.$('p a')[i].attribs.href.split('/r/AnimeThemes/wiki/')[1].split('#wiki')[0]);
                    
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

                await this.year(this.$(yearElement).attr('href').split('/')[4])
                    .then(async animes => {
                        this.animes = this.animes.concat(animes);
                        if(i === years.length - 1) {
                            resolve(this.animes);
                        }
                    })
            }

        })
    }

    /* - ParseAnime
        Parse the h3 tag and get the table
        for the next function to parse the table
        and get the information about the ending and
        openings.
    */
    parseAnime(element) {
        let el = this.$(element).find('a');
        let title = this.$(el).text();
        let mal_id = this.$(el).attr('href').split('/')[4];
        let next = this.$(element).next();

        let theme = {
            id: mal_id,
            title
        };

        if (this.$(next).prop("tagName") === "TABLE") {
            theme.themes = this.parseTable(this.$(next));
        }else if (this.$(next).prop("tagName") === "P") {
            theme.themes = this.parseTable(this.$(next).next());
        } 

        return theme;
    }

    /*  - ParseTable
        Parse the table tag from the HTML
        and returns a object with all the
        information.
    */
    parseTable(element) {

        if (this.$(element).prop('tagName') !== "TABLE") {
            return this.parseTable(this.$(element).next());
        }

        let themes = [];

        
        this.$(element).find('tbody').find('tr').each((i, elem) => {
            
            let name = replaceAll(this.$(elem).find('td').eq(0).text(), '&quot;', '"');
            let link = this.$(elem).find('td').eq(1).find('a').attr('href');
            let linkDesc = this.$(elem).find('td').eq(1).find('a').text();
            let episodes = this.$(elem).find('td').eq(2).text().length > 0 ? this.$(elem).find('td').eq(2).text() : "";
            let notes = this.$(elem).find('td').eq(3).text().length > 0 ? this.$(elem).find('td').eq(3).text() : "";

            themes.push({
                name,
                link,
                desc: linkDesc,
                type: name.startsWith('OP') ? 'Opening' : name.startsWith('ED') ? 'Ending' : 'OP/ED',
                episodes,
                notes
            });
        });

        return themes;
    }
}

async function redditocall(href) {
    let resp = await homgot(REDDIT_ANIMETHEMES + href + ".json", { parse: true });
    return cheerio.load(getHTML(resp.data.content_html));
}


function getHTML(str) {
    let html = replaceAll(str, "&lt;", "<");
    html = replaceAll(html, "&gt;", ">");
    return html;
}

function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}


module.exports = ThemeParser;
