// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    /// <summary>
    /// Producers Resource
    /// </summary>
    public class Producer : IAdditionalDataHolder, IParsable {
        /// <summary>About the Producer</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? About { get; set; }
#nullable restore
#else
        public string About { get; set; }
#endif
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Producers&apos;s anime count</summary>
        public int? Count { get; set; }
        /// <summary>Established Date ISO8601</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Established { get; set; }
#nullable restore
#else
        public string Established { get; set; }
#endif
        /// <summary>Producers&apos;s member favorites count</summary>
        public int? Favorites { get; set; }
        /// <summary>The images property</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Common_images? Images { get; set; }
#nullable restore
#else
        public Common_images Images { get; set; }
#endif
        /// <summary>MyAnimeList ID</summary>
        public int? MalId { get; set; }
        /// <summary>All titles</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public List<Title>? Titles { get; set; }
#nullable restore
#else
        public List<Title> Titles { get; set; }
#endif
        /// <summary>MyAnimeList URL</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Url { get; set; }
#nullable restore
#else
        public string Url { get; set; }
#endif
        /// <summary>
        /// Instantiates a new producer and sets the default values.
        /// </summary>
        public Producer() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static Producer CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new Producer();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"about", n => { About = n.GetStringValue(); } },
                {"count", n => { Count = n.GetIntValue(); } },
                {"established", n => { Established = n.GetStringValue(); } },
                {"favorites", n => { Favorites = n.GetIntValue(); } },
                {"images", n => { Images = n.GetObjectValue<Common_images>(Common_images.CreateFromDiscriminatorValue); } },
                {"mal_id", n => { MalId = n.GetIntValue(); } },
                {"titles", n => { Titles = n.GetCollectionOfObjectValues<Title>(Title.CreateFromDiscriminatorValue)?.ToList(); } },
                {"url", n => { Url = n.GetStringValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteStringValue("about", About);
            writer.WriteIntValue("count", Count);
            writer.WriteStringValue("established", Established);
            writer.WriteIntValue("favorites", Favorites);
            writer.WriteObjectValue<Common_images>("images", Images);
            writer.WriteIntValue("mal_id", MalId);
            writer.WriteCollectionOfObjectValues<Title>("titles", Titles);
            writer.WriteStringValue("url", Url);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}