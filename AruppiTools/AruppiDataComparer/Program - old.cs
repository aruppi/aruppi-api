//using AruppiDataComparer;
//using System.Text.Json;
//using System.Linq;
//using FuzzySharp;
//using System.Text.RegularExpressions;
//using System.Security.Cryptography.X509Certificates;
//using System;

//internal class Program
//{
//    private static JsonSerializerOptions JsonSerializerOptions = new JsonSerializerOptions()
//    {
//        NumberHandling = System.Text.Json.Serialization.JsonNumberHandling.AllowReadingFromString,
//    };

//    private static string currentDirectory = Directory.GetCurrentDirectory();
//    //var monos = LoadFromFile<List<MonoDataItem>>(Path.Combine(currentDirectory, "monos_data.json"));
//    public static List<TioDataItem> TioList = LoadFromFile<List<TioDataItem>>(Path.Combine(currentDirectory, "tio_data.json"));
//    public static List<MyAnimeListDataItem> MalList = MalJsonPreprocesor(Path.Combine(currentDirectory, "maldata_mod.json"));
//    public static Dictionary<string, List<MyAnimeListDataItem>> MalListByYear = new Dictionary<string, List<MyAnimeListDataItem>>();

//    private static T LoadFromFile<T>(string filePath)
//    {
//        using var fs = File.OpenRead(filePath);
//        return JsonSerializer.Deserialize<T>(fs, JsonSerializerOptions);
//    }

//    private static List<MyAnimeListDataItem> MalJsonPreprocesor(string path)
//    {
//        using var fs = File.OpenRead(path);
//        using var reader = new StreamReader(fs);

//        var raw = reader.ReadToEnd();

//        var replacements = new List<(string, string)>()
//        {
//            ("\"[]\"", "null"),
//            ("[]", "null"),
//            ("\"\"", "null"),
//            ("\"['", "[\""),
//            ("\"[\\\"", "[\""),
//            ("', '", "\", \""),
//            ("', \"", "\", \""),
//            ("\", '", "\", \""),
//            ("\", \"", "\", \""),
//            ("\\\", \"", "\", \""),
//            ("']\"", "\"]"),
//            ("\\\"]\"", "\"]"),
//        };

//        foreach (var replace in replacements)
//        {
//            raw = raw.Replace(replace.Item1, replace.Item2);
//        }

//        return JsonSerializer.Deserialize<List<MyAnimeListDataItem>>(raw, JsonSerializerOptions);
//    }
//    private static void Main(string[] args)
//    {
//        // Preprocess mal entries
//        foreach (var item in MalList)
//        {
//            // Preprocess titles
//            if (!string.IsNullOrEmpty(item.Title))
//            {
//                item.AllTitles.Add(item.Title);
//            }

//            if (!string.IsNullOrEmpty(item.TitleEnglish))
//            {
//                item.AllTitles.Add(item.TitleEnglish);
//            }

//            if (!string.IsNullOrEmpty(item.TitleJapanese))
//            {
//                item.AllTitles.Add(item.TitleJapanese);
//            }

//            if (item.TitleSynonyms != null)
//            {
//                item.AllTitles.AddRange(item.TitleSynonyms);
//            }

//            // Verify that they have at least one title

//            if (item.AllTitles.Count < 1)
//            {
//                throw new Exception();
//            }

//            if (item.PremieredYear == null)
//            {
//                if (!string.IsNullOrEmpty(item.AiredFrom))
//                {
//                    item.PremieredYear = item.AiredFrom.Split("-")[0];
//                }
//                else if (!string.IsNullOrEmpty(item.AiredTo))
//                {
//                    item.PremieredYear = item.AiredTo.Split("-")[0];
//                }
//                else
//                {
//                    item.PremieredYear = "";
//                }
//            }

//            if (!MalListByYear.TryGetValue(item.PremieredYear, out var value))
//            {
//                value = new List<MyAnimeListDataItem>()
//                {
//                    item
//                };

//                MalListByYear.Add(item.PremieredYear, value);
//            }
//            else
//            {
//                value.Add(item);
//            }
//        }

//        Console.WriteLine("oh");

//        var temp = TioList;

//        var data = new Dictionary<int, List<TioDataItem>>();

//        for (int i = 105; i >= 80; i--)
//        {
//            // for clarity
//            var minScore = i;

//            temp = temp.Where(item => item.MyAnimeListId.Count == 0).ToList();

//            Parallel.For(0, temp.Count, (index) =>
//            {
//                var tioItem = temp[index];

//                if (tioItem.Year == null)
//                {
//                    tioItem.Year = "";
//                }

//                if (MalListByYear.TryGetValue(tioItem.Year, out var animesThatYear))
//                {
//                    TioProcessing(tioItem, animesThatYear, minScore);
//                }
//            });

//            var found = temp.Where(item => item.MyAnimeListId.Count > 0).ToList();

//            data.Add(i, found);
//        }

//        temp = temp.Where(item => item.MyAnimeListId.Count == 0).ToList();

//        data.Add(0, temp);

//        foreach (var item in data)
//        {
//            Console.Write($@"
//Match percentage => {item.Key}
//    Too many matches => {item.Value.Where(item => item.MyAnimeListId.Count > 1).Count()}    
//    Good matches => {item.Value.Where(item => item.MyAnimeListId.Count == 1).Count()}
//    No matches (?) => {item.Value.Where(item => item.MyAnimeListId.Count < 1).Count()}

//");
//        }

//        var noMatch = temp.Where(item => item.MyAnimeListId.Count == 0).Count();
        

//        Console.WriteLine("it finished?");

//    }

//    public static void TioProcessing(TioDataItem item, List<MyAnimeListDataItem> dataset, int minScore)
//    {
//        testTitle(item.Title);
//        testTitle(item.OriginalTitleRaw);

//        void testTitle(string title)
//        {
//            if (title != null)
//            {
//                foreach (var malItem in dataset)
//                {
//                    var results = Process.ExtractAll(title, malItem.AllTitles);

//                    foreach (var result in results)
//                    {
//                        if (result.Score >= minScore)
//                        {
//                            item.MyAnimeListId.Add(malItem.MalId);
//                            break;
//                        }
//                    }
//                }
                
//            }
//        }
//    }
//}