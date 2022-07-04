import got from 'got';
import cheerio from 'cheerio';
import { CookieJar } from 'tough-cookie';
// @ts-ignore
import * as got_pjson from 'got/package.json'
const pjson = require('../../package.json');

const cookieJar = new CookieJar();
const aruppi_options: any = {
    cookieJar,
    'headers': {
        'user-agent': `Aruppi-API/${pjson.version} ${got_pjson.name}/${got_pjson.version}`,
        'x-client': 'aruppi-api'
    },

};

interface Options {
  scrapy?:  boolean,
  parse?:   boolean,
  spoof?:   boolean
}

export const requestGot = async (
    url: string,
    options?: Options,
): Promise<any> => {
    const got_options: any = {...got.defaults.options, ...aruppi_options}
    if (options) {
        if (options.spoof != null) {
            got_options.headers["user-agent"] = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:71.0) Gecko/20100101 Firefox/69.0";
            delete got_options.headers['x-client'];
            if (!options.spoof)
                got_options.headers['user-agent'] = got.defaults.options.headers['user-agent'];
        } else if (process.env.ALPI_KEY && (new URL(url)).hostname.match(/\.jeluchu\.xyz$/)) {
            got_options.headers['x-aruppi-key'] = process.env.ALPI_KEY;
        }

        if (options.scrapy) {
            const response = await got(url, got_options);
            return cheerio.load(response.body);
        }

        if (options.parse) {
            got_options.responseType = 'json';
            const response = await got(url, got_options);
            return response.body;
        }
    }
    const response = await got.get(url, got_options);
    return response;
};
