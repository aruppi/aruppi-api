using AruppiApi.Startup;
using AruppiApi.Startup.ApiVersioning;
using AruppiApi.Startup.AuthenticationAndAuthorizationOptions;
using AruppiApi.Startup.HealthCheck;
using AruppiApi.Startup.Swagger;
using JikanRest;
using Microsoft.Kiota.Abstractions.Authentication;
using Microsoft.Kiota.Http.HttpClientLibrary;

namespace AruppiApi;

public class Program
{
    public static void Main(string[] args)
    {
        var builder = WebApplication.CreateBuilder(args);

        // Add Configuration
        // This initializes the configuration abstractions
        // https://learn.microsoft.com/en-us/aspnet/core/fundamentals/configuration/?view=aspnetcore-8.0
        var configuration = builder.Configuration;

        // https://learn.microsoft.com/en-us/aspnet/core/fundamentals/configuration/?view=aspnetcore-8.0#default-application-configuration-sources
        configuration.AddJsonFile(path: "appsettings.json", optional: false, reloadOnChange: false);
        configuration.AddJsonFile(path: "appsettings.Development.json", optional: true, reloadOnChange: true);
        configuration.AddEnvironmentVariables();
        configuration.AddCommandLine(args);

        // Add services to the container.
        var services = builder.Services;

        services.AddControllers();

        services.ConfigureJson();

        services.AddHealthCheckConfigured();

        // https://learn.microsoft.com/en-us/dotnet/architecture/microservices/implement-resilient-applications/use-httpclientfactory-to-implement-resilient-http-requests
        services.AddHttpClient();

        services.AddDatabaseContext();

        services.AddApiVersioningConfigured();

        services.AddAuthenticationAndAuthorization();

        services.AddSwaggerConfigured();
#if DEBUG
        services.AddHttpLogging((options) => { });
#endif

        services.AddSingleton((IServiceProvider) =>
        {
            var httpClient = IServiceProvider.GetRequiredService<HttpClient>();

            var authProvider = new AnonymousAuthenticationProvider();
            
            var adapter = new HttpClientRequestAdapter(authenticationProvider: authProvider, httpClient: httpClient);
            
            var client = new JikanApi(adapter);

            return client;
        });

        //services.AddSingleton((IServiceProvider) =>
        //{
        //    var httpClientFactory = IServiceProvider.GetRequiredService<IHttpClientFactory>();
        //    return new JikanApiClient(httpClientFactory);
        //});

        //services.AddTransient<RestApiClient>((IServiceProvider) =>
        //{
        //    var httpClientFactory = IServiceProvider.GetRequiredService<IHttpClientFactory>();
        //    return new RestApiClient(httpClientFactory);
        //});

        var app = builder.Build();

        // Configure the HTTP request pipeline.

        app.UseCors(options =>
        {
            options.AllowAnyOrigin();
            options.AllowAnyMethod();
            options.AllowAnyHeader();
        });

        app.UseAuthenticationAndAuthorization();

        app.UseHealthCheckConfigured();

        app.MapControllers();

#if DEBUG
        if (app.Environment.IsDevelopment())
        {
            app.UseHttpLogging();
            app.UseDeveloperExceptionPage();

            app.UseSwaggerConfigured();
        }
#endif

        app.Run();
    }
}
