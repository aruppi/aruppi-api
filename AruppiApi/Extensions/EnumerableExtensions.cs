namespace AruppiApi.Extensions;

public static class EnumerableExtensions
{
    public static (IEnumerable<Y> Added, IEnumerable<T> Deleted) FindChanges<T, Y, Z>(this ICollection<T> baseList, List<Y> newList, Func<T, Z> baseSelector, Func<Y, Z> newSelector) where Z : IEquatable<Z>
    {
        var comparer = EqualityComparer<Z>.Default;
        var newGeneres = newList.Where(@new => !baseList.Any(bas => comparer.Equals(newSelector(@new), baseSelector(bas))));
        var deletedGeneres = baseList.Where(bas => !newList.Any(@new => comparer.Equals(baseSelector(bas), newSelector(@new))));
        return (newGeneres, deletedGeneres);
    }
}
