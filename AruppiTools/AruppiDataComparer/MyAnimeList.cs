using System;
using System.Collections.Generic;

using System.Text.Json;
using System.Text.Json.Serialization;
using System.Globalization;

namespace AruppiDataComparer
{


    public partial class MyAnimeListDatabase
    {
        [JsonPropertyName("schema")]
        public Schema Schema { get; set; }

        [JsonPropertyName("data")]
        public List<MyAnimeListItem> Data { get; set; }
    }

    public class MyAnimeListItem
    {
        [JsonPropertyName("anime_id")]
        public long AnimeId { get; set; }

        [JsonPropertyName("title")]
        public string Title { get; set; }

        [JsonPropertyName("type")]
        public string Type { get; set; }

        [JsonPropertyName("score")]
        public decimal? Score { get; set; }

        [JsonPropertyName("scored_by")]
        public long ScoredBy { get; set; }

        [JsonPropertyName("status")]
        public string Status { get; set; }

        [JsonPropertyName("episodes")]
        public long? Episodes { get; set; }

        [JsonPropertyName("start_date")]
        public string StartDate { get; set; }

        [JsonPropertyName("end_date")]
        public string EndDate { get; set; }

        [JsonPropertyName("source")]
        public string Source { get; set; }

        [JsonPropertyName("members")]
        public long Members { get; set; }

        [JsonPropertyName("favorites")]
        public long Favorites { get; set; }

        [JsonPropertyName("episode_duration")]
        public string EpisodeDuration { get; set; }

        [JsonPropertyName("total_duration")]
        public string TotalDuration { get; set; }

        [JsonPropertyName("rating")]
        public string Rating { get; set; }

        [JsonPropertyName("sfw")]
        public bool Sfw { get; set; }

        [JsonPropertyName("approved")]
        public bool Approved { get; set; }

        [JsonPropertyName("created_at")]
        public string CreatedAt { get; set; }

        [JsonPropertyName("updated_at")]
        public string UpdatedAt { get; set; }

        [JsonPropertyName("start_year")]
        public long? StartYear { get; set; }

        [JsonPropertyName("start_season")]
        public string StartSeason { get; set; }

        [JsonPropertyName("real_start_date")]
        public string RealStartDate { get; set; }

        [JsonPropertyName("real_end_date")]
        public string RealEndDate { get; set; }

        [JsonPropertyName("broadcast_day")]
        public string BroadcastDay { get; set; }

        [JsonPropertyName("broadcast_time")]
        public string BroadcastTime { get; set; }

        [JsonPropertyName("genres")]
        public List<string> Genres { get; set; }

        [JsonPropertyName("themes")]
        public List<string> Themes { get; set; }

        [JsonPropertyName("demographics")]
        public List<string> Demographics { get; set; }

        [JsonPropertyName("studios")]
        public List<string> Studios { get; set; }

        [JsonPropertyName("producers")]
        public List<string> Producers { get; set; }

        [JsonPropertyName("licensors")]
        public List<string> Licensors { get; set; }

        [JsonPropertyName("synopsis")]
        public string Synopsis { get; set; }

        [JsonPropertyName("background")]
        public string Background { get; set; }

        [JsonPropertyName("main_picture")]
        public Uri MainPicture { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }

        [JsonPropertyName("trailer_url")]
        public Uri TrailerUrl { get; set; }

        [JsonPropertyName("title_english")]
        public string TitleEnglish { get; set; }

        [JsonPropertyName("title_japanese")]
        public string TitleJapanese { get; set; }

        [JsonPropertyName("title_synonyms")]
        public List<string> TitleSynonyms { get; set; }

        // Custom field to simplify code
        [JsonIgnore]
        public List<string> AllTitles { get; set; } = new List<string>();
    }

    public class Schema
    {
        [JsonPropertyName("fields")]
        public List<Field> Fields { get; set; }

        [JsonPropertyName("pandas_version")]
        public string PandasVersion { get; set; }
    }

    public class Field
    {
        [JsonPropertyName("name")]
        public string Name { get; set; }

        [JsonPropertyName("type")]
        public string Type { get; set; }

