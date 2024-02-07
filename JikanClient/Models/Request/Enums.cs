using System.Text.Json.Serialization;

namespace JikanClient.Models.Request;

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum Days
{
    Monday, 
    Tuesday, 
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
    Unknown,
    Other
}

[JsonConverter(typeof(JsonStringEnumConverter))]
public enum Boolean
{
    True,
    False,
}