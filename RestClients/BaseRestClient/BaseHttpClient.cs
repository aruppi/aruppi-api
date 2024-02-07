using System.Net;
using System.Net.Http.Json;
using System.Text;

namespace BaseRestClient;

public abstract class BaseHttpClient
{
    protected virtual string BaseUrl { get; set; }
    protected virtual int AuthRetryCount { get; set; } = 0;

    private readonly HttpClient HttpClient = null!;
    private readonly IHttpClientFactory IHttpClientFactory = null!;

    public BaseHttpClient(IHttpClientFactory IHttpClientFactory)
    {
        if (IHttpClientFactory is null)
        {
            throw new NullReferenceException(nameof(IHttpClientFactory));
        }
        this.IHttpClientFactory = IHttpClientFactory;
    }

    public BaseHttpClient(HttpClient HttpClient)
    {
        if (HttpClient is null)
        {
            throw new NullReferenceException(nameof(HttpClient));
        }
        this.HttpClient = HttpClient;
    }

    protected abstract void SetupHttpClient(HttpClient httpClient);

    protected async Task<TResponse?> SendAsync<TResponse>(RequestConfig requestConfiguration, CancellationToken cancellationToken)
    {
        var response = await SendAsync(requestConfiguration, cancellationToken);

        var result = await response.ReadFromJsonAsync<TResponse>(cancellationToken);

        return result;
    }

    protected async Task<HttpContent> SendAsync(RequestConfig requestConfiguration, CancellationToken cancellationToken)
    {
        var localRetryCount = AuthRetryCount;

        var client = GetHttpClient();

    retry:

        SetupHttpClient(client);

        // HttpRequestMessage cannot be reused, without rebuilding it ourselves we can not handle retry logic automatically
        var message = requestConfiguration.BuildMessage();

        message.RequestUri ??= GetUri(requestConfiguration.Endpoint, requestConfiguration.QueryParams);

        var response = await client.SendAsync(message, cancellationToken);

        if (response.StatusCode == HttpStatusCode.Unauthorized)
        {
            if (localRetryCount > 0)
            {
                localRetryCount--;
                SetupHttpClient(client);
                goto retry;
            }
        }

        response.EnsureSuccessStatusCode();

        return response.Content;
    }

    private Uri GetUri(string? endpoint, List<(string Key, dynamic Value)>? param = null)
    {
        var uriBuilder = new StringBuilder();

        uriBuilder.Append(BaseUrl);

        endpoint ??= "";

        uriBuilder.Append(endpoint);

        if (param is not null && param.Count > 0)
        {
            uriBuilder.Append('?');

            UrlEncode(uriBuilder, param);
        }

        var fullUrl = uriBuilder.ToString();

        var uri = new Uri(fullUrl);

        return uri;
    }

    private void UrlEncode(StringBuilder sb, List<(string Key, dynamic Value)>? param = null)
    {
        for (int i = 0; i < param.Count; i++)
        {
            var pair = param[i];

            if (string.IsNullOrWhiteSpace(pair.Key))
            {
                continue;
            }

            if (i != 0)
            {
                sb.Append('&');
            }

            sb.Append(pair.Key);

            if (string.IsNullOrWhiteSpace(pair.Key))
            {
                continue;
            }

            sb.Append('=');

            sb.Append(pair.Value);
        }
    }

    private HttpClient GetHttpClient()
    {
        if (HttpClient is not null)
        {
            return HttpClient;
        }
        return IHttpClientFactory.CreateClient();
    }
}