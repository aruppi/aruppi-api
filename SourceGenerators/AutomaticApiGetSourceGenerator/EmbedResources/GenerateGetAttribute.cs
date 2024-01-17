using System;
using System.Text.Json;
using System.Text.Json.Serialization;

namespace ApiGetGenerator;

[AttributeUsage(AttributeTargets.Method, Inherited = false, AllowMultiple = false)]
public sealed class GenerateGetAttribute : Attribute
{
    // This is a named argument
    public string InyectedDatabaseContextName { get; set; }
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum OrderDirection
{
    Ascending,
    Descending,
}

public class OrderBy<TEntityColumns>
{
    public OrderDirection Direction { get; set; }
    public TEntityColumns Column { get; set; }
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum StringComparationType
{
    Equals,
    StartsWith,
    Contains,
    EndsWith,
}

public class Page<T>
{
    public long TotalResults { get; set; }
    public int PageSize { get; set; }
    public int PageIndex { get; set; }
    public int TotalPages { get; set; }
    public IEnumerable<T> Data { get; set; }

    public Page(IEnumerable<T> data, int count, int pageSize, int pageIndex)
    {
        var Quotient = Math.DivRem(count, pageSize, out var Remainder);

        if (Remainder == 0)
        {
            Quotient--;
        }

        TotalResults = count;
        PageSize = pageSize;
        PageIndex = pageIndex;
        TotalPages = Quotient;
        Data = data;
    }
}