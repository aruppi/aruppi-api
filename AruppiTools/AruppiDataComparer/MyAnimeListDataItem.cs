using System.Text.Json.Serialization;

namespace AruppiDataComparer
{
    public class MyAnimeListDataItem
    {
        [JsonPropertyName("mal_id")]
        public long MalId { get; set; }

        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("type")]
        public string Type { get; set; }

        [JsonPropertyName("episodes")]
        public string Episodes { get; set; }

        [JsonPropertyName("aired_from")]
        public string AiredFrom { get; set; }

        [JsonPropertyName("aired_to")]
        public string AiredTo { get; set; }

        [JsonPropertyName("premiered_year")]
        public string PremieredYear { get; set; }

        [JsonPropertyName("title_english")]
        public string TitleEnglish { get; set; }

        [JsonPropertyName("title_japanese")]
        public string TitleJapanese { get; set; }

        [JsonPropertyName("title_synonyms")]
        public List<string> TitleSynonyms { get; set; }

        [JsonIgnore]
        public List<string> AllTitles { get; set; } = new List<string>();
    }
}
