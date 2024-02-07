using BaseRestClient;
using JikanClient.Models.Request;
using JikanClient.Models.Response;

namespace JikanClient;

public class JikanApiClient : BaseHttpClient
{
    protected override int AuthRetryCount { get; set; } = 0;

    protected override string BaseUrl { get; set; } = "https://api.jikan.moe/v4";

    public JikanApiClient(IHttpClientFactory IHttpClientFactory) : base(IHttpClientFactory) { }

    public JikanApiClient(HttpClient HttpClient) : base(HttpClient) { }

    protected override void SetupHttpClient(HttpClient httpClient) { }

    public async Task<Page<GetScheduleResponse>> SchedulesGet(GetSchedulesRequest request, CancellationToken cancellationToken = default)
    {
        const string Endpoint = "/schedules";

        var config = new RequestConfig
        {
            Endpoint = Endpoint,
            QueryParams = request.ToQueryData(),
            MessageBuilder = () =>
            {
                var message = new HttpRequestMessage()
                {
                    Method = HttpMethod.Get,
                };

                return message;
            },
        };

        var result = await SendAsync<Page<GetScheduleResponse>>(config, cancellationToken);

        return result!;
    }
}