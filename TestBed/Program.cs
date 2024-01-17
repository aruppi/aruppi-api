using MonosScrapper;
using TioScrapper;

namespace TestBed;

internal class Program
{
    static void Main(string[] args)
    {
        var url = "https://tioanime.com/ver/one-piece-tv-1083";

        var sources = Tio.GetSourcesFromUrl(url);

        var url2 = "https://monoschinos2.com/ver/one-piece-episodio-1";

        var sources2 = Monos.GetSourcesFromUrl(url2);
    }
}