using Asp.Versioning;
using Microsoft.Extensions.Options;

namespace AruppiApi.Startup.ApiVersioning;

public class ConfiguredApiVersioningOptions : IConfigureNamedOptions<ApiVersioningOptions>
{
    public void Configure(string? name, ApiVersioningOptions options)
    {
        Configure(options);
    }

    public void Configure(ApiVersioningOptions options)
    {
        options.DefaultApiVersion = ApiVersioning.DefaultApiVersion;
        options.AssumeDefaultVersionWhenUnspecified = true;
        options.ReportApiVersions = true;

        var possibleApiVersionLocations = new IApiVersionReader[]
        {
            new UrlSegmentApiVersionReader(),
            //new HeaderApiVersionReader("x-api-version"),
            //new MediaTypeApiVersionReader("x-api-version"),
        };
        options.ApiVersionReader = ApiVersionReader.Combine(possibleApiVersionLocations);
    }
}
