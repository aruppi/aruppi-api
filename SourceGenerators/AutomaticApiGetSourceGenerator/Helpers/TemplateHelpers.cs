using AutomaticApiGetSourceGenerator;
using Microsoft.CodeAnalysis;
using System.Text;
using System.Text.RegularExpressions;

namespace SourceGeneratorHelpers;

public static class TemplateHelpers
{
    private static readonly Type AssemblyType = typeof(AutomaticApiGetGenerator);

    public static Stream GetEmbedFile(string filename)
    {
        const string folderName = "EmbedResources";
        // this may fail if the type in AssemblyType doesn't belong to the default namespace of the package
        var path = $"{AssemblyType.Namespace}.{folderName}.{filename}";
        var asm = AssemblyType.Assembly;
        var resource = asm.GetManifestResourceStream(path);
        return resource;
    }

    private static readonly Dictionary<string, string> FileCache = new();

    public static string GetEmbedFileAsString(string filename)
    {
        if (FileCache.TryGetValue(filename, out var value))
        {
            return value;
        }

        using var embedFile = GetEmbedFile(filename);
        using var reader = new StreamReader(embedFile);
        string text = reader.ReadToEnd();

        FileCache.Add(filename, text);
        return text;
    }

    public static string FillTemplateFromFile(string filename, Dictionary<string, string> replacements)
    {
        var template = GetEmbedFileAsString(filename);

        return FillTemplate(template, replacements);
    }

    public static string FillTemplate(string template, Dictionary<string, string> replacements)
    {
        return Regex.Replace(template, @"Template(.+?)Template", m =>
        {
            if (!replacements.TryGetValue(m.Groups[1].Value, out var value))
            {
                return "ERROR PATTERN NOT FOUND";
            }

            return value;
        });
    }

    public static void AddStaticFile(this IncrementalGeneratorPostInitializationContext context, string filename)
    {
        var templateString = GetEmbedFileAsString(filename);

        var outputName = filename.Replace(".cs", ".generated.cs");

        context.AddSource(outputName, templateString);
    }

    public static void AddTemplate(this IncrementalGeneratorPostInitializationContext context, string filename, string discriminator, Dictionary<string, string> replacements)
    {
        var source = FillTemplateFromFile(filename, replacements);

        if (discriminator is not null)
        {
            filename = GetDiscriminatedName(filename, discriminator);
        }

        context.AddSource(filename, source);
    }

    public static void AddTemplate(this SourceProductionContext context, string filename, string? discriminator, Dictionary<string, string> replacements)
    {
        var source = FillTemplateFromFile(filename, replacements);

        if (discriminator is not null)
        {
            filename = GetDiscriminatedName(filename, discriminator);
        }

        context.AddSource(filename, source);
    }

    public static string GetDiscriminatedName(string filename, string discriminator)
    {
        discriminator = string.Concat(discriminator.Split(Path.GetInvalidFileNameChars()));

        filename = filename.Replace(".cs", $".{discriminator}.cs");
        filename = filename.Replace(".cs", ".generated.cs");

        return filename;
    }

    public static StringBuilder Indent(this StringBuilder sb, int ammount)
    {
        return sb.Append('\t', ammount);
    }
}