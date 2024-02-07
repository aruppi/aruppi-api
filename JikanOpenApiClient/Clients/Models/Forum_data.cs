// <auto-generated/>
using Microsoft.Kiota.Abstractions.Serialization;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System;
namespace JikanRest.Models {
    public class Forum_data : IAdditionalDataHolder, IParsable {
        /// <summary>Stores additional data not described in the OpenAPI description found when deserializing. Can be used for serialization as well.</summary>
        public IDictionary<string, object> AdditionalData { get; set; }
        /// <summary>Author Profile URL</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? AuthorUrl { get; set; }
#nullable restore
#else
        public string AuthorUrl { get; set; }
#endif
        /// <summary>Author MyAnimeList Username</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? AuthorUsername { get; set; }
#nullable restore
#else
        public string AuthorUsername { get; set; }
#endif
        /// <summary>Comment count</summary>
        public int? Comments { get; set; }
        /// <summary>Post Date ISO8601</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Date { get; set; }
#nullable restore
#else
        public string Date { get; set; }
#endif
        /// <summary>Last comment details</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public Forum_data_last_comment? LastComment { get; set; }
#nullable restore
#else
        public Forum_data_last_comment LastComment { get; set; }
#endif
        /// <summary>MyAnimeList ID</summary>
        public int? MalId { get; set; }
        /// <summary>Title</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public string? Title { get; set; }
#nullable restore
#else
        public string Title { get; set; }
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
        /// Instantiates a new forum_data and sets the default values.
        /// </summary>
        public Forum_data() {
            AdditionalData = new Dictionary<string, object>();
        }
        /// <summary>
        /// Creates a new instance of the appropriate class based on discriminator value
        /// </summary>
        /// <param name="parseNode">The parse node to use to read the discriminator value and create the object</param>
        public static Forum_data CreateFromDiscriminatorValue(IParseNode parseNode) {
            _ = parseNode ?? throw new ArgumentNullException(nameof(parseNode));
            return new Forum_data();
        }
        /// <summary>
        /// The deserialization information for the current model
        /// </summary>
        public virtual IDictionary<string, Action<IParseNode>> GetFieldDeserializers() {
            return new Dictionary<string, Action<IParseNode>> {
                {"author_url", n => { AuthorUrl = n.GetStringValue(); } },
                {"author_username", n => { AuthorUsername = n.GetStringValue(); } },
                {"comments", n => { Comments = n.GetIntValue(); } },
                {"date", n => { Date = n.GetStringValue(); } },
                {"last_comment", n => { LastComment = n.GetObjectValue<Forum_data_last_comment>(Forum_data_last_comment.CreateFromDiscriminatorValue); } },
                {"mal_id", n => { MalId = n.GetIntValue(); } },
                {"title", n => { Title = n.GetStringValue(); } },
                {"url", n => { Url = n.GetStringValue(); } },
            };
        }
        /// <summary>
        /// Serializes information the current object
        /// </summary>
        /// <param name="writer">Serialization writer to use to serialize this model</param>
        public virtual void Serialize(ISerializationWriter writer) {
            _ = writer ?? throw new ArgumentNullException(nameof(writer));
            writer.WriteStringValue("author_url", AuthorUrl);
            writer.WriteStringValue("author_username", AuthorUsername);
            writer.WriteIntValue("comments", Comments);
            writer.WriteStringValue("date", Date);
            writer.WriteObjectValue<Forum_data_last_comment>("last_comment", LastComment);
            writer.WriteIntValue("mal_id", MalId);
            writer.WriteStringValue("title", Title);
            writer.WriteStringValue("url", Url);
            writer.WriteAdditionalData(AdditionalData);
        }
    }
}
