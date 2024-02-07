// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    public class Manga_statistics_data : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Number of users who have completed the resource</summary>
        public int? Completed { get; set; }
        /// <summary>Number of users who have dropped the resource</summary>
        public int? Dropped { get; set; }
        /// <summary>Number of users who have put the resource on hold</summary>
        public int? OnHold { get; set; }
        /// <summary>Number of users who have planned to read the resource</summary>
        public int? PlanToRead { get; set; }
        /// <summary>Number of users reading the resource</summary>
        public int? Reading { get; set; }
        /// <summary>The scores property</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public List<Manga_statistics_data_scores>? Scores { get; set; }
#nullable restore
#else
        public List<Manga_statistics_data_scores> Scores { get; set; }
#endif
        /// <summary>Total number of users who have the resource added to their lists</summary>
        public int? Total { get; set; }
        /// <summary>
        /// Instantiates a new manga_statistics_data and sets the default values.
        /// </summary>
        public Manga_statistics_data() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static Manga_statistics_data CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new Manga_statistics_data();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"completed", n => { Completed = n.GetIntValue(); } },
                {"dropped", n => { Dropped = n.GetIntValue(); } },
                {"on_hold", n => { OnHold = n.GetIntValue(); } },
                {"plan_to_read", n => { PlanToRead = n.GetIntValue(); } },
                {"reading", n => { Reading = n.GetIntValue(); } },
                {"scores", n => { Scores = n.GetCollectionOfObjectValues<Manga_statistics_data_scores>(Manga_statistics_data_scores.CreateFromDiscriminatorValue)?.ToList(); } },
                {"total", n => { Total = n.GetIntValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteIntValue("completed", Completed);
            writer.WriteIntValue("dropped", Dropped);
            writer.WriteIntValue("on_hold", OnHold);
            writer.WriteIntValue("plan_to_read", PlanToRead);
            writer.WriteIntValue("reading", Reading);
            writer.WriteCollectionOfObjectValues<Manga_statistics_data_scores>("scores", Scores);
            writer.WriteIntValue("total", Total);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}
