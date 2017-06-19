using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using TestEPF.Models;
using TestEPF.Worker;
using Button = System.Windows.Controls.Button;
using ListViewItem = System.Windows.Controls.ListViewItem;
using MessageBox = System.Windows.MessageBox;

namespace TestEPF
{
    public partial class MainWindow
    {
        private List<Post> posts;
        private ContentLoader cl;
        private int currpage = 0, lastpagenum = 0, offset = 0;

        public MainWindow()
        {
            InitializeComponent();
            try
            {
                Init();                
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message + "\n" + ex.StackTrace);
            }
        }

        void Init()
        {
            Directory.CreateDirectory("./downloads");
            cl = new ContentLoader();
            currpage = lastpagenum = cl.GetLastPageNum();
            pageLabel.Content = $"{currpage}/{lastpagenum}";
            cl = new ContentLoader(currpage);
            CLDispatcher(cl.LoadPosts());
        }

        void CLDispatcher(List<Post> posts)
        {
            List<ListViewItem> _posts = new List<ListViewItem>();
            foreach (var post in posts)
            {
                ListViewItem item = new ListViewItem()
                {
                    Content = post,
                    ContentTemplate = (DataTemplate)FindResource("itemTemplate")
                };
                _posts.Add(item);
            }
            listView.ItemsSource = _posts;
        }

        private void DownButton_OnClick(object sender, RoutedEventArgs e)
        {
            Post curr = (Post) ((StackPanel) ((Button) sender).Parent).DataContext;
            string filename = curr.tags.Aggregate("", (current, tag) => current + (tag + "-"));
            filename.Trim();
            filename += curr.imageURL.Split('-').Last();
            var wc = new WebClient();
            wc.DownloadFileAsync(new Uri(curr.imageURL), $"./downloads/{filename}");
        }

        private void loadPrevPage(object sender, RoutedEventArgs e)
        {
            var tmp = offset - 1;
            if (tmp < lastpagenum) return;
            currpage = lastpagenum - (offset--);
            cl = new ContentLoader(currpage);
            CLDispatcher(cl.LoadPosts());
            pageLabel.Content = $"{currpage}/{lastpagenum}";
        }

        private void loadNextPage(object sender, RoutedEventArgs e)
        {
            var tmp = offset + 1;
            if (tmp >= lastpagenum) return;
            currpage = lastpagenum - (offset++);
            cl = new ContentLoader(currpage);
            CLDispatcher(cl.LoadPosts());
            pageLabel.Content = $"{currpage}/{lastpagenum}";
        }
    }
}
