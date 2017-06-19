using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Policy;
using System.Text;
using System.Threading.Tasks;

namespace Image_Processing
{
    class ImageItem
    {
        public string Name { get; set; }
        public string Path { get; set; }
        public int Id { get; set; }

        public ImageItem(string name, int id)
        {
            Path = name;
            var tmp = name.Split('/');
            tmp = tmp.Last().Split('.');
            Name = Uri.UnescapeDataString(tmp[tmp.Length-2]);
            Id = id;
        }
    }
}
