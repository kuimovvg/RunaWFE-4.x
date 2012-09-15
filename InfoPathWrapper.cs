using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Drawing;
using System.Data;
using System.Text;
using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Security;
using System.Security.Permissions;
using System.Xml.XPath;
using System.Xml;
using Microsoft.Office.Interop.InfoPath;
using Microsoft.Office.InfoPath;
using System.Net;
using System.Runtime.InteropServices.ComTypes;
using System.IO;
using Microsoft.Win32;
using System.Web;
using System.Globalization;
using System.Text.RegularExpressions;
using System.Collections.Specialized;

#region Assembly level directives

// General Information
[assembly: AssemblyTitle("InfoPathWrapperLibrary")]
[assembly: AssemblyDescription("")]
[assembly: AssemblyConfiguration("")]
[assembly: AssemblyCompany("")]
[assembly: AssemblyProduct("InfoPathWrapperLibrary")]
[assembly: AssemblyCopyright("Copyright ©  2007")]
[assembly: AssemblyTrademark("")]
[assembly: AssemblyCulture("")]

// COM
[assembly: ComVisible(true)]
[assembly: Guid("2b586510-20b1-4e00-9479-ebcfe7f14650")]

// Version
[assembly: AssemblyVersion("1.0.*")]
[assembly: AssemblyFileVersion("1.0.0.0")]

// This is required in order to run in the context of Internet Explorer
[assembly: AllowPartiallyTrustedCallersAttribute()]

#endregion

namespace ru.runa.ipwctrl
{
    [Guid("c5cca6cd-8f5b-4417-ab7f-422f1d354782")]
    [ClassInterface(ClassInterfaceType.AutoDispatch)]
    [PermissionSet(SecurityAction.Assert, Unrestricted=true)]
    public partial class InfoPathWrapperControl : UserControl, ISubmitToHostEventHandler, IInitEventHandler
    {
        private bool formTemplateLoaded = false;

        public InfoPathWrapperControl()
        {
            try
            {
                // Workaround
                // System.ArgumentException: Absolute path information is required
                // http://blogs.msdn.com/jpsanders/archive/2007/12/18/system-argumentexception-absolute-path-information-is-required.aspx
                AppDomain.CurrentDomain.SetData(".appVPath", "/");
                //

                InitializeComponent();
                setDebugOptions();
                formControl1.SetInitEventHandler(this);
                formControl1.SetSubmitToHostEventHandler(this);
            }
            catch (Exception e)
            { throw e; }

        }

        #region Security Asserts
        protected override void WndProc(ref Message m)
        { base.WndProc(ref m); }
        #endregion

        #region Debug options

        private void setDebugOptions()
        {
            if(isDebugEnabled())
                debugWindow.Show();
        }

        private bool isDebugEnabled()
        {
            bool debugEnabled = false;
            RegistryKey rkHKLM = Registry.LocalMachine;
            RegistryKey rkDebug = null;

            try
            {
                rkDebug = rkHKLM.OpenSubKey("Software\\Microsoft\\.NETFramework", false);
                if ((int)rkDebug.GetValue("DebugIEHost") != 0)
                    debugEnabled = true;
            }
            catch
            { /* ok, no debugging */ }
            if (rkDebug != null)
                rkDebug.Close();
            rkHKLM.Close();

            return debugEnabled;
        }

        #endregion

        #region Properties set as parameters on object tag

        private string templateURL;
        public string TemplateURL {
            set
            {
                templateURL = value;
                trace("TemplateURL: " + templateURL);
            }
            get { return templateURL; }
        }

        private string loadFromURL = null;
        public string LoadFromURL
        {
            set
            {
                loadFromURL = value;
                trace("LoadFromURL: " + loadFromURL);
            }
            get { return loadFromURL;  }
        }

        private string submitToURL = null;
        public string SubmitToURL
        {
            set
            {
                submitToURL = value;
                trace("SubmitToURL: " + submitToURL);
            }
            get { return submitToURL; }
        }

        #endregion

        #region Properties set from javascript

        public string DocumentURL
        {
            set
            {
                trace("DocumentURL: " + value);
                formControl1.Host = value;
                this.urlhelper_ = new URLHelper(value);
                UpdateCookies(documentCookie);
                Init();
            }
            get {
                return (formControl1.Host != null ? formControl1.Host.ToString() : "");
            }
        }

        private string documentCookie;
        public string DocumentCookie
        {
            set
            {
                documentCookie = value;
                trace("DocumentCookie: " + value);
                if(urlhelper_ != null)
                    UpdateCookies(value);
            }
            get
            {
                return "not impl";
            }
        }

        #endregion

        // This helper parses and caches important values declared in a form-template for quick access
        private FormTemplateHelper template_ = null;
        // This helper is used to compute absolute URLs
        private URLHelper urlhelper_ = null;
        //
        WebClientWrapper webclient = null;

