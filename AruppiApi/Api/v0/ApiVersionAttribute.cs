using Asp.Versioning;

namespace AruppiApi.Api.v0;

[AttributeUsage(AttributeTargets.Class, Inherited = false, AllowMultiple = true)]
public sealed class ApiV0Attribute : ApiVersionAttribute
{
    public static readonly ApiVersion Version = new(majorVersion: 0, minorVersion: 0, status: "alpha");
    public ApiV0Attribute() : base(version: Version)
    {

    }
}
