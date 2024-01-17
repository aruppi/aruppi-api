using AruppiDataComparer;
using System.Text.Json;
using System.Linq;
using FuzzySharp;
using System.Text.RegularExpressions;
using System.Security.Cryptography.X509Certificates;
using System;

internal class Program
{
    private static JsonSerializerOptions JsonSerializerOptions = new JsonSerializerOptions()
    {
        NumberHandling = System.Text.Json.Serialization.JsonNumberHandling.AllowReadingFromString,
    };

    private static string currentDirectory = Directory.GetCurrentDirectory();
    //var monos = LoadFromFile<List<MonoDataItem>>(Path.Combine(currentDirectory, "monos_data.json"));
    public static List<TioDataItem> TioList = LoadFromFile<List<TioDataItem>>(Path.Combine(currentDirectory, "tio_data.json"));
    public static MyAnimeListDatabase MyAnimeListDatabase = LoadFromFile<MyAnimeListDatabase>(Path.Combine(currentDirectory, "anime.json"));
    public static List<MyAnimeListItem> MalList => MyAnimeListDatabase.Data;

    public static Dictionary<long, List<MyAnimeListItem>> MalListByYear = new Dictionary<long, List<MyAnimeListItem>>();

    private static T LoadFromFile<T>(string filePath)
    {
        using var fs = File.OpenRead(filePath);
        return JsonSerializer.Deserialize<T>(fs, JsonSerializerOptions);
    }

    private static void Main(string[] args)
    {
        // Preprocess mal entries
        foreach (var item in MalList)
        {
            // Preprocess titles
            if (!string.IsNullOrEmpty(item.Title))
            {
                item.AllTitles.Add(item.Title);
            }

            if (!string.IsNullOrEmpty(item.TitleEnglish))
            {
                item.AllTitles.Add(item.TitleEnglish);
            }

            if (!string.IsNullOrEmpty(item.TitleJapanese))
            {
                item.AllTitles.Add(item.TitleJapanese);
            }

            if (item.TitleSynonyms != null)
            {
                item.AllTitles.AddRange(item.TitleSynonyms);
            }

            // Verify that they have at least one title

            if (item.AllTitles.Count < 1)
            {
                throw new Exception();
            }

            if (item.StartYear == null)
            {
                if (!string.IsNullOrEmpty(item.RealStartDate))
                {
                    item.StartYear = long.Parse(item.RealStartDate.Split("-")[0]);
                }
                else if (!string.IsNullOrEmpty(item.RealEndDate))
                {
                    item.StartYear = long.Parse(item.RealEndDate.Split("-")[0]);
                }
                else
                {
                    item.StartYear = -1;
                }
            }

            if (!MalListByYear.TryGetValue(item.StartYear.Value, out var value))
            {
                value = new List<MyAnimeListItem>()
                {
                    item
                };

                MalListByYear.Add(item.StartYear.Value, value);
            }
            else
            {
                value.Add(item);
            }
        }

        Console.WriteLine("Starting");

        var temp = TioList;

        var data = new Dictionary<int, List<TioDataItem>>();

        for (int i = 101; i >= 90; i--)
        {
            // for clarity
            var minScore = i;

            temp = temp.Where(item => item.MyAnimeListId.Count == 0).ToList();

            Parallel.For(0, temp.Count, (index) =>
            {
                var tioItem = temp[index];

                if (tioItem.Year == null)
                {
                    tioItem.Year = -1;
                }

                if (MalListByYear.TryGetValue(tioItem.Year.Value, out var animesThatYear))
                {
                    TioProcessing(tioItem, animesThatYear, minScore);
                }
            });

            var found = temp.Where(item => item.MyAnimeListId.Count > 0).ToList();

            data.Add(i, found);
        }

        temp = temp.Where(item => item.MyAnimeListId.Count == 0).ToList();

        data.Add(-1, temp);

        foreach (var item in data)
        {
            Console.Write($@"
Match percentage => {item.Key}
    Too many matches => {item.Value.Where(item => item.MyAnimeListId.Count > 1).Count()}    
    Good matches => {item.Value.Where(item => item.MyAnimeListId.Count == 1).Count()}
    No matches (?) => {item.Value.Where(item => item.MyAnimeListId.Count < 1).Count()}
");
        }

        var noMatch = temp.Where(item => item.MyAnimeListId.Count == 0).Count();

        Console.WriteLine("it finished?");

    }

    public static void TioProcessing(TioDataItem item, List<MyAnimeListItem> dataset, int minScore)
    {
        var titles = new List<string>();

        if (item.Title != null)
        {
            titles.Add(item.Title);
        }

        if (item.OriginalTitleRaw != null)
        {
            titles.Add(item.OriginalTitleRaw);
        }

        if (titles.Count < 1)
        {
            throw new Exception("Error in tio database");
        }

        if (minScore == 101)
        {
            foreach (var malItem in dataset)
            {
                foreach (var malTitle in malItem.AllTitles)
                {
                    foreach (var localTitle in titles)
                    {
                        if (localTitle == malTitle)
                        {
                            item.MyAnimeListId.Add(malItem.AnimeId);
                            goto next;
                        }
                    }
                }
            next:;
            }
        }
        else
        {
            foreach (var malItem in dataset)
            {
                foreach (var localTitle in titles)
                {
                    var results = Process.ExtractAll(localTitle, malItem.AllTitles);

                    foreach (var result in results)
                    {
                        if (result.Score >= minScore)
                        {
                            item.MyAnimeListId.Add(malItem.AnimeId);
                            goto next2;
                        }
                    }
                }
            next2:;
            }
        }
        
    }
}