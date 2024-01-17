using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc.Authorization;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;
using System.Text;
using AruppiApi.Startup.AuthenticationAndAuthorizationOptions;

namespace AruppiApi.Startup.Swagger;

public class SecurityRequirementsOperationFilter : IOperationFilter
{
    private readonly SwaggerGenOptions SwaggerGenOptions;
    public SecurityRequirementsOperationFilter(IConfiguration IConfiguration, IOptions<SwaggerGenOptions> SwaggerGenOptions)
    {
        this.SwaggerGenOptions = SwaggerGenOptions.Value;
    }

    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var policyNames = new List<string>();
        var roleNames = new List<string>();

        var metadata = context.ApiDescription.ActionDescriptor.EndpointMetadata;

        var authRequired = false;

        foreach (var item in metadata)
        {
            if (item is AuthorizeAttribute auth)
            {
                authRequired = true;
                if (auth.Policy is not null)
                {
                    policyNames.Add(auth.Policy);
                }
                if (auth.Roles is not null)
                {
                    roleNames.Add(auth.Roles);
                }
            }
        }

        var filter = context.ApiDescription.ActionDescriptor.FilterDescriptors;

        foreach (var item in filter)
        {
            if (item.Filter is AuthorizeFilter auth)
            {
                if (auth.AuthorizeData is null)
                {
                    continue;
                }

                foreach (var data in auth.AuthorizeData)
                {
                    authRequired = true;
                    if (data.Policy is not null)
                    {
                        policyNames.Add(data.Policy);
                    }
                }
            }
        }

        if (!authRequired)
        {
            // No AuthenticationRequieredWasFound
            return;
        }

        var responses = operation.Responses;

        responses.Add("401", new OpenApiResponse { Description = "Unauthorized" });
        responses.Add("403", new OpenApiResponse { Description = "Forbidden" });
        // Not requiered, in theory it should never happen
        // responses.Add("500", new OpenApiResponse { Description = "Internal Server Error" });

        var requirement = new OpenApiSecurityRequirement();

        var descriptionStringBuilder = new StringBuilder();

        if (operation.Description is not null)
        {
            descriptionStringBuilder.AppendLine(operation.Description);
        }

        const string BaseText = "Requiered Autorizations =>";

        descriptionStringBuilder.AppendLine(BaseText);

        var securityStringBuilder = new StringBuilder();

        if (policyNames.Count > 0)
        {
            securityStringBuilder.Append("Policies: ");
            for (int i = 0; i < policyNames.Count; i++)
            {
                if (i > 0)
                {
                    securityStringBuilder.Append(", ");
                }
                securityStringBuilder.Append(policyNames[i]);
            }
            securityStringBuilder.AppendLine();
        }

        if (roleNames.Count > 0)
        {
            securityStringBuilder.Append("Roles: ");
            for (int i = 0; i < roleNames.Count; i++)
            {
                if (i > 0)
                {
                    securityStringBuilder.Append(", ");
                }
                securityStringBuilder.Append(roleNames[i]);
            }
            securityStringBuilder.AppendLine();
        }

        if (securityStringBuilder.Length < 1)
        {
            securityStringBuilder.Append("Default Authorization");
        }

        descriptionStringBuilder.Append(securityStringBuilder);

        operation.Description = descriptionStringBuilder.ToString();

        var DefaultSchemaId = ConfiguredAuthenticationOptions.DefaultSchemaId;

        var schema = new OpenApiSecurityScheme
        {
            Reference = new OpenApiReference
            {
                Type = ReferenceType.SecurityScheme,
                Id = DefaultSchemaId,
            }
        };

        var securitySchemas = this.SwaggerGenOptions.SwaggerGeneratorOptions.SecuritySchemes;

        foreach (var securityScheme in securitySchemas)
        {
            requirement.Add(schema, Array.Empty<string>());
        }

        var security = operation.Security;
        security.Add(requirement);
    }
}