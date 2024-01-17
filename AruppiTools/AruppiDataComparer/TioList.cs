using System.Text.Json.Serialization;

namespace AruppiDataComparer
{
    public class TioDataListItem
    {
        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }

        [JsonPropertyName("img")]
        public Img Img { get; set; }
    }
}
