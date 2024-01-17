using Asp.Versioning.ApiExplorer;
using Microsoft.Extensions.Options;
using Swashbuckle.AspNetCore.SwaggerUI;

namespace AruppiApi.Startup.Swagger;

public class ConfiguredSwaggerUIOptions : IConfigureNamedOptions<SwaggerUIOptions>
{
    private readonly IApiVersionDescriptionProvider IApiVersionDescriptionProvider;
    private readonly Ui SwaggerUI;

    public ConfiguredSwaggerUIOptions(IApiVersionDescriptionProvider IApiVersionDescriptionProvider, IConfiguration IConfiguration)
    {
        SwaggerUI = IConfiguration.GetConfig<Ui>();
        this.IApiVersionDescriptionProvider = IApiVersionDescriptionProvider;
    }

    public void Configure(string? name, SwaggerUIOptions options)
    {
        Configure(options);
    }

    public void Configure(SwaggerUIOptions options)
    {
        options.OAuthClientId(SwaggerUI.ClientId);

        var newerToOlder = IApiVersionDescriptionProvider.ApiVersionDescriptions.Reverse();

        foreach (var description in newerToOlder)
        {
            options.SwaggerEndpoint($"/swagger/{description.GroupName}/swagger.json", description.GroupName.ToUpperInvariant());
        }

        options.EnableFilter();
        options.EnableTryItOutByDefault();
        options.DisplayRequestDuration();
        options.DocExpansion(DocExpansion.List);
        options.EnableDeepLinking();
        options.ShowCommonExtensions();
        options.ShowExtensions();
        options.DefaultModelRendering(ModelRendering.Example);
        options.DefaultModelsExpandDepth(0);
    }
}

public class Ui
{
    public string? ClientId { get; set; }
}
