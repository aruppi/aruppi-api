namespace JikanClient.Models.Request;

public class GetSchedulesRequest
{
    public Days? Day { get; set; }
    public bool? Kids { get; set; }
    public bool? Sfw { get; set; }
    public bool? Unapproved { get; set; }
    public int? Page { get; set; }
    public int? Limit { get; set; }

    public List<(string Key, dynamic Value)> ToQueryData()
    {
        var data = new List<(string Key, dynamic Value)>();

        if (Day is not null)
        {
            data.Add(("filter", Day));
        }

        if (Kids is not null)
        {
            data.Add(("kids", Kids));
        }

        if (Sfw is not null)
        {
            data.Add(("sfw", Sfw));
        }

        if (Unapproved is not null)
        {
            data.Add(("unapproved", Unapproved));
        }

        if (Page is not null)
        {
            data.Add(("page", Page));
        }

        if (Limit is not null)
        {
            data.Add(("limit", Limit));
        }

        return data;
    }
}