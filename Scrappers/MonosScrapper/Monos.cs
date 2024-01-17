using System.Text;
using HtmlAgilityPack;
using ScrapperShared;

namespace MonosScrapper;

public class Monos
{
    public static List<Source> GetSourcesFromUrl(string url)
    {
        var web = new HtmlWeb();
        var doc = web.Load(url);
        var nodes = doc.DocumentNode;
        var sourcesNode = nodes.SelectSingleNode("/html/body/section[3]/div/div[1]");

        var childs = sourcesNode.Descendants();

        var sources = new List<Source>();

        foreach (var child in childs)
        {
            if (child.Name != "p")
            {
                continue;
            }

            var base64Url = child.Attributes.First(attr => attr.Name == "data-player");

            var bytes = Convert.FromBase64String(base64Url.Value);

            var sourceUrl = Encoding.UTF8.GetString(bytes);

            var source = new Source() 
            {
                Server = child.InnerText,
                Url = sourceUrl,
            };

            sources.Add(source);
        }

        return sources;
    }
}