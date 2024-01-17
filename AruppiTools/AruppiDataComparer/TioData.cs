using System.Text.Json.Serialization;

namespace AruppiDataComparer
{
    public class TioDataItem
    {
        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }

        [JsonPropertyName("id")]
        public string Id { get; set; }

        [JsonPropertyName("poster")]
        public Uri Poster { get; set; }

        [JsonPropertyName("background")]
        public Uri Background { get; set; }

        [JsonPropertyName("original-title-raw")]
        public string OriginalTitleRaw { get; set; }

        [JsonPropertyName("type")]
        public string Type { get; set; }

        [JsonPropertyName("year")]
        public long? Year { get; set; }

        [JsonPropertyName("season")]
        public List<string> Season { get; set; }

        [JsonPropertyName("genres")]
        public List<string> Genres { get; set; }

        [JsonPropertyName("description")]
        public string Description { get; set; }

        public List<long> MyAnimeListId { get; set; } = new List<long>();
    }
}
