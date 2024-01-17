using Microsoft.EntityFrameworkCore;
using AruppiApi.Database;

namespace AruppiApi.Startup;

public static class Database
{
    public static void AddDatabaseContext(this IServiceCollection services)
    {
        services.AddDbContext<DbContext, DatabaseContext>(optionsAction: (serviceProvider, dbContextOptionsBuilder) =>
        {
#if DEBUG
            dbContextOptionsBuilder.EnableDetailedErrors();
            dbContextOptionsBuilder.EnableSensitiveDataLogging();
#endif
            var config = serviceProvider.GetRequiredService<IConfiguration>();
            var connectionString = config.GetConnectionString(nameof(DatabaseContext));

            // Change this as needed
            dbContextOptionsBuilder.UseSqlite(connectionString);
        });
    }
}