// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    /// <summary>
    /// Date Prop
    /// </summary>
    public class Daterange_prop : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Date Prop From</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Daterange_prop_from? From { get; set; }
#nullable restore
#else
        public Daterange_prop_from From { get; set; }
#endif
        /// <summary>Raw parsed string</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? String { get; set; }
#nullable restore
#else
        public string String { get; set; }
#endif
        /// <summary>Date Prop To</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Daterange_prop_to? To { get; set; }
#nullable restore
#else
        public Daterange_prop_to To { get; set; }
#endif
        /// <summary>
        /// Instantiates a new daterange_prop and sets the default values.
        /// </summary>
        public Daterange_prop() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static Daterange_prop CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new Daterange_prop();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"from", n => { From = n.GetObjectValue<Daterange_prop_from>(Daterange_prop_from.CreateFromDiscriminatorValue); } },
                {"string", n => { String = n.GetStringValue(); } },
                {"to", n => { To = n.GetObjectValue<Daterange_prop_to>(Daterange_prop_to.CreateFromDiscriminatorValue); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteObjectValue<Daterange_prop_from>("from", From);
            writer.WriteStringValue("string", String);
            writer.WriteObjectValue<Daterange_prop_to>("to", To);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}