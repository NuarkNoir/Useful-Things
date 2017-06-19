using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;
using NSoup;
using NSoup.Nodes;
using NSoup.Select;
using System.Threading;
using Newtonsoft.Json;
using System.IO;

namespace Tests
{
    public partial class Form1 : Form
    {
        TextBox lv;
        TrashboxDump td;
        List<AndroidApp> programms = new List<AndroidApp>();
        List<AndroidApp> games = new List<AndroidApp>();

        public Form1()
        {
            InitializeComponent();
        }

        async void lookup()
        {
            int exceptions = 0;
            for (int i = 779; i != 0; i--)
            {
                Invoke(new MethodInvoker(() => printer("Добываем страницу номер " + i + "...")));
                try
                {
                    Document d = NSoupClient.Connect("https://trashbox.ru/public/progs/tags/os_android/page_topics/" + i.ToString()).Get();
                    d = NSoupClient.Parse(d.Select("div.div_content_cat_topics").Html());
                    Elements apps = d.Select("div.div_topic_cat_content");
                    foreach (Element app in apps)
                    {

                        String name;
                        List<string> tags = new List<string>();
                        name = app.Select("span.div_topic_tcapt_content").First().Text();
                        string version = app.Select("span.div_topic_cat_tag_os_android").First().Text();
                        version = version.Replace("Android ", "").Replace(" и выше", "");
                        Elements _tags = app.Select("div.div_topic_cat_tags a");
                        foreach (Element tag in _tags)
                        {
                            tags.Add(tag.Text());
                        }
                        programms.Add(new AndroidApp(name, tags, version));
                    }
                }
                catch (Exception ex)
                {
                    if (exceptions == 10)
                    {
                        Invoke(new MethodInvoker(() => printer("Случилось 10 ошибок! Завершаем работу...")));
                        td = new TrashboxDump(programms);
                        new FileStream("./TrashDUMP.json", FileMode.OpenOrCreate).Close();
                        File.WriteAllText("./TrashDUMP.json", JsonConvert.SerializeObject(td));
                        break;
                    }
                    Invoke(new MethodInvoker(() => printer("Ошибка при добыче страницы " + i + "!")));
                    Invoke(new MethodInvoker(() => printer(ex.Message)));
                    exceptions++;
                    continue;
                }
                finally
                {
                    Invoke(new MethodInvoker(() => pb.Value++));
                    td = new TrashboxDump(programms);
                }
            }
            td = new TrashboxDump(programms);
        }

        private void button1_Click(object sender, EventArgs e)
        {
            textBox1.AppendText("Начинаем работать...");
            Task t = new Task(lookup);
            t.Start();
            ((Button)sender).Enabled = false;
        }

        void printer(object o)
        {
            Invoke(new MethodInvoker(()=> textBox1.AppendText(Environment.NewLine + o)));
        }
    }
}
