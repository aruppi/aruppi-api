namespace AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

public static class AuthenticationAndAuthorization
{
    public static void AddAuthenticationAndAuthorization(this IServiceCollection services)
    {
        services.ConfigureOptions<ConfiguredAuthenticationOptions>();
        services.ConfigureOptions<ConfiguredAuthorizationOptions>();
        services.ConfigureOptions<ConfigureDefaultAuthorizationFilterOptions>();

        var authBuilder = services.AddAuthentication();

        //services.ConfigureOptions<ConfiguredJwtBearerOptions>();
        //authBuilder.AddJwtBearer();

        services.AddAuthorization();
    }

    public static void UseAuthenticationAndAuthorization(this IApplicationBuilder appBuilder)
    {
        appBuilder.UseAuthentication();
        appBuilder.UseAuthorization();
    }

    public static TSettings GetConfig<TSettings>(this IConfiguration IConfiguration)
    {
        return IConfiguration.GetConfig<TSettings>(nameof(AuthenticationAndAuthorization));
    }
}
