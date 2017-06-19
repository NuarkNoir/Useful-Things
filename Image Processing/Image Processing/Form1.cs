using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace Image_Processing
{
    public partial class Form1 : Form
    {
        List<ImageItem> images = new List<ImageItem>();
        List<string> acceptableExtansions = new List<string>{ "gif", "png", "jpg", "jpeg" };

        public Form1()
        {
            InitializeComponent();
        }

        private void loader_button_Click(object sender, EventArgs e)
        {
            try
            {
                var path = "./images/";
                fbd.Description = @"Выберите папку с изображениями";
                var res = fbd.ShowDialog();
                if (res == DialogResult.OK || res == DialogResult.Yes) path = fbd.SelectedPath;
                else
                {
                    MessageBox.Show(@"Выберите папку!");
                    loader_button_Click(sender, e);
                }
                var dir = new DirectoryInfo(path);
                if (!dir.Exists) throw new IOException("Директория не существует!");
                foreach (var file in dir.GetFiles())
                {
                    //if (acceptableExtansions.Contains(file.Extension.ToLower()))
                    images.Add(new ImageItem(path + "/" + file.Name, int.Parse(file.CreationTime.ToString("ffffff"))));
                }
                foreach (var image in images)
                {
                    imageList.Items.Add(image.Name);
                }
                new DirectoryInfo(dir.Parent.FullName + "/to_delete").Create();
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex);
                throw;
            }
            finally
            {
                fbd.Reset();
                fbd.Dispose();
            }
        }

        private void imageList_Click(object sender, EventArgs e)
        {
            var lb = (ListBox) sender;
            picture.Load(images[lb.SelectedIndex].Path);
        }

        private void imageList_DoubleClick(object sender, EventArgs e)
        {
            var lb = (ListBox)sender;
            lb.SelectedIndex++;
            imageList_Click(sender, e);
            picture.Image = null;
            picture.Refresh();
            var f = new FileInfo(images[lb.SelectedIndex].Path);
            lb.ClearSelected();
            new DirectoryInfo(f.Directory.Parent.FullName + "/to_delete").Create();
            f.MoveTo(f.Directory.Parent.FullName + "/to_delete");
        }
    }
}
