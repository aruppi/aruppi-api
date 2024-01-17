using System.Text.Json.Serialization;
using static Program;

namespace AruppiDataComparer
{
    public class MonoDataItem
    {
        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }

        [JsonPropertyName("id")]
        public string Id { get; set; }

        [JsonPropertyName("poster")]
        public Uri Poster { get; set; }

        [JsonPropertyName("category")]
        public string Category { get; set; }

        [JsonPropertyName("year")]
        public string Year { get; set; }

        [JsonPropertyName("thumbnail")]
        public Uri Thumbnail { get; set; }

        [JsonPropertyName("title_alter")]
        public string TitleAlter { get; set; }

        [JsonPropertyName("genres")]
        public List<string> Genres { get; set; }

        [JsonPropertyName("premiere")]
        public string Premiere { get; set; }

        [JsonPropertyName("description")]
        public string Description { get; set; }

        [JsonPropertyName("score")]
        public string Score { get; set; }

        public MalResult MalResult { get; set; } = new MalResult();
    }
}
