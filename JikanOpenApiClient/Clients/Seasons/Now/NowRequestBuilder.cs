// <auto-generated/>
using JikanRest.Models;
using Microsoft.Kiota.Abstractions.Serialization;
using Microsoft.Kiota.Abstractions;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;
using System;
namespace JikanRest.Seasons.Now {
    /// <summary>
    /// Builds and executes requests for operations under \seasons\now
    /// </summary>
    public class NowRequestBuilder : BaseRequestBuilder {
        /// <summary>
        /// Instantiates a new NowRequestBuilder and sets the default values.
        /// </summary>
        /// <param name="pathParameters">Path parameters for the request</param>
        /// <param name="requestAdapter">The request adapter to use to execute the requests.</param>
        public NowRequestBuilder(Dictionary<string, object> pathParameters, IRequestAdapter requestAdapter) : base(requestAdapter, "{+baseurl}/seasons/now{?filter*,limit*,page*,sfw*,unapproved*}", pathParameters) {
        }
        /// <summary>
        /// Instantiates a new NowRequestBuilder and sets the default values.
        /// </summary>
        /// <param name="rawUrl">The raw URL to use for the request builder.</param>
        /// <param name="requestAdapter">The request adapter to use to execute the requests.</param>
        public NowRequestBuilder(string rawUrl, IRequestAdapter requestAdapter) : base(requestAdapter, "{+baseurl}/seasons/now{?filter*,limit*,page*,sfw*,unapproved*}", rawUrl) {
        }
        /// <param name="cancellationToken">Cancellation token to use when cancelling requests</param>
        /// <param name="requestConfiguration">Configuration for the request such as headers, query parameters, and middleware options.</param>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public async Task<Pagination_plus?> GetAsync(Action<RequestConfiguration<NowRequestBuilderGetQueryParameters>>? requestConfiguration = default, CancellationToken cancellationToken = default) {
#nullable restore
#else
        public async Task<Pagination_plus> GetAsync(Action<RequestConfiguration<NowRequestBuilderGetQueryParameters>> requestConfiguration = default, CancellationToken cancellationToken = default) {
#endif
            var requestInfo = ToGetRequestInformation(requestConfiguration);
            return await RequestAdapter.SendAsync<Pagination_plus>(requestInfo, Pagination_plus.CreateFromDiscriminatorValue, default, cancellationToken).ConfigureAwait(false);
        }
        /// <param name="requestConfiguration">Configuration for the request such as headers, query parameters, and middleware options.</param>
#if NETSTANDARD2_1_OR_GREATER || NETCOREAPP3_1_OR_GREATER
#nullable enable
        public RequestInformation ToGetRequestInformation(Action<RequestConfiguration<NowRequestBuilderGetQueryParameters>>? requestConfiguration = default) {
#nullable restore
#else
        public RequestInformation ToGetRequestInformation(Action<RequestConfiguration<NowRequestBuilderGetQueryParameters>> requestConfiguration = default) {
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
        public NowRequestBuilder WithUrl(string rawUrl) {
            return new NowRequestBuilder(rawUrl, RequestAdapter);
        }
        public class NowRequestBuilderGetQueryParameters {
            /// <summary>Entry types</summary>
            [QueryParameter("filter")]
            public GetFilterQueryParameterType? Filter { get; set; }
            [QueryParameter("limit")]
            public int? Limit { get; set; }
            [QueryParameter("page")]
            public int? Page { get; set; }
            /// <summary>&apos;Safe For Work&apos;. This is a flag. When supplied it will filter out entries according to the SFW Policy. You do not need to pass a value to it. e.g usage: `?sfw`</summary>
            [QueryParameter("sfw")]
            public bool? Sfw { get; set; }
            /// <summary>This is a flag. When supplied it will include entries which are unapproved. Unapproved entries on MyAnimeList are those that are user submitted and have not yet been approved by MAL to show up on other pages. They will have their own specifc pages and are often removed resulting in a 404 error. You do not need to pass a value to it. e.g usage: `?unapproved`</summary>
            [QueryParameter("unapproved")]
            public bool? Unapproved { get; set; }
        }
    }
}
