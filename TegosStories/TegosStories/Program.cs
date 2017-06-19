using System;
using System.IO;
using System.Collections.Generic;
using NSoup;
using Newtonsoft.Json;
using static TegosStories.Logger;

namespace TegosStories
{
    class Program
    {
        static void Main(string[] args)
        {
            const string log = "./log.txt";
            new FileInfo(log).Create().Close();
            var lggr = new Logger(log);
            lggr.Log(Level.Info, "Starting systems...");
            const string mid = "&pg=2&cid=", url = "http://chateg.ru/stories/?p=";
            lggr.Log(Level.Info, $"MIDDLE : {mid} || URL : {url}");
            Console.WriteLine("Введите 'cid' страницы: ");
            var cid = Console.ReadLine();
            lggr.Log(Level.Info, $"cid : {cid} || Номер категории. Может приводить к различным проблемам. Должно быть числом.");
            Console.WriteLine("Введите номер последней страницы: ");
            var last = int.Parse(Console.ReadLine());
            lggr.Log(Level.Info, $"last : {last} || Номер последней страницы. Может приводить к различным проблемам. Должно быть числом.");

            var Stories = new Stories(DateTime.Now.ToLongDateString() + " " + DateTime.Now.ToLongTimeString(), cid);
            lggr.Log(Level.Info, "Создан экземпляр внутреннего класса <Stories>");

            lggr.Log(Level.Info, "Начинаем работать...");
            for (var i = 1; i <= last; i++)
            {
                double perx = GetPercent(last, i);
                lggr.Log(Level.Info, $"Выполнено {perx}%");
                Console.WriteLine($"Выполнено {perx}%");
                try
                {
                    var toload = url + i + mid + cid;
                    foreach (var Element in NSoupClient.Connect(toload).Get().Select("ul.vo").Select("li"))
                    {
                        Stories.addStory(Element.Text());
                    }
                    lggr.Log(Level.Info, $"{Stories.ListOfStories.Count} историй сграблено%");
                }
                catch (Exception e)
                {
                    Console.WriteLine(e.Message);
                    lggr.Log(Level.Warning, e.Message);
                    lggr.Log(Level.Error, e.StackTrace);
                }
            }
            var serialized = JsonConvert.SerializeObject(Stories);
            new FileInfo($"./[{cid}]stories.json").Create().Close();
            var serializer = new StreamWriter($"./[{cid}]stories.json");
            lggr.Log(Level.Info, "Создан сериализатор...");
            lggr.Log(Level.Info, "Сериализуем...");
            Console.WriteLine("Сериализуем...");
            serializer.WriteLine(serialized);
            serializer.Close();
            lggr.Log(Level.Info, "Сериализованно!");
            Console.WriteLine("Работа завершена!");
            lggr.Log(Level.Info, "Работа завершена!");
            lggr.Close();
            Console.ReadKey();
        }

        public static Int32 GetPercent(Int32 b, Int32 a)
        {
            if (b == 0) return 0;
            return (Int32)(a / (b / 100M));
        }
    }

    internal class Logger
    {
        private readonly StreamWriter _file;

        public Logger(string filename)
        {
            _file = new StreamWriter(filename, true, System.Text.Encoding.Default);
        }

        public void Log(Level lv, string text)
        {
            var tmp = "";
            switch (lv)
            {
                case Level.Info:
                    tmp = $"{DateTime.Today.ToLongDateString()} {DateTime.Now.ToLongTimeString()} [INFO] {text}";
                    break;
                case Level.Warning:
                    tmp = $"{DateTime.Today.ToLongDateString()} {DateTime.Now.ToLongTimeString()} [WARNING] {text}";
                    break;
                case Level.Error:
                    tmp = $"{DateTime.Today.ToLongDateString()} {DateTime.Now.ToLongTimeString()} [ERROR] {text}";
                    break;
                default:
                    tmp = $"{DateTime.Today.ToLongDateString()} {DateTime.Now.ToLongTimeString()} [WARNING] Log level is wrong. How you did it?";
                    break;
            }
            _file.WriteLine(tmp);
        }

        public void Close()
        {
            _file.Close();
        }

        public enum Level
        {
            Warning,
            Error,
            Info
        }
    }

    class Stories
    {
        public string Date { get; }
        public string Cid { get; }
        public List<string> ListOfStories { get; }

        public Stories(string date, string cid)
        {
            Date = date;
            Cid = cid;
            ListOfStories = new List<string>();
        }

        public void addStory(string storyText)
        {
            ListOfStories.Add(storyText.Remove(0,6).Trim());
        }
    }
}
