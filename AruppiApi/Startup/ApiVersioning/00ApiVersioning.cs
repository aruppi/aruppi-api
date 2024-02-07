using Asp.Versioning;
using AruppiApi.Api.v5;

namespace AruppiApi.Startup.ApiVersioning;

// https://github.com/dotnet/aspnet-api-versioning/wiki/
public static class ApiVersioning
{
    public static ApiVersion DefaultApiVersion => ApiV5Attribute.Version;

    public static void AddApiVersioningConfigured(this IServiceCollection services)
    {
        services.ConfigureOptions<ConfiguredApiVersioningOptions>();

        services.ConfigureOptions<VersionedApiExplorerOptions>();

        services.ConfigureOptions<VersionedSwaggerGenOptions>();

        var builder = services.AddApiVersioning();

        builder.AddMvc();

        builder.AddApiExplorer();
    }
}
