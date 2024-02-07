using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace AruppiApi.Api.Shared;

// "Light Controller" May not extend Controller base
// Check Performance Implications
//
// services.AddHttpContextAccessor();
// var ctx = IServiceProvider.GetRequiredService<IHttpContextAccessor>();

[AllowAnonymous]
[Route("api/v{version:apiVersion}/[controller]")]
public abstract class BaseController<TController> : ControllerBase where TController : BaseController<TController>
{
    protected readonly ILogger<TController> Logger;
    protected readonly IServiceProvider IServiceProvider;

    public BaseController(IServiceProvider IServiceProvider)
    {
        this.IServiceProvider = IServiceProvider;
        this.Logger = IServiceProvider.GetRequiredService<ILogger<TController>>();
    }
}