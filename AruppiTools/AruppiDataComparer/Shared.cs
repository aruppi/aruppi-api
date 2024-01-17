using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;

namespace AruppiDataComparer
{
    public class Img
    {
        [JsonPropertyName("alt")]
        public string Alt { get; set; }

        [JsonPropertyName("url")]
        public Uri Url { get; set; }
    }

    public class MalResult
    {
        public const int MatchRatio = 95;
        public const int MaybeRatio = 90;
        public const int Cutoff = MaybeRatio;
        public List<long> Matches { get; set; } = new List<long>();
        public List<long> Maybe { get; set; } = new List<long>();
    }
}
