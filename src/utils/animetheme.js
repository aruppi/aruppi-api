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
        this.$('h3').each((i, el) => {
            let parsed = this.parseAnime(el);
            parsed.year = date;
            animes.push(parsed);
        })
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

    parseYears() {
        return new Promise(async resolve => {

            let promises = [];
            let data = this.$('h3 a');

            for (let i = 0; i < data.length; i++) {

                promises.push({
                    id: data[i].children[0].parent.attribs.href.split('/')[4],
                    name: data[i].children[0].data
                })

                if (i === data.length - 1) {
                    resolve(promises);
                }

            }

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
            this.$('h3 a')[0].children[0].data

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

    parseAnime(element) {
        let el = this.$(element).children('a');
        let title = el.text();
        let mal_id = el.attr('href').split('/')[4];
        let next = this.$(element).next();

        let theme = {
            id: mal_id,
            title
        };


        if (next.prop("tagName") === "TABLE") {

            theme.themes = this.parseTable(next);

        }else if (next.prop("tagName") === "P") {
            
            theme.themes = this.parseTable(next.next());

        } 


        return theme;
    }

    /*  - ParseTable
        Parse the table tag from the HTML
        and returns a object with all the
        information.
    */

    parseTable(element) {

        if (element.prop('tagName') !== "TABLE") {
            return this.parseTable(element.next());
        }

        let themes = [];

        
        element.find('tbody').find('tr').each((i, elem) => {

            let name = replaceAll(elem.children[1].children[0].data, "&quot;", "\"");
            let link = elem.children[3].children[0].attribs.href;
            let linkDesc = elem.children[3].children[0].children[0].data;
            let episodes = elem.children[5].children.length > 0 ? elem.children[5].children[0].data : "";
            let notes = elem.children[7].children.length > 0 ? elem.children[7].children[0].data : "";
            
            

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
