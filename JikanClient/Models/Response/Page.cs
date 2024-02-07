using System.Text.Json.Serialization;

namespace JikanClient.Models.Response;

public partial class Page<TData>
{
    [JsonPropertyName("data")]
    public List<TData> Data { get; set; }

    [JsonPropertyName("pagination")]
    public Pagination Pagination { get; set; }
}

public partial class Pagination
{
    [JsonPropertyName("last_visible_page")]
    public long LastVisiblePage { get; set; }

    [JsonPropertyName("has_next_page")]
    public bool HasNextPage { get; set; }

    [JsonPropertyName("items")]
    public Items Items { get; set; }
}

public partial class Items
{
    [JsonPropertyName("count")]
    public long Count { get; set; }

    [JsonPropertyName("total")]
    public long Total { get; set; }

    [JsonPropertyName("per_page")]
    public long PerPage { get; set; }
}