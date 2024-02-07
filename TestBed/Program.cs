using JikanClient;
using JikanClient.Models.Request;
using JikanClient.Models.Response;
using MonosScrapper;
using Riok.Mapperly.Abstractions;
using TioScrapper;

namespace TestBed;

internal class Program
{
    static async Task Main(string[] args)
    {
        //var url = "https://tioanime.com/ver/one-piece-tv-1083";

        //var sources = Tio.GetSourcesFromUrl(url);

        //var url2 = "https://monoschinos2.com/ver/one-piece-episodio-1";

        //var sources2 = Monos.GetSourcesFromUrl(url2);

        var test = new JikanApiClient(new HttpClient());

        var request = new GetSchedulesRequest()
        {
            Day = Days.Monday,
        };

        var data = await test.SchedulesGet(request);

        var slim = data.Data.Select(i => i.ToDto());
    }
}

[Mapper]
public static partial class ScheduleMapper
{
    [MapProperty(nameof(@GetScheduleResponse.Images.Webp.LargeImageUrl), nameof(@ScheduleSlim.Image))]
    public static partial ScheduleSlim ToDto(this GetScheduleResponse src);
}

public class ScheduleSlim
{
    public long MalId { get; set; }
    public string Title { get; set; }
    public string Image { get; set; }
}