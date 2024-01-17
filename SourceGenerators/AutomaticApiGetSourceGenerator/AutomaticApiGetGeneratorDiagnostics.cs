using Microsoft.CodeAnalysis;

namespace AutomaticApiGetSourceGenerator;

public partial class AutomaticApiGetGenerator : IIncrementalGenerator
{
    private static readonly DiagnosticDescriptor InvalidReturnType = new(
        id: "InvalidReturnType",
        title: "InvalidReturnType",
        messageFormat: "Return type must be a generic IEnumerable of a single type.{0}",
        category: "Funtionality",
        defaultSeverity: DiagnosticSeverity.Error,
        isEnabledByDefault: true,
        description: "Method must return a generic enumerable of an object");

    private static readonly DiagnosticDescriptor InvalidParams = new(
        id: "InvalidParams",
        title: "InvalidParams",
        messageFormat: "Method must have at least 1 param and the first param should be the query model which will be automatically created as partial class.{0}",
        category: "Funtionality",
        defaultSeverity: DiagnosticSeverity.Error,
        isEnabledByDefault: true,
        description: "Method must have at least 1 param and the first param should be the query model which will be automatically created as partial class.{0}");
}