        [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
        [JsonPropertyName("extDtype")]
        public string ExtDtype { get; set; }

        [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
        [JsonPropertyName("tz")]
        public string Tz { get; set; }
    }

    public partial class Temperatures
    {
        public static Temperatures FromJson(string json) => JsonSerializer.Deserialize<Temperatures>(json, AruppiDataComparer.Converter.Settings);
    }

    public static class Serialize
    {
        public static string ToJson(this Temperatures self) => JsonSerializer.Serialize(self, AruppiDataComparer.Converter.Settings);
    }

    internal static class Converter
    {
        public static readonly JsonSerializerOptions Settings = new(JsonSerializerDefaults.General)
        {
            Converters =
            {
                new DateOnlyConverter(),
                new TimeOnlyConverter(),
                IsoDateTimeOffsetConverter.Singleton
            },
        };
    }

    public class DateOnlyConverter : JsonConverter<DateOnly>
    {
        private readonly string serializationFormat;
        public DateOnlyConverter() : this(null) { }

        public DateOnlyConverter(string? serializationFormat)
        {
            this.serializationFormat = serializationFormat ?? "yyyy-MM-dd";
        }

        public override DateOnly Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            var value = reader.GetString();
            return DateOnly.Parse(value!);
        }

        public override void Write(Utf8JsonWriter writer, DateOnly value, JsonSerializerOptions options)
            => writer.WriteStringValue(value.ToString(serializationFormat));
    }

    public class TimeOnlyConverter : JsonConverter<TimeOnly>
    {
        private readonly string serializationFormat;

        public TimeOnlyConverter() : this(null) { }

        public TimeOnlyConverter(string? serializationFormat)
        {
            this.serializationFormat = serializationFormat ?? "HH:mm:ss.fff";
        }

        public override TimeOnly Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            var value = reader.GetString();
            return TimeOnly.Parse(value!);
        }

        public override void Write(Utf8JsonWriter writer, TimeOnly value, JsonSerializerOptions options)
            => writer.WriteStringValue(value.ToString(serializationFormat));
    }

    internal class IsoDateTimeOffsetConverter : JsonConverter<DateTimeOffset>
    {
        public override bool CanConvert(Type t) => t == typeof(DateTimeOffset);

        private const string DefaultDateTimeFormat = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.FFFFFFFK";

        private DateTimeStyles _dateTimeStyles = DateTimeStyles.RoundtripKind;
        private string? _dateTimeFormat;
        private CultureInfo? _culture;

        public DateTimeStyles DateTimeStyles
        {
            get => _dateTimeStyles;
            set => _dateTimeStyles = value;
        }

        public string? DateTimeFormat
        {
            get => _dateTimeFormat ?? string.Empty;
            set => _dateTimeFormat = (string.IsNullOrEmpty(value)) ? null : value;
        }

        public CultureInfo Culture
        {
            get => _culture ?? CultureInfo.CurrentCulture;
            set => _culture = value;
        }

        public override void Write(Utf8JsonWriter writer, DateTimeOffset value, JsonSerializerOptions options)
        {
            string text;


            if ((_dateTimeStyles & DateTimeStyles.AdjustToUniversal) == DateTimeStyles.AdjustToUniversal
                || (_dateTimeStyles & DateTimeStyles.AssumeUniversal) == DateTimeStyles.AssumeUniversal)
            {
                value = value.ToUniversalTime();
            }

            text = value.ToString(_dateTimeFormat ?? DefaultDateTimeFormat, Culture);

            writer.WriteStringValue(text);
        }

        public override DateTimeOffset Read(ref Utf8JsonReader reader, Type typeToConvert, JsonSerializerOptions options)
        {
            string? dateText = reader.GetString();

            if (string.IsNullOrEmpty(dateText) == false)
            {
                if (!string.IsNullOrEmpty(_dateTimeFormat))
                {
                    return DateTimeOffset.ParseExact(dateText, _dateTimeFormat, Culture, _dateTimeStyles);
                }
                else
                {
                    return DateTimeOffset.Parse(dateText, Culture, _dateTimeStyles);
                }
            }
            else
            {
                return default(DateTimeOffset);
            }
        }


        public static readonly IsoDateTimeOffsetConverter Singleton = new IsoDateTimeOffsetConverter();
    }
}