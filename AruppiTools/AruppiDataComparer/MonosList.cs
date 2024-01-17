using System.Text.Json.Serialization;

namespace AruppiDataComparer
{
    public class MonoListItem
    {
        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }

        [JsonPropertyName("img")]
        public Img Img { get; set; }

        [JsonPropertyName("details")]
        public Details Details { get; set; }
    }

    public class Details
    {
        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("info")]
        public string Info { get; set; }
    }
}
