using System.Text.Json.Serialization;

namespace JikanClient.Models.Response;

public partial class GetScheduleResponse
{
    [JsonPropertyName("mal_id")]
    public long MalId { get; set; }

    [JsonPropertyName("url")]
    public string Url { get; set; }

    [JsonPropertyName("images")]
    public Images Images { get; set; }

    [JsonPropertyName("trailer")]
    public Trailer Trailer { get; set; }

    [JsonPropertyName("approved")]
    public bool Approved { get; set; }

    [JsonPropertyName("titles")]
    public List<Title> Titles { get; set; }

    [JsonPropertyName("title")]
    public string Title { get; set; }

    [JsonPropertyName("title_english")]
    public string TitleEnglish { get; set; }

    [JsonPropertyName("title_japanese")]
    public string TitleJapanese { get; set; }

    [JsonPropertyName("title_synonyms")]
    public List<string> TitleSynonyms { get; set; }

    [JsonPropertyName("type")]
    public string Type { get; set; }

    [JsonPropertyName("source")]
    public string Source { get; set; }

    [JsonPropertyName("episodes")]
    public long? Episodes { get; set; }

    [JsonPropertyName("status")]
    public string Status { get; set; }

    [JsonPropertyName("airing")]
    public bool Airing { get; set; }

    [JsonPropertyName("aired")]
    public Aired Aired { get; set; }

    [JsonPropertyName("duration")]
    public string Duration { get; set; }

    [JsonPropertyName("rating")]
    public string Rating { get; set; }

    [JsonPropertyName("score")]
    public double? Score { get; set; }

    [JsonPropertyName("scored_by")]
    public long? ScoredBy { get; set; }

    [JsonPropertyName("rank")]
    public long Rank { get; set; }

    [JsonPropertyName("popularity")]
    public long Popularity { get; set; }

    [JsonPropertyName("members")]
    public long Members { get; set; }

    [JsonPropertyName("favorites")]
    public long Favorites { get; set; }

    [JsonPropertyName("synopsis")]
    public string Synopsis { get; set; }

    [JsonPropertyName("background")]
    public string Background { get; set; }

    [JsonPropertyName("season")]
    public string Season { get; set; }

    [JsonPropertyName("year")]
    public long? Year { get; set; }

    [JsonPropertyName("broadcast")]
    public Broadcast Broadcast { get; set; }

    [JsonPropertyName("producers")]
    public List<Demographic> Producers { get; set; }

    [JsonPropertyName("licensors")]
    public List<Demographic> Licensors { get; set; }

    [JsonPropertyName("studios")]
    public List<Demographic> Studios { get; set; }

    [JsonPropertyName("genres")]
    public List<Demographic> Genres { get; set; }

    [JsonPropertyName("explicit_genres")]
    public List<Demographic> ExplicitGenres { get; set; }

    [JsonPropertyName("themes")]
    public List<Demographic> Themes { get; set; }

    [JsonPropertyName("demographics")]
    public List<Demographic> Demographics { get; set; }
}



public partial class Images : Dictionary<string, Image>
{
    [JsonIgnore]
    public Image Jpg 
    { 
        get
        {
            TryGetValue("jpg", out var value);
            return value;
        }
    }

    [JsonIgnore]
    public Image Webp
    {
        get
        {
            TryGetValue("webp", out var value);
            return value;
        }
    }
}

public partial class Aired
{
    [JsonPropertyName("from")]
    public string From { get; set; }

    [JsonPropertyName("to")]
    public string To { get; set; }

    [JsonPropertyName("prop")]
    public Prop Prop { get; set; }
}

public partial class Prop
{
    [JsonPropertyName("from")]
    public From From { get; set; }

    [JsonPropertyName("to")]
    public From To { get; set; }

    [JsonPropertyName("string")]
    public string String { get; set; }
}

public partial class From
{
    [JsonPropertyName("day")]
    public long? Day { get; set; }

    [JsonPropertyName("month")]
    public long? Month { get; set; }

    [JsonPropertyName("year")]
    public long? Year { get; set; }
}

public partial class Broadcast
{
    [JsonPropertyName("day")]
    public string Day { get; set; }

    [JsonPropertyName("time")]
    public string Time { get; set; }

    [JsonPropertyName("timezone")]
    public string Timezone { get; set; }

    [JsonPropertyName("string")]
    public string String { get; set; }
}

public partial class Demographic
{
    [JsonPropertyName("mal_id")]
    public long MalId { get; set; }

    [JsonPropertyName("type")]
    public string Type { get; set; }

    [JsonPropertyName("name")]
    public string Name { get; set; }

    [JsonPropertyName("url")]
    public string Url { get; set; }
}

public partial class Image
{
    [JsonPropertyName("image_url")]
    public string ImageUrl { get; set; }

    [JsonPropertyName("small_image_url")]
    public string SmallImageUrl { get; set; }

    [JsonPropertyName("large_image_url")]
    public string LargeImageUrl { get; set; }
}

public partial class Title
{
    [JsonPropertyName("type")]
    public string Type { get; set; }

    [JsonPropertyName("title")]
    public string TitleTitle { get; set; }
}

public partial class Trailer
{
    [JsonPropertyName("youtube_id")]
    public string YoutubeId { get; set; }

    [JsonPropertyName("url")]
    public string Url { get; set; }

    [JsonPropertyName("embed_url")]
    public string EmbedUrl { get; set; }
}