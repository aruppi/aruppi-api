using Asp.Versioning.ApiExplorer;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace AruppiApi.Startup.ApiVersioning;

public class VersionedSwaggerGenOptions : IConfigureNamedOptions<SwaggerGenOptions>
{
    private readonly IApiVersionDescriptionProvider ApiVersionProvider;

    public VersionedSwaggerGenOptions(IApiVersionDescriptionProvider provider)
    {
        ApiVersionProvider = provider;
    }

    public void Configure(string? name, SwaggerGenOptions options)
    {
        Configure(options);
    }

    public void Configure(SwaggerGenOptions options)
    {
        // add swagger document for every API version discovered
        foreach (var description in ApiVersionProvider.ApiVersionDescriptions)
        {
            options.SwaggerDoc(name: description.GroupName, info: CreateVersionInfo(description));
        }
    }

    private static OpenApiInfo CreateVersionInfo(ApiVersionDescription desc)
    {
        var info = new OpenApiInfo()
        {
            Title = $"Template API",
            Version = desc.ApiVersion.ToString(),
        };

        if (desc.IsDeprecated)
        {
            info.Description += " This API version has been deprecated. Please use one of the new APIs available from the explorer.";
        }

        return info;
    }
}
