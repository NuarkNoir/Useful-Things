namespace Image_Processing
{
    partial class Form1
    {
        /// <summary>
        /// Обязательная переменная конструктора.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Освободить все используемые ресурсы.
        /// </summary>
        /// <param name="disposing">истинно, если управляемый ресурс должен быть удален; иначе ложно.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Код, автоматически созданный конструктором форм Windows

        /// <summary>
        /// Требуемый метод для поддержки конструктора — не изменяйте 
        /// содержимое этого метода с помощью редактора кода.
        /// </summary>
        private void InitializeComponent()
        {
            this.imageList = new System.Windows.Forms.ListBox();
            this.picture = new System.Windows.Forms.PictureBox();
            this.loader_button = new System.Windows.Forms.Button();
            this.fbd = new System.Windows.Forms.FolderBrowserDialog();
            ((System.ComponentModel.ISupportInitialize)(this.picture)).BeginInit();
            this.SuspendLayout();
            // 
            // imageList
            // 
            this.imageList.FormattingEnabled = true;
            this.imageList.Location = new System.Drawing.Point(12, 12);
            this.imageList.Name = "imageList";
            this.imageList.Size = new System.Drawing.Size(282, 472);
            this.imageList.TabIndex = 0;
            this.imageList.Click += new System.EventHandler(this.imageList_Click);
            this.imageList.DoubleClick += new System.EventHandler(this.imageList_DoubleClick);
            // 
            // picture
            // 
            this.picture.Location = new System.Drawing.Point(300, 12);
            this.picture.Name = "picture";
            this.picture.Size = new System.Drawing.Size(579, 498);
            this.picture.SizeMode = System.Windows.Forms.PictureBoxSizeMode.CenterImage;
            this.picture.TabIndex = 1;
            this.picture.TabStop = false;
            // 
            // loader_button
            // 
            this.loader_button.Location = new System.Drawing.Point(12, 490);
            this.loader_button.Name = "loader_button";
            this.loader_button.Size = new System.Drawing.Size(282, 20);
            this.loader_button.TabIndex = 3;
            this.loader_button.Text = "Загрузить";
            this.loader_button.UseVisualStyleBackColor = true;
            this.loader_button.Click += new System.EventHandler(this.loader_button_Click);
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(891, 522);
            this.Controls.Add(this.loader_button);
            this.Controls.Add(this.picture);
            this.Controls.Add(this.imageList);
            this.Name = "Form1";
            this.Text = "Form1";
            ((System.ComponentModel.ISupportInitialize)(this.picture)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.ListBox imageList;
        private System.Windows.Forms.PictureBox picture;
        private System.Windows.Forms.Button loader_button;
        private System.Windows.Forms.FolderBrowserDialog fbd;
    }
}

