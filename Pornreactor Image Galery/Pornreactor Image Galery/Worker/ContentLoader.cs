using NSoup;
using System.Collections.Generic;
using System.Linq;
using TestEPF.Models;

namespace TestEPF.Worker
{
    internal class ContentLoader
    {
        private string currentpage;

        public ContentLoader()
        {
            currentpage = "";
        }

        public ContentLoader(int index)
        {
            currentpage = index.ToString();
        }

        public List<Post> LoadPosts()
        {
            var posts = new List<Post>();
            var doc = NSoupClient.Connect($"http://pornreactor.cc/{currentpage}").Timeout(10000).Get();
            var els = doc.Select("div.postContainer");
            foreach (var el in els)
            {
                var Link = "http://pornreactor.cc" + el.Select("span.link_wr a.link").First().Attr("href");
                var Author = el.Select(".uhead_nick a").First().Text();
                var tags = el.Select("h2.taglist b a").Select(tag => tag.Text()).ToList();
                foreach (var img in el.Select(".post_content img"))
                {
                    var imgurl = img.Attr("src");
                    posts.Add(new Post(Link, Author, imgurl, tags));
                }
            }
            return posts;
        }

        public int GetLastPageNum()
        {
            return int.Parse(NSoupClient.Connect("http://pornreactor.cc/").Timeout(10000).Get().Select(".pagination_expanded .current").First().Text());
        }
    }
}
