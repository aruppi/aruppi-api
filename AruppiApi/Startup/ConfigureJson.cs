using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using System.Text.Json.Serialization;

namespace AruppiApi.Startup;

public static class Json
{
    public static void ConfigureJson(this IServiceCollection services)
    {
        services.ConfigureOptions<ConfigureJsonOptions>();
    }
}

public class ConfigureJsonOptions : IConfigureNamedOptions<JsonOptions>
{
    public void Configure(string? name, JsonOptions options)
    {
        Configure(options);
    }

    public void Configure(JsonOptions options)
    {
        var so = options.JsonSerializerOptions;
        // Sane Defaults
        so.Converters.Add(new JsonStringEnumConverter());
        so.ReferenceHandler = ReferenceHandler.IgnoreCycles;
        so.MaxDepth = 128;
    }
}
