using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.ApplicationModels;
using Microsoft.AspNetCore.Mvc.Authorization;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Extensions.Options;

namespace AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

public class ConfigureDefaultAuthorizationFilterOptions : IConfigureNamedOptions<MvcOptions>
{
    public ConfigureDefaultAuthorizationFilterOptions() { }

    public void Configure(string? name, MvcOptions options)
    {
        Configure(options);
    }

    public void Configure(MvcOptions options)
    {
        options.Conventions.Add(new DefaultAuthorizationFilter());
    }
}

public class DefaultAuthorizationFilter : IControllerModelConvention
{
    public void Apply(ControllerModel controller)
    {
        // This makes sure that by default there's no exposed endpoints without auth
        // While also it tries to avoid overriding already configured settings

        if (controller.Attributes.Any(HasConfigurationAttribute))
        {
            // Do Nothing
            return;
        }

        foreach (var action in controller.Actions)
        {
            if (action.Attributes.Any(HasConfigurationAttribute))
            {
                // Do Nothing
                continue;
            }

            var filters = action.Filters;

            if (HasAuthorizeFilter(filters))
            {
                continue;
            }

            filters.Add(new AuthorizeFilter());
        }
    }

    private static bool HasAuthorizeFilter(IList<IFilterMetadata> filter)
    {
        foreach (var filterMetadata in filter)
        {
            if (filterMetadata is AuthorizeFilter)
            {
                return true;
            }
        }

        return false;
    }

    private static bool HasConfigurationAttribute(object attr)
    {
        return attr is AllowAnonymousAttribute || attr is AuthorizeAttribute;
    }
}
