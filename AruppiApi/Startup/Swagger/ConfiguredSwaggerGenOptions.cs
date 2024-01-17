using Microsoft.AspNetCore.Hosting.Server;
using Microsoft.AspNetCore.Hosting.Server.Features;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace AruppiApi.Startup.Swagger;

public class ConfiguredSwaggerGenOptions : IConfigureNamedOptions<SwaggerGenOptions>
{
    private readonly Generation SwaggerGen;
    private readonly IServer IServer;

    public ConfiguredSwaggerGenOptions(IConfiguration IConfiguration, IServer IServer)
    {
        SwaggerGen = IConfiguration.GetConfig<Generation>();
        this.IServer = IServer;
    }

    public void Configure(string? name, SwaggerGenOptions options)
    {
        Configure(options);
    }

    public void Configure(SwaggerGenOptions options)
    {
        options.EnableAnnotations();

        SetupSecurityDocumentation(options);

        SetupAdresses(options);

        options.OperationFilter<SecurityRequirementsOperationFilter>();
    }

    private void SetupAdresses(SwaggerGenOptions options)
    {
        var serverAdresses = IServer.Features.Get<IServerAddressesFeature>();

        if (serverAdresses is not null)
        {
            var count = 1;
            foreach (var currentListeningAddress in serverAdresses.Addresses)
            {
                var server = new OpenApiServer()
                {
                    Description = $"CurrentExecution Address{count}",
                    Url = currentListeningAddress,
                };

                count++;

                options.AddServer(server);
            }
        }

        if (SwaggerGen.ExtraServers is not null)
        {
            foreach (var server in SwaggerGen.ExtraServers)
            {
                options.AddServer(server);
            }
        }
    }

    private void SetupSecurityDocumentation(SwaggerGenOptions options)
    {
        if (SwaggerGen.SecurityConfigs is null)
        {
            return;
        }

        foreach (var securityConfig in SwaggerGen.SecurityConfigs)
        {
            if (securityConfig.SecuritySchemeType is not SecuritySchemeType schemeType)
            {
                throw new InvalidDataException("SecurityConfig.SecuritySchemeType can not be null in config");
            }

            if (securityConfig.Name is null)
            {
                throw new InvalidDataException("SecurityConfig.Name can not be null in config");
            }

            var securityScheme = new OpenApiSecurityScheme
            {
                Type = schemeType,
            };

            switch (schemeType)
            {
                case SecuritySchemeType.Http:
                    if (securityConfig.Http?.Scheme is not string scheme)
                    {
                        throw new InvalidDataException("SecurityConfig.Http.Scheme can not be null");
                    }
                    securityScheme.Scheme = scheme;
                    securityScheme.BearerFormat = securityConfig.Http?.Scheme;
                    break;
                case SecuritySchemeType.ApiKey:
                    if (securityConfig.ApiKey?.ParameterLocation is not ParameterLocation parameterLocation)
                    {
                        throw new InvalidDataException("SecurityConfig.ApiKey.InLocation can not be null");
                    }

                    if (securityConfig.ApiKey?.ParameterName is not string keyName)
                    {
                        throw new InvalidDataException("SecurityConfig.ApiKey.KeyName can not be null");
                    }

                    securityScheme.In = parameterLocation;
                    securityScheme.Name = keyName;
                    break;
                case SecuritySchemeType.OAuth2:
                    if (securityConfig.Oauth2?.AuthorizationUrl is not string authorizationUrl)
                    {
                        throw new InvalidDataException("SecurityConfig.Oauth2.AuthorizationUrl can not be null");
                    }

                    if (securityConfig.Oauth2?.ApiScopes is null)
                    {
                        throw new InvalidDataException("SecurityConfig.Oauth2.ApiScopes can not be null");
                    }

                    securityScheme.Flows = new OpenApiOAuthFlows
                    {
                        Implicit = new OpenApiOAuthFlow
                        {
                            AuthorizationUrl = new Uri(authorizationUrl),
                            Scopes = securityConfig.Oauth2.ApiScopes,
                        }
                    };
                    break;
                case SecuritySchemeType.OpenIdConnect:
                default:
                    throw new NotImplementedException(schemeType.ToString());
            }

            options.AddSecurityDefinition(securityConfig.Name, securityScheme);
        }

    }
}

public class Generation
{
    public List<SecurityConfig>? SecurityConfigs { get; set; }
    public List<OpenApiServer>? ExtraServers { get; set; }
}

public class SecurityConfig
{
    public string? Name { get; set; }
    public SecuritySchemeType? SecuritySchemeType { get; set; }
    public SecurityConfigHttp? Http { get; set; }
    public SecurityConfigApiKey? ApiKey { get; set; }
    public SecurityConfigOauth2? Oauth2 { get; set; }
}

public class SecurityConfigHttp
{
    public string? Scheme { get; set; }
    public string? BearerFormat { get; set; }
}

public class SecurityConfigApiKey
{
    public ParameterLocation? ParameterLocation { get; set; }
    public string? ParameterName { get; set; }
}

public class SecurityConfigOauth2
{
    public string? AuthorizationUrl { get; set; }
    public Dictionary<string, string>? ApiScopes { get; set; }
}