using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Swashbuckle.AspNetCore.Annotations;
using AruppiApi.Api.Shared;
using AruppiApi.Database;
using ApiGetGenerator;
using Riok.Mapperly.Abstractions;
using JikanClient;
using JikanClient.Models.Response;
using JikanClient.Models.Request;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace AruppiApi.Api.v0.Controllers.Default;

[ApiV0]
[ApiController]
public partial class SchedulesController : BaseController<SchedulesController>
{
    public DatabaseContext DatabaseContext { get; set; }
    public JikanApiClient JikanApiClient { get; set; }
    public SchedulesController(DatabaseContext DatabaseContext, JikanApiClient JikanApiClient, IServiceProvider IServiceProvider) : base(IServiceProvider)
    {
        this.DatabaseContext = DatabaseContext;
        this.JikanApiClient = JikanApiClient;
    }

    [HttpGet]
    [SwaggerOperation(Summary = "GetSchedulesFilteredByDayOfWeek")]
    public async Task<IEnumerable<ScheduleSlim>> Get([FromQuery]GetSchedulesRequest request, CancellationToken cancellationToken = default)
    {
        var response = await JikanApiClient.SchedulesGet(request, cancellationToken);

        var slim = response.Data.Select(i => i.ToDto());

        return slim;
    }
}

[Mapper]
public static partial class ScheduleMapper
{
    [MapProperty(nameof(@GetScheduleResponse.Images.Webp.LargeImageUrl), nameof(@ScheduleSlim.Image))]
    public static partial ScheduleSlim ToDto(this GetScheduleResponse src);
}

public class ScheduleSlim
{
    public long MalId { get; set; }
    public string Title { get; set; }
    public string Image { get; set; }
}