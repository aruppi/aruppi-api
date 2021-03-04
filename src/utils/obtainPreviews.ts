import urls from './urls';

export const obtainPreviewNews = (encoded: string) => {
  let image: string;

  try {
    if (encoded.includes('src="https://img1.ak.crunchyroll.com/')) {
      if (
        encoded.split('https://img1.ak.crunchyroll.com/')[1].includes('.jpg')
      ) {
        image = `https://img1.ak.crunchyroll.com/${
          encoded.split('https://img1.ak.crunchyroll.com/')[1].split('.jpg')[0]
        }.jpg`;
      } else {
        image = `https://img1.ak.crunchyroll.com/${
          encoded.split('https://img1.ak.crunchyroll.com/')[1].split('.png')[0]
        }.png`;
      }
    } else if (encoded.includes('<img title=')) {
      image = encoded
        .substring(encoded.indexOf('<img title="'), encoded.indexOf('" alt'))
        .split('src="')[1];
    } else if (encoded.includes('<img src=')) {
      image = encoded
        .substring(encoded.indexOf('<img src="'), encoded.indexOf('" alt'))
        .substring(10)
        .replace('http', 'https')
        .replace('httpss', 'https');
    } else if (encoded.includes('<img')) {
      image = encoded
        .split('src=')[1]
        .split(' class=')[0]
        .replace('"', '')
        .replace('"', '');
    } else if (encoded.includes('https://www.youtube.com/embed/')) {
      let getSecondThumb = encoded
        .split('https://www.youtube.com/embed/')[1]
        .split('?feature')[0];
      image = `https://img.youtube.com/vi/${getSecondThumb}/0.jpg`;
    } else if (encoded.includes('https://www.dailymotion.com/')) {
      let getDailymotionThumb = encoded
        .substring(encoded.indexOf('" src="'), encoded.indexOf('" a'))
        .substring(47);
      image = `https://www.dailymotion.com/thumbnail/video/${getDailymotionThumb}`;
    } else {
      let number = Math.floor(Math.random() * 30);
      image = `${urls.BASE_ARUPPI}news/${number}.png`;
    }

    return image;
  } catch (err) {
    console.log(err);
  }
};