        private void Init()
        {
            this.Cursor = Cursors.WaitCursor;
            //debugWindow.Text = "";
            try
            {
                loadTemplate(urlhelper_.ComputeFrom(templateURL));
                if (loadFromURL != null)
                    loadData(urlhelper_.ComputeFrom(loadFromURL));
		        formTemplateLoaded = true;
            }
            finally
            {
                this.Cursor = Cursors.Default;
            }
        }

        public void UpdateCookies(string documentCookie) {
            if (documentCookie != null)
            {
                Match m = Regex.Match(documentCookie, "JSESSIONID=[a-zA-Z0-9]+");
                if (m.Success)
                {
                    webclient = new WebClientWrapper(m.Value.Substring(11), urlhelper_.GetAuthority(), urlhelper_.GetPath());
                    return;
                }
            }
            webclient = new WebClientWrapper(null, null, null);
        }

        private void loadTemplate(string url)
        {
            trace("Loading InfoPath form template from: " + url);
            try
            {
                template_ = null;
                formControl1.Close();
                formControl1.NewFromFormTemplate(url);
                template_ = new FormTemplateHelper(formControl1.XmlForm.Template);
            }
            catch (Exception e)
            {
                trace(e.ToString());
                formControl1.Close();
                throw e;
            }
            trace("InfoPath form template loaded");
        }

        private void loadData(string url)
        {
            trace("Loading InfoPath form data from: " + url);
            try
            {
                string variablesXML = QueryVariables(url);
                XPathNodeIterator variablesIter = new XPathDocument(new StringReader(variablesXML)).CreateNavigator().Select("/vars/v");
                XPathNavigator fieldsNavigator = formControl1.XmlForm.MainDataSource.CreateNavigator();
                while (variablesIter.MoveNext())
                {
                    string name = variablesIter.Current.GetAttribute("name", "");
                    string value = variablesIter.Current.GetAttribute("value", "");

                    XPathNavigator fieldNavigator = fieldsNavigator.SelectSingleNode(
                        // TODO extract exact namespace prefix from form-template
                        "/my:" + template_.myFields + "/my:" + name, formControl1.XmlForm.NamespaceManager);

                    setFieldValue(fieldNavigator, value);
                }
            }
            catch (Exception e)
            {
                trace(e.ToString());
                throw e;
            }
            trace("InfoPath form data loaded");
        }

        private string QueryVariables(string url)
        {
            NameValueCollection query = new NameValueCollection();
            foreach (string variable in template_.Fields.Keys)
                query.Add("v", variable);

            trace("\tURL: " + url);

            string[] values = query.GetValues("v");
            if (values != null)
                foreach (string q in values)
                    trace("\t\tvariable: " + q);

            trace("\tExecuting request...");
            string xml = webclient.executeGetRequest(url, query);
            trace("\tRequest result: " + System.Environment.NewLine + xml);
            return xml;
        }

        private void setFieldValue(XPathNavigator nav, string value)
        {
            string name = nav.LocalName;
            trace("\tsetting field '" + name + "' with value '" + value + "'");

            FormTemplateHelper.FieldDescr field = null;
            if (value != "" && template_.Fields.TryGetValue(name, out field))
            {
                value = ConverterFactory.GetConverter(field.type).ConvertFromWFEToInfopath(value);

                //Remove the "nil" attribute as described in http://support.microsoft.com/kb/826998
                if (nav.MoveToAttribute("nil", "http://www.w3.org/2001/XMLSchema-instance"))
                {
                    trace("\t\tfound nil attribute, deleting...");
                    nav.DeleteSelf();
                }

                try
                {
                    nav.SetValue(value);
                }
                catch (InvalidOperationException e)
                {
                    // InfoPath validation error, ignore it for now
                    trace("\t\t" + e.ToString());
                }
            }
        }

        public bool ValidateForm()
        {
            trace("Validating form, number of errors: " + formControl1.XmlForm.Errors.Count);
            reportErrors(formControl1.XmlForm.Errors.GetErrors(FormErrorType.SchemaValidation));
            reportErrors(formControl1.XmlForm.Errors.GetErrors(FormErrorType.SystemGenerated));
            reportErrors(formControl1.XmlForm.Errors.GetErrors(FormErrorType.UserDefined));
            return formControl1.XmlForm.Errors.Count == 0;
        }

