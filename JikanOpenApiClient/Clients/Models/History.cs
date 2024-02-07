// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    /// <summary>
    /// Transform the resource into an array.
    /// </summary>
    public class History : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Date ISO8601</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Date { get; set; }
#nullable restore
#else
        public string Date { get; set; }
#endif
        /// <summary>Parsed URL Data</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Mal_url? Entry { get; set; }
#nullable restore
#else
        public Mal_url Entry { get; set; }
#endif
        /// <summary>Number of episodes/chapters watched/read</summary>
        public int? Increment { get; set; }
        /// <summary>
        /// Instantiates a new history and sets the default values.
        /// </summary>
        public History() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static History CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new History();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"date", n => { Date = n.GetStringValue(); } },
                {"entry", n => { Entry = n.GetObjectValue<Mal_url>(Mal_url.CreateFromDiscriminatorValue); } },
                {"increment", n => { Increment = n.GetIntValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteStringValue("date", Date);
            writer.WriteObjectValue<Mal_url>("entry", Entry);
            writer.WriteIntValue("increment", Increment);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}
