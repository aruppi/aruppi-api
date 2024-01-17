using Microsoft.Extensions.Diagnostics.HealthChecks;
using AruppiApi.Database;

namespace AruppiApi.Startup.HealthCheck;

public class DatabaseHealthCheck : IHealthCheck
{
    public DatabaseContext DatabaseContext { get; set; }
    public DatabaseHealthCheck(DatabaseContext DatabaseContext)
    {
        this.DatabaseContext = DatabaseContext;
    }

    public async Task<HealthCheckResult> CheckHealthAsync(HealthCheckContext context, CancellationToken cancellationToken = default)
    {
        var db = DatabaseContext.Database;

        if (!await db.CanConnectAsync(cancellationToken))
        {
            return HealthCheckResult.Unhealthy("Error, Could Not Connect");
        }

        return HealthCheckResult.Healthy("Ok");
    }
}
