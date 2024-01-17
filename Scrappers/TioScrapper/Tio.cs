using HtmlAgilityPack;
using ScrapperShared;
using System.Text.Json;

namespace TioScrapper;

public class Tio
{
    public static List<Source> GetSourcesFromUrl(string url)
    {
        var web = new HtmlWeb();
        var doc = web.Load(url);
        var nodes = doc.DocumentNode;
        var sourcesNode = nodes.SelectSingleNode("/html/body/script[15]");
        var sourcesRawText = sourcesNode.InnerText;

        var first = sourcesRawText.IndexOf('[');
        var last = sourcesRawText.LastIndexOf(']') + 1;

        var slice = sourcesRawText[first..last];

        var jsonDoc = JsonSerializer.Deserialize<JsonDocument>(slice);

        var root = jsonDoc.RootElement;

        var length = root.GetArrayLength();

        var sources = new List<Source>(length);

        for (var i = 0; i < length; i++)
        {
            var current = root[i];

            var source = new Source()
            {
                Server = current[0].GetString(),
                Url = current[1].GetString(),
            };

            sources.Add(source);
        }

        return sources;
    }
}