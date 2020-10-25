const got = require('got');
const cheerio = require('cheerio');
const { CookieJar} = require('tough-cookie');
const cookieJar = new CookieJar();

let response
let data

const homgot = async (url, options) => {

    if (options !== undefined) {
        if (options.scrapy) {
            response = await got(url, { cookieJar })
            data = await cheerio.load(response.body)
        }
        if (options.parse) {
            data = await got(url, { cookieJar }).json()
        }
    } else {
        data = await got.get(url, { cookieJar });
    }

    return data

}

module.exports = {homgot}
