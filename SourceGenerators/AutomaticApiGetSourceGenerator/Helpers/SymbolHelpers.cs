using Microsoft.CodeAnalysis;

namespace SourceGeneratorHelpers;

public static class SymbolHelpers
{
    public static string GetFullyQualifiedName(this ISymbol symbol)
    {
        string name;
        if (symbol is INamedTypeSymbol namedSymbol)
        {
            name = namedSymbol.GetUnderlyingNullableName();
        }
        else
        {
            name = symbol.Name;
        }

        return $"{symbol.ContainingNamespace}.{name}";
    }

    public static string GetFullyQualifiedGenericsString(this INamedTypeSymbol symbol)
    {
        var @base = symbol.GetFullyQualifiedName();

        var typeArguments = symbol.TypeArguments;

        var genericStringComponents = new List<string>();

        foreach (var type in typeArguments)
        {
            if (type is INamedTypeSymbol subSymbol)
            {
                var subgenerics = subSymbol.GetFullyQualifiedGenericsString();

                if (subgenerics.Length > 0)
                {
                    genericStringComponents.Add(subgenerics);
                }
            }
        }

        if (genericStringComponents.Count < 1)
        {
            return @base;
        }

        var joined = string.Join(", ", genericStringComponents);

        return $"{@base}<{joined}>";
    }

    public static string GetUnderlyingNullableName(this INamedTypeSymbol symbol)
    {
        if (!symbol.IsNullable())
        {
            return symbol.Name;
        }

        return symbol.TypeArguments[0].Name;
    }

    public static bool InheritFrom(this ITypeSymbol symbol, string fullyQualifiedName)
    {
        while (symbol.BaseType is not null)
        {
            symbol = symbol.BaseType;

            var name = symbol.ToString();

            if (name == fullyQualifiedName)
            {
                return true;
            }
        }

        return false;
    }

    public static bool HasInterface(this ITypeSymbol symbol, string name)
    {
        if (symbol is INamedTypeSymbol named)
        {
            if (named.IsNullable())
            {
                symbol = named.TypeArguments[0];
            }
        }

        var indexOfGeneric = name.IndexOf('`');

        name = name.Substring(0, indexOfGeneric);

        if (symbol.AllInterfaces.Any(i => i.Name == name))
        {
            return true;
        }

        return false;
    }

    public static bool IsNullable(this ITypeSymbol symbol)
    {
        var name = typeof(Nullable<>).Name;
        var indexOfGeneric = name.IndexOf('`');
        name = name.Substring(0, indexOfGeneric);

        return symbol.Name == name;
    }

    public static bool IsIAsyncResult(this ITypeSymbol symbol)
    {
        return symbol.AllInterfaces.Any(i => i.Name == typeof(IAsyncResult).Name);
    }

    public static bool IsIEquatable(this ITypeSymbol symbol)
    {
        return symbol.HasInterface(typeof(IEquatable<>).Name);
    }

    public static bool IsIComparable(this ITypeSymbol symbol)
    {
        return symbol.HasInterface(typeof(IComparable<>).Name);
    }

    public static bool IsDictionary(this ITypeSymbol symbol)
    {
        return symbol.HasInterface(typeof(IDictionary<,>).Name);
    }

    public static bool IsEnumerable(this ITypeSymbol symbol)
    {
        return symbol.HasInterface(typeof(IEnumerable<>).Name);
    }
    
    public static bool IsBoolean(this ITypeSymbol symbol)
    {
        return symbol.GetFullyQualifiedName() == "System.Boolean";
    }

    public static bool IsString(this ITypeSymbol symbol)
    {
        return symbol.GetFullyQualifiedName() == "System.String";
    }

    public static bool IsPartiallyUdaptableClass(this ITypeSymbol symbol)
    {
        return symbol.TypeKind == TypeKind.Class
            && !symbol.IsString()
            && !symbol.IsEnumerable();
    }

    public static bool HasAttribute(this ITypeSymbol typeSymbol, string name)
    {
        var attrs = typeSymbol.GetAttributes();
        foreach (var attr in attrs)
        {
            if (attr.AttributeClass?.Name == name)
            {
                return true;
            }
        }

        return false;
    }
}
