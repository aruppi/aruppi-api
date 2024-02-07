// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    public class User_updates_data : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Last updated Anime</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public List<User_updates_data_anime>? Anime { get; set; }
#nullable restore
#else
        public List<User_updates_data_anime> Anime { get; set; }
#endif
        /// <summary>Last updated Manga</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public List<User_updates_data_manga>? Manga { get; set; }
#nullable restore
#else
        public List<User_updates_data_manga> Manga { get; set; }
#endif
        /// <summary>
        /// Instantiates a new user_updates_data and sets the default values.
        /// </summary>
        public User_updates_data() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static User_updates_data CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new User_updates_data();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"anime", n => { Anime = n.GetCollectionOfObjectValues<User_updates_data_anime>(User_updates_data_anime.CreateFromDiscriminatorValue)?.ToList(); } },
                {"manga", n => { Manga = n.GetCollectionOfObjectValues<User_updates_data_manga>(User_updates_data_manga.CreateFromDiscriminatorValue)?.ToList(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteCollectionOfObjectValues<User_updates_data_anime>("anime", Anime);
            writer.WriteCollectionOfObjectValues<User_updates_data_manga>("manga", Manga);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}