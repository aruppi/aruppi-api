using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;

namespace AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

public class ConfiguredAuthenticationOptions : IConfigureNamedOptions<AuthenticationOptions>
{
    public const string DefaultSchemaId = "SampleApiKey";
    public void Configure(string? name, AuthenticationOptions options)
    {
        Configure(options);
    }

    public void Configure(AuthenticationOptions options)
    {
        options.DefaultScheme = DefaultSchemaId;
        options.AddScheme<CustomAuthSchemeApiKeyAuthenticationHandler>(DefaultSchemaId, DefaultSchemaId);
    }
}
