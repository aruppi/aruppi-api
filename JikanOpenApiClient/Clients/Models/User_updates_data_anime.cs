// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    public class User_updates_data_anime : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>ISO8601 format</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Date { get; set; }
#nullable restore
#else
        public string Date { get; set; }
#endif
        /// <summary>The entry property</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Anime_meta? Entry { get; set; }
#nullable restore
#else
        public Anime_meta Entry { get; set; }
#endif
        /// <summary>The episodes_seen property</summary>
        public int? EpisodesSeen { get; set; }
        /// <summary>The episodes_total property</summary>
        public int? EpisodesTotal { get; set; }
        /// <summary>The score property</summary>
        public int? Score { get; set; }
        /// <summary>The status property</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Status { get; set; }
#nullable restore
#else
        public string Status { get; set; }
#endif
        /// <summary>
        /// Instantiates a new user_updates_data_anime and sets the default values.
        /// </summary>
        public User_updates_data_anime() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static User_updates_data_anime CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new User_updates_data_anime();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"date", n => { Date = n.GetStringValue(); } },
                {"entry", n => { Entry = n.GetObjectValue<Anime_meta>(Anime_meta.CreateFromDiscriminatorValue); } },
                {"episodes_seen", n => { EpisodesSeen = n.GetIntValue(); } },
                {"episodes_total", n => { EpisodesTotal = n.GetIntValue(); } },
                {"score", n => { Score = n.GetIntValue(); } },
                {"status", n => { Status = n.GetStringValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteStringValue("date", Date);
            writer.WriteObjectValue<Anime_meta>("entry", Entry);
            writer.WriteIntValue("episodes_seen", EpisodesSeen);
            writer.WriteIntValue("episodes_total", EpisodesTotal);
            writer.WriteIntValue("score", Score);
            writer.WriteStringValue("status", Status);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}
