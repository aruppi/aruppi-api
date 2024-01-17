using Microsoft.AspNetCore.Authorization;
using Microsoft.Extensions.Options;
using AruppiApi.Startup.Swagger;

namespace AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

public class ConfiguredAuthorizationOptions : IConfigureNamedOptions<AuthorizationOptions>
{
    private readonly List<SecurityConfig>? SecurityConfigs;
    public ConfiguredAuthorizationOptions(IConfiguration IConfiguration)
    {
        SecurityConfigs = Swagger.Swagger.GetConfig<Generation>(IConfiguration)?.SecurityConfigs;
    }

    public void Configure(string? name, AuthorizationOptions options)
    {
        Configure(options);
    }

    public void Configure(AuthorizationOptions options)
    {
        var authenticationSchemes = new string[]
        {
            ConfiguredAuthenticationOptions.DefaultSchemaId
        };

        var defaultAuthorizationPolicyBuilder = new AuthorizationPolicyBuilder(authenticationSchemes);
        defaultAuthorizationPolicyBuilder.RequireAuthenticatedUser();

        var policy = defaultAuthorizationPolicyBuilder.Build();
        options.DefaultPolicy = policy;
    }
}
