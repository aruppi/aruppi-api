using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Swashbuckle.AspNetCore.Annotations;
using AruppiApi.Api.Shared;
using AruppiApi.Database;
using ApiGetGenerator;

namespace AruppiApi.Api.v0.Controllers.Default;

[ApiV0]
[ApiController]
public partial class SampleController : BaseController<SampleController>
{
    public DatabaseContext DatabaseContext { get; set; }
    public SampleController(DatabaseContext DatabaseContext, IServiceProvider IServiceProvider) : base(IServiceProvider)
    {
        this.DatabaseContext = DatabaseContext;
    }

    [HttpGet]
    [AllowAnonymous]
    [SwaggerOperation(Summary = "GetShowList", Description = "Sample Description")]
    [GenerateGetAttribute(InyectedDatabaseContextName = nameof(SampleController.DatabaseContext))]
    public partial Task<Page<Database.Models.Show>> AutoGet([FromQuery] ShowTempQuery? query = null);

    [HttpGet(nameof(SampleEndpointWithNoConfiguration))]
    [SwaggerOperation(Summary = "UpdateAvailableShows", Description = "Sample Description")]
    public async Task SampleEndpointWithNoConfiguration(CancellationToken cancellationToken = default)
    {
    }

    [Authorize(Policy = "SamplePolicy, Other", Roles = "SampleRole, Others")]
    [HttpGet(nameof(SampleEndpointWithCustomPoliciesAndRoles))]
    [SwaggerOperation(Summary = "UpdateAvailableShows", Description = "Sample Description")]
    public async Task SampleEndpointWithCustomPoliciesAndRoles(CancellationToken cancellationToken = default)
    {
    }
}
