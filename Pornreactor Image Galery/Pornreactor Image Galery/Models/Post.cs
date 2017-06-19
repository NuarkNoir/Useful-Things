using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Media.Imaging;

namespace TestEPF.Models
{
    class Post
    {
        public string Author { get; }
        public string imageURL { get; }
        public string Link { get; }
        public string tagsstred { get; }
        public List<string> tags;
        public BitmapImage bi3 { get; }

        public Post(string Link, string Author, string imageURL, List<string> tags)
        {
            this.Link = Link;
            this.Author = Author;
            this.imageURL = "http://dev.nuarknoir.h1n.ru/api/reactor/imgview.php?l=" + imageURL;
            this.tags = tags;
            bi3 = new BitmapImage();
            tagsstred = tags2str();
            generateBI();
        }

        private void generateBI()
        {
            bi3.BeginInit();
            bi3.UriSource = new Uri(imageURL);
            bi3.EndInit();
        }

        public string tags2str()
        {
            var tags2str = tags.Aggregate("", (current, tag) => current + (tag + ","));
            return tags2str.TrimEnd(',');
        }
    }
}
