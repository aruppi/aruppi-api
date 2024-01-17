using Microsoft.EntityFrameworkCore;
using System.Linq.Expressions;
using AruppiApi.Database.Infraestructure;

namespace AruppiApi.Database;

public partial class DatabaseContext : DbContext
{
    public bool IncludeSoftDeleted { get; set; }

    partial void OnModelCreatingPartial(ModelBuilder modelBuilder)
    {
        // Here i am creating general filters on models of the database that will be added to any query by default
        // Using the program metadata to know whichones are relevant to what
        var models = modelBuilder.Model.GetEntityTypes();

        var thisInstance = Expression.Constant(this);

        foreach (var model in models)
        {
            var clrType = model.ClrType;

            var expressionList = new List<BinaryExpression>();

            var dbItem = Expression.Parameter(clrType, clrType.Name);

            if (clrType.IsAssignableTo(typeof(ISoftDeleted)))
            {
                var includeSoftDeletedValues = Expression.Property(thisInstance, nameof(IncludeSoftDeleted));

                var falseExpression = Expression.Constant(false);
                var dbValue = Expression.Property(dbItem, nameof(ISoftDeleted.IsDeleted));
                var equalityExpression = Expression.Equal(dbValue, falseExpression);

                var includeSoftDeleted = Expression.OrElse(includeSoftDeletedValues, equalityExpression);
                expressionList.Add(includeSoftDeleted);
            }

            if (expressionList.Count < 1)
            {
                continue;
            }

            BinaryExpression body = expressionList[0];

            for (int i = 1; i < expressionList.Count; i++)
            {
                body = Expression.AndAlso(body, expressionList[i]);
            }

            var lambda = Expression.Lambda(body, dbItem);

            model.SetQueryFilter(lambda);
        }
    }
}
