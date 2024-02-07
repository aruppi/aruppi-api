using Asp.Versioning;

namespace AruppiApi.Api.v5;

[AttributeUsage(AttributeTargets.Class, Inherited = false, AllowMultiple = true)]
public sealed class ApiV5Attribute : ApiVersionAttribute
{
    public static readonly ApiVersion Version = new(majorVersion: 5, minorVersion: 0, status: "alpha");
    public ApiV5Attribute() : base(version: Version)
    {

    }
}