        private HttpWebResponse SubmitFormInternal()
        {
            {
                XPathNavigator nav = formControl1.XmlForm.MainDataSource.CreateNavigator();
                trace("Submitting InfoPath form:" + System.Environment.NewLine + nav.OuterXml);
            }
            try
            {
                // FIXME hidden inputs (like "id") may interfere with business process variable with the same name
                // TODO introduce isolated namespaces?
                XPathNodeIterator iter = GetXPathNodesIterator(formControl1.XmlForm.MainDataSource.CreateNavigator());
                Dictionary<String, WebClientWrapper.POSTData> fields = new Dictionary<String, WebClientWrapper.POSTData>();
                while (iter.MoveNext())
                {
                    FormTemplateHelper.FieldDescr f = new FormTemplateHelper.FieldDescr();
                    string fieldName = iter.Current.LocalName;
                    if (template_.Fields.TryGetValue(fieldName, out f))
                    {
                        WebClientWrapper.POSTData value = ConverterFactory.GetConverter(f.type).ConvertFromInfopathToWFE(iter.Current.InnerXml);
                        fields.Add(fieldName, value);
                        trace(fieldName + ": " + value);
                    }
                    else
                        trace("\tError getting type information for " + fieldName);
                }
                trace("Total " + iter.Count + " field(s)");
                return webclient.executePostRequest(urlhelper_.ComputeFrom(submitToURL), fields);
            }
            catch (Exception e)
            {
                trace(e.ToString());
                return null;
            }
        }

        public string SubmitForm()
        {
            try
            {
                HttpWebResponse response = SubmitFormInternal();
                if (response.StatusCode != HttpStatusCode.OK)
                    throw new Exception("HTTP status code: " + response.StatusCode);
                string redirect = response.ResponseUri.ToString();
                trace("Redirect: " + redirect);
                return redirect;
            }
            catch (Exception e)
            {
                trace(e.ToString());
                return "";
            }
        }

        public bool SubmitFormRtn()
        {
            try
            {
                HttpWebResponse response = SubmitFormInternal();
                return response.StatusCode == HttpStatusCode.OK;
            }
            catch (Exception e)
            {
                trace(e.ToString());
                return false;
            }
        }

        private XPathNodeIterator GetXPathNodesIterator(XPathNavigator nav)
        {
            // TODO extract correct namespace prefix from form-template
            return nav.Select("//my:*[count(./child::*)=0]", formControl1.XmlForm.NamespaceManager);
        }

        private void reportErrors(FormError[] errors)
        {
            foreach (FormError e in errors)
                trace("[" + e.FormErrorType + ", " + e.ErrorCode + "] " + e.Name + ", " + e.Site.Name + ", " + e.Message + ", " + ", " + e.DetailedMessage);
        }

        private void trace(string s)
        {
            if (!isDebugEnabled())
                return;

            const int MAX_CHARS = 65536;
            if (s.Length > MAX_CHARS)
            {
                string tail = "... (message trucated, total chars: " + s.Length + ")" ;
                s = s.Substring(0, MAX_CHARS - tail.Length) + tail;
            }
            string time = DateTime.Now.ToLocalTime().ToLongTimeString();
            debugWindow.AppendText(System.Environment.NewLine + time + " | " + s);
        }

        #region IInitEventHandler

        public void InitEventHandler(object sender, XmlForm xmlForm, out XdReadOnlyViewMode viewsReadOnlyMode)
        {
            trace("Init handler started [Used for XmlDataSource URL's changing]");
            IEnumerator enumerator = xmlForm.DataSources.GetEnumerator();
            while (enumerator.MoveNext())
            {
                DataSource ds = (DataSource) enumerator.Current;
                if (ds.Name.StartsWith("ru.runa"))
                {
                    trace("DataSource found to replace: " + ds.Name);
                    FileQueryConnection conn = (FileQueryConnection) ds.QueryConnection;
                    // FIXME unsecuredAction.do is not a good way to do...
                    String url = urlhelper_.ComputeFrom("/wfe/unsecuredGetActors.do?dataSource=" + ds.Name);
                    conn.FileLocation = url;
                    trace("DataSource url changed to: " + conn.FileLocation);
                    conn.Execute();
                }
            }
            viewsReadOnlyMode = XdReadOnlyViewMode.xdDefault;
            trace("Init handler ended");
        }

        #endregion

        #region ISubmitToHostEventHandler

        int ISubmitToHostEventHandler.SubmitToHostEventHandler(object sender, string adapterName, out string errorMessage)
        {
            errorMessage = "Submit buttons are not allowed in Runa WFE forms!";
            return 0;
        }
        
        #endregion
	
	// functions for RTN
        public void CloseForm()
        {
            try
            {
                if (formControl1 != null)
                {
                    if (formControl1.XmlForm != null)
                        formControl1.XmlForm.Close();
                    //formControl1.Close();
                    loadFromURL = null;
                    templateURL = null;
                    trace("Form closed");
                }
                formTemplateLoaded = false;
            }
            catch (Exception e)
            {
                throw e;
            }
        }

        public string OpenDummyForm(string templatePath)
        {
            try
            {
                template_ = null;
                formControl1.Close();
                formControl1.NewFromFormTemplate(templatePath);
                if (formControl1.XmlForm != null)
                    formControl1.XmlForm.Close();
                return "OK";
            }
            catch (Exception e)
            {
                return e.Message;
            }
        }

        public bool isFormTemplateLoaded()
        {
            return formTemplateLoaded;
        }

    }
}

