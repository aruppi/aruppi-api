import { requestGot } from './requestCall';

export const transformUrlServer = async (urlReal: any) => {
  for (const data of urlReal) {
    if (data.server === 'amus' || data.server === 'natsuki') {
      let res = await requestGot(data.code.replace('embed', 'check'), {
        parse: true,
        scrapy: false,
      });
      data.code = res.file || null;
      data.direct = true;
    }
  }

  return urlReal.map((item: any) => {
    return {
      id: item.title.toLowerCase(),
      url: item.code,
      direct: item.direct || false,
    };
  });
};
