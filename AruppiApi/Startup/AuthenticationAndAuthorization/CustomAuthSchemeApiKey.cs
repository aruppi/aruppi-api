using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;
using Microsoft.Extensions.Primitives;
using System.Security.Claims;
using System.Text.Encodings.Web;

namespace AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

public class CustomAuthSchemeApiKeyAutenthicationOptions : AuthenticationSchemeOptions
{

}

public class CustomAuthSchemeApiKeyAuthenticationHandler : AuthenticationHandler<CustomAuthSchemeApiKeyAutenthicationOptions>
{
    private IOptionsMonitor<CustomAuthSchemeApiKeyAutenthicationOptions> options;
    public CustomAuthSchemeApiKeyAuthenticationHandler(IOptionsMonitor<CustomAuthSchemeApiKeyAutenthicationOptions> options, ILoggerFactory logger, UrlEncoder encoder, ISystemClock clock) : base(options, logger, encoder, clock)
    {
        this.options = options;
    }

    private const string HeaderName = "CustomAuth";

    protected override Task<AuthenticateResult> HandleAuthenticateAsync()
    {
        if (!Request.Headers.TryGetValue(HeaderName, out var Uid) || Uid == StringValues.Empty)
        {
            return Task.FromResult(AuthenticateResult.Fail($"Missing Requiered Header {HeaderName}"));
        }

        var claims = new List<Claim>()
        {
            new Claim(nameof(Uid), Uid.ToString()),
        };

        var claimsIdentity = new ClaimsIdentity(claims, HeaderName);

        var claimsPrincipal = new ClaimsPrincipal(claimsIdentity);

        var authTicket = new AuthenticationTicket(claimsPrincipal, Scheme.Name);

        return Task.FromResult(AuthenticateResult.Success(authTicket));
    }
}