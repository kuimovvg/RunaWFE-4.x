using Microsoft.Office.Interop.InfoPath;
using System;
namespace ru.runa.ipwctrl
{
    partial class InfoPathWrapperControl
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            try
            {
                if (formControl1 != null)
                {
                    if (formControl1.XmlForm != null)
                        formControl1.XmlForm.Close();
                    formControl1.Close();
                }
            }
            catch (Exception)
            { /* we can do almost nothing here*/ }

            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Component Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(InfoPathWrapperControl));
            this.formControl1 = new Microsoft.Office.InfoPath.FormControl();
            this.debugWindow = new System.Windows.Forms.TextBox();
            ((System.ComponentModel.ISupportInitialize)(this.formControl1)).BeginInit();
            this.SuspendLayout();
            // 
            // formControl1
            // 
            this.formControl1.Dock = System.Windows.Forms.DockStyle.Fill;
            this.formControl1.Enabled = true;
            this.formControl1.Location = new System.Drawing.Point(0, 0);
            this.formControl1.Name = "formControl1";
            this.formControl1.OcxState = ((System.Windows.Forms.AxHost.State)(resources.GetObject("formControl1.OcxState")));
            this.formControl1.Size = new System.Drawing.Size(800, 600);
            this.formControl1.TabIndex = 2;
            // 
            // debugWindow
            // 
            this.debugWindow.BackColor = System.Drawing.SystemColors.GradientActiveCaption;
            this.debugWindow.BorderStyle = System.Windows.Forms.BorderStyle.None;
            this.debugWindow.Dock = System.Windows.Forms.DockStyle.Bottom;
            this.debugWindow.Location = new System.Drawing.Point(0, 504);
            this.debugWindow.Multiline = true;
            this.debugWindow.Name = "debugWindow";
            this.debugWindow.ScrollBars = System.Windows.Forms.ScrollBars.Both;
            this.debugWindow.Size = new System.Drawing.Size(800, 96);
            this.debugWindow.TabIndex = 3;
            this.debugWindow.TabStop = false;
            this.debugWindow.Visible = false;
            this.debugWindow.WordWrap = false;
            // 
            // InfoPathWrapperControl
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.Controls.Add(this.debugWindow);
            this.Controls.Add(this.formControl1);
            this.Name = "InfoPathWrapperControl";
            this.Size = new System.Drawing.Size(800, 600);
            ((System.ComponentModel.ISupportInitialize)(this.formControl1)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private Microsoft.Office.InfoPath.FormControl formControl1;
        private System.Windows.Forms.TextBox debugWindow;
    }
}
