import got from 'got';
import cheerio from 'cheerio';
import { CookieJar } from 'tough-cookie';

const cookieJar = new CookieJar();

interface Options {
  scrapy: boolean;
  parse: boolean;
}

export const requestGot = async (
  url: string,
  options?: Options,
): Promise<any> => {
  if (options !== undefined) {
    if (options.scrapy) {
      const response = await got(url, { cookieJar });
      return await cheerio.load(response.body);
    }

    if (options.parse) {
      return await got(url, { cookieJar }).json();
    }
  } else {
    return await got.get(url, { cookieJar });
  }
};
