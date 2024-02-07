// <auto-generated/>
using JikanRest.Anime.Item;
using JikanRest.Models;
using Microsoft.Kiota.Abstractions.Serialization;
using Microsoft.Kiota.Abstractions;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;
using System;
namespace JikanRest.Anime {
    /// <summary>
    /// Builds and executes requests for operations under \anime
    /// </summary>
    public class AnimeRequestBuilder : BaseRequestBuilder {
        /// <summary>Gets an item from the JikanRest.anime.item collection</summary>
        /// <param name="position">Unique identifier of the item</param>
        public AnimeItemRequestBuilder this[int position] { get {
            var urlTplParams = new Dictionary<string, object>(PathParameters);
            urlTplParams.Add("id", position);
            return new AnimeItemRequestBuilder(urlTplParams, RequestAdapter);
        } }
        /// <summary>
        /// Instantiates a new AnimeRequestBuilder and sets the default values.
        /// </summary>
        /// <param name="pathParameters">Path parameters for the request</param>
        /// <param name="requestAdapter">The request adapter to use to execute the requests.</param>
        public AnimeRequestBuilder(Dictionary<string, object> pathParameters, IRequestAdapter requestAdapter) : base(requestAdapter, "{+baseurl}/anime{?end_date*,genres*,genres_exclude*,letter*,limit*,max_score*,min_score*,order_by*,page*,producers*,q*,rating*,score*,sfw*,sort*,start_date*,status*,type*,unapproved*}", pathParameters) {
        }
        /// <summary>
        /// Instantiates a new AnimeRequestBuilder and sets the default values.
        /// </summary>
        /// <param name="rawUrl">The raw URL to use for the request builder.</param>
        /// <param name="requestAdapter">The request adapter to use to execute the requests.</param>
        public AnimeRequestBuilder(string rawUrl, IRequestAdapter requestAdapter) : base(requestAdapter, "{+baseurl}/anime{?end_date*,genres*,genres_exclude*,letter*,limit*,max_score*,min_score*,order_by*,page*,producers*,q*,rating*,score*,sfw*,sort*,start_date*,status*,type*,unapproved*}", rawUrl) {
        }
        /// <param name="cancellationToken">Cancellation token to use when cancelling requests</param>
        /// <param name="requestConfiguration">Configuration for the request such as headers, query parameters, and middleware options.</param>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public async Task<Pagination_plus?> GetAsync(Action<RequestConfiguration<AnimeRequestBuilderGetQueryParameters>>? requestConfiguration = default, CancellationToken cancellationToken = default) {
#nullable restore
#else
        public async Task<Pagination_plus> GetAsync(Action<RequestConfiguration<AnimeRequestBuilderGetQueryParameters>> requestConfiguration = default, CancellationToken cancellationToken = default) {
#endif
            var requestInfo = ToGetRequestInformation(requestConfiguration);
            return await RequestAdapter.SendAsync<Pagination_plus>(requestInfo, Pagination_plus.CreateFromDiscriminatorValue, default, cancellationToken).ConfigureAwait(false);
        }
        /// <param name="requestConfiguration">Configuration for the request such as headers, query parameters, and middleware options.</param>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public RequestInformation ToGetRequestInformation(Action<RequestConfiguration<AnimeRequestBuilderGetQueryParameters>>? requestConfiguration = default) {
#nullable restore
#else
        public RequestInformation ToGetRequestInformation(Action<RequestConfiguration<AnimeRequestBuilderGetQueryParameters>> requestConfiguration = default) {
#endif
            var requestInfo = new RequestInformation(Method.GET, UrlTemplate, PathParameters);
            requestInfo.Configure(requestConfiguration);
            requestInfo.Headers.TryAdd("Accept", "application/json");
            return requestInfo;
        }
        /// <summary>
        /// Returns a request builder with the provided arbitrary URL. Using this method means any other path or query parameters are ignored.
        /// </summary>
        /// <param name="rawUrl">The raw URL to use for the request builder.</param>
        public AnimeRequestBuilder WithUrl(string rawUrl) {
            return new AnimeRequestBuilder(rawUrl, RequestAdapter);
        }
        public class AnimeRequestBuilderGetQueryParameters {
            /// <summary>Filter by ending date. Format: YYYY-MM-DD. e.g `2022`, `2005-05`, `2005-01-01`</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("end_date")]
            public string? EndDate { get; set; }
#nullable restore
#else
            [QueryParameter("end_date")]
            public string EndDate { get; set; }
#endif
            /// <summary>Filter by genre(s) IDs. Can pass multiple with a comma as a delimiter. e.g 1,2,3</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("genres")]
            public string? Genres { get; set; }
#nullable restore
#else
            [QueryParameter("genres")]
            public string Genres { get; set; }
#endif
            /// <summary>Exclude genre(s) IDs. Can pass multiple with a comma as a delimiter. e.g 1,2,3</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("genres_exclude")]
            public string? GenresExclude { get; set; }
#nullable restore
#else
            [QueryParameter("genres_exclude")]
            public string GenresExclude { get; set; }
#endif
            /// <summary>Return entries starting with the given letter</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("letter")]
            public string? Letter { get; set; }
#nullable restore
#else
            [QueryParameter("letter")]
            public string Letter { get; set; }
#endif
            [QueryParameter("limit")]
            public int? Limit { get; set; }
            /// <summary>Set a maximum score for results</summary>
            [QueryParameter("max_score")]
            public double? MaxScore { get; set; }
            /// <summary>Set a minimum score for results.</summary>
            [QueryParameter("min_score")]
            public double? MinScore { get; set; }
            [QueryParameter("order_by")]
            public Anime_search_query_orderby? OrderBy { get; set; }
            [QueryParameter("page")]
            public int? Page { get; set; }
            /// <summary>Filter by producer(s) IDs. Can pass multiple with a comma as a delimiter. e.g 1,2,3</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("producers")]
            public string? Producers { get; set; }
#nullable restore
#else
            [QueryParameter("producers")]
            public string Producers { get; set; }
#endif
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("q")]
            public string? Q { get; set; }
#nullable restore
#else
            [QueryParameter("q")]
            public string Q { get; set; }
#endif
            [QueryParameter("rating")]
            public Anime_search_query_rating? Rating { get; set; }
            [QueryParameter("score")]
            public double? Score { get; set; }
            /// <summary>&apos;Safe For Work&apos;. This is a flag. When supplied it will filter out entries according to the SFW Policy. You do not need to pass a value to it. e.g usage: `?sfw`</summary>
            [QueryParameter("sfw")]
            public bool? Sfw { get; set; }
            [QueryParameter("sort")]
            public Search_query_sort? Sort { get; set; }
            /// <summary>Filter by starting date. Format: YYYY-MM-DD. e.g `2022`, `2005-05`, `2005-01-01`</summary>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
            [QueryParameter("start_date")]
            public string? StartDate { get; set; }
#nullable restore
#else
            [QueryParameter("start_date")]
            public string StartDate { get; set; }
#endif
            [QueryParameter("status")]
            public Anime_search_query_status? Status { get; set; }
            [QueryParameter("type")]
            public Anime_search_query_type? Type { get; set; }
            /// <summary>This is a flag. When supplied it will include entries which are unapproved. Unapproved entries on MyAnimeList are those that are user submitted and have not yet been approved by MAL to show up on other pages. They will have their own specifc pages and are often removed resulting in a 404 error. You do not need to pass a value to it. e.g usage: `?unapproved`</summary>
            [QueryParameter("unapproved")]
            public bool? Unapproved { get; set; }
        }
    }
}
