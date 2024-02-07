using JikanClient.Models.Response;
using JikanRest;
using JikanRest.Models;
using Microsoft.Kiota.Abstractions.Authentication;
using Microsoft.Kiota.Http.HttpClientLibrary;
using Riok.Mapperly.Abstractions;

namespace TestBed;

internal class Program
{
    static async Task Main(string[] args)
    {
        //var url = "https://tioanime.com/ver/one-piece-tv-1083";

        //var sources = Tio.GetSourcesFromUrl(url);

        //var url2 = "https://monoschinos2.com/ver/one-piece-episodio-1";

        //var sources2 = Monos.GetSourcesFromUrl(url2);

        //var test = new JikanApiClient(new HttpClient());

        //var request = new GetSchedulesRequest()
        //{
        //    Day = Days.Monday,
        //};

        //var data = await test.SchedulesGet(request);

        //var slim = data.Data.Select(i => i.ToDto());

        // API requires no authentication, so use the anonymous
        // authentication provider
        var authProvider = new AnonymousAuthenticationProvider();
        // Create request adapter using the HttpClient-based implementation
        var adapter = new HttpClientRequestAdapter(authProvider);
        // Create the API client
        var client = new JikanApi(adapter);


        var year = 1999;
        var season = "spring";
        var result = await client.Seasons[year][season].GetAsync(b =>
        {
        });
        
        Pagination_plus? data = await client.Schedules.GetAsync((requestBuilder) =>
        {
            requestBuilder.QueryParameters.Filter = JikanRest.Schedules.GetFilterQueryParameterType.Monday;
        });

        Console.WriteLine($"Retrieved {data.Pagination.Items.Count} posts.");
    }
}