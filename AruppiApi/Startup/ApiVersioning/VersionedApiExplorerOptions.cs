using Asp.Versioning.ApiExplorer;
using Microsoft.Extensions.Options;

namespace AruppiApi.Startup.ApiVersioning;

public class VersionedApiExplorerOptions : IConfigureNamedOptions<ApiExplorerOptions>
{
    public void Configure(string? name, ApiExplorerOptions options)
    {
        Configure(options);
    }

    public void Configure(ApiExplorerOptions options)
    {
        // add the versioned api explorer, which also adds IApiVersionDescriptionProvider service
        // note: the specified format code will format the version as "'v'major[.minor][-status]"
        options.GroupNameFormat = "'v'VVV";

        // note: this option is only necessary when versioning by url segment. the SubstitutionFormat
        // can also be used to control the format of the API version in route templates
        options.SubstituteApiVersionInUrl = true;
    }
}
