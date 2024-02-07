// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    /// <summary>
    /// Available images in JPG
    /// </summary>
    public class Manga_images_jpg : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Image URL JPG</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? ImageUrl { get; set; }
#nullable restore
#else
        public string ImageUrl { get; set; }
#endif
        /// <summary>Image URL JPG</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? LargeImageUrl { get; set; }
#nullable restore
#else
        public string LargeImageUrl { get; set; }
#endif
        /// <summary>Small Image URL JPG</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? SmallImageUrl { get; set; }
#nullable restore
#else
        public string SmallImageUrl { get; set; }
#endif
        /// <summary>
        /// Instantiates a new manga_images_jpg and sets the default values.
        /// </summary>
        public Manga_images_jpg() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static Manga_images_jpg CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new Manga_images_jpg();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"image_url", n => { ImageUrl = n.GetStringValue(); } },
                {"large_image_url", n => { LargeImageUrl = n.GetStringValue(); } },
                {"small_image_url", n => { SmallImageUrl = n.GetStringValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteStringValue("image_url", ImageUrl);
            writer.WriteStringValue("large_image_url", LargeImageUrl);
            writer.WriteStringValue("small_image_url", SmallImageUrl);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}
