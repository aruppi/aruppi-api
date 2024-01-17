namespace AruppiApi.Startup;

public static class Shared
{
    public static TSettings GetConfig<TSettings>(this IConfiguration IConfiguration, string currentSection)
    {
        var parentGroup = IConfiguration.GetRequiredSection(currentSection);

        var settingsType = typeof(TSettings);

        var currentGroup = parentGroup.GetRequiredSection(settingsType.Name);

        var settings = currentGroup.Get<TSettings>()!;

        return settings;
    }
}
