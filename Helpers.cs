using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.Office.InfoPath;
using System.Xml.XPath;
using System.IO;
using System.Text.RegularExpressions;
using System.Net;
using System.Collections.Specialized;
using System.Runtime.InteropServices.ComTypes;
using System.Runtime.InteropServices;
using System.Globalization;

namespace ru.runa.ipwctrl
{
    class FormTemplateHelper
    {
        private string schemaFile = null;
        public string SchemaFile
        { get { return schemaFile; } }

        public class FieldDescr
        {
            public string name;
            public string type;
            public bool nillable;
        }

        private Dictionary<string, FieldDescr> fields = new Dictionary<string, FieldDescr>();
        public Dictionary<string, FieldDescr>  Fields
        { get {return fields; } }

        private string myfields = "myFields";
        public string myFields
        { get { return myfields; } }

        public FormTemplateHelper(FormTemplate template)
        {
            // first load all namespaces
            template.Manifest.MoveToChild(XPathNodeType.Element);

            // extract schema file name
            // FIXME There may be a number of xsd-files in the package.
            XPathNavigator xsdNav = template.Manifest.SelectSingleNode(
                "xsf:package/xsf:files/xsf:file[string-length(@name)>4 and substring(@name, string-length(@name)-3) = '.xsd']"
                , template.Manifest);

            schemaFile = xsdNav.SelectSingleNode("@name", template.Manifest).InnerXml;

            using (Stream s = template.OpenFileFromPackage(schemaFile))
            {
                XPathNavigator nav = (new XPathDocument(s)).CreateNavigator();
                nav.MoveToChild(XPathNodeType.Element);
                XPathNodeIterator iter = nav.Select("xsd:element[@type!='']", nav);

                while (iter.MoveNext())
                {
                    FieldDescr descr = new FieldDescr();
                    descr.name = iter.Current.GetAttribute("name", "");
                    descr.type = iter.Current.GetAttribute("type", "");
                    descr.nillable = "true".Equals(iter.Current.GetAttribute("nillable", ""));
                    fields.Add(descr.name, descr);
                }
            }

            myfields = xsdNav.SelectSingleNode(
                "xsf:fileProperties/xsf:property[@name = 'rootElement']/@value"
                , template.Manifest).InnerXml;
        }
    }


    class URLHelper
    {
        Uri documentUri;
        string jsessionid;

        public URLHelper(string documentURL)
        {
            documentUri = new Uri(documentURL);
            jsessionid = extract_jsessionid(documentUri.ToString());
        }

        public string ComputeFrom(string url)
        {
            Uri rel = new Uri(url, UriKind.Relative);
            Uri abs = new Uri(documentUri, rel);
            //return abs.AbsoluteUri;
            return embed_jsessionid(abs.AbsoluteUri);
	    //return embed_jsessionid(uri.ToString());
        }

        public string GetAuthority()
        { return documentUri.Authority;  }

        public string GetPath()
        {
            string res = "";
            for (int i = 0; i < documentUri.Segments.Length-1; i++)
                res += documentUri.Segments[i];
            return res;
        }

        private string extract_jsessionid(string url)
        {
            Match m = Regex.Match(url, ";jsessionid=[a-zA-Z0-9]+");
            if (m.Success)
                return m.Value;
            return null;
        }

        private string embed_jsessionid(string url)
        {
            if (jsessionid == null)
                return url;

            string[] tmp = url.Split(new char[] { '?' });

            if (tmp.Length == 1)
                return tmp[0] + jsessionid;
            if (tmp.Length == 2)
                return tmp[0] + jsessionid + "?" + tmp[1];

            throw new Exception("Unexpected URL:" + url);
        }
    }

    class WebClientWrapper
    {
        string jsessionid;
        string domain;
        string path;

        // marker interface
        public interface POSTData
        {}

        public class Param : POSTData
        {
            public string value;

            public override string ToString()
            {
                return "value: " + value;
            }
        }

        public class FileAttachment : POSTData
        {
            public string file;
            public byte[] data;

            public override string ToString()
            {
                return "name: " + file + "; length: " + (data != null ? data.Length : 0);
            }
        }

        public WebClientWrapper(string jsessionid, string domain, string path)
        {
            this.jsessionid = jsessionid;
            this.domain = domain;
            this.path = path;
        }

        public string executeGetRequest(string url, NameValueCollection values)
        {
            WebClient client = new WebClient();
            if (jsessionid != null)
                client.Headers.Add("Cookie", "JSESSIONID=" + jsessionid);
            client.Encoding = System.Text.Encoding.UTF8;
            string query = "";
            foreach (string key in values.Keys)
                foreach (string value in values.GetValues(key))
                    query += "&" + Uri.EscapeUriString(key) + "=" + Uri.EscapeUriString(value);
            if (url.IndexOf('?') < 0)
                query = "?" + query.Substring(1);

            url += query;

            string s = client.DownloadString(url);
            return s;
        }

        public HttpWebResponse executePostRequest(string url, Dictionary<String, POSTData> values)
        {
            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            string boundary = "---------------------" + DateTime.Now.Ticks.ToString("x", NumberFormatInfo.InvariantInfo);
            request.ContentType = "multipart/form-data; boundary=" + boundary;
            request.Method = WebRequestMethods.Http.Post;
            request.CookieContainer = new CookieContainer();
            if (jsessionid != null)
                request.CookieContainer.Add(new Cookie("JSESSIONID", jsessionid, path, domain));

            MemoryStream data = preparePostData(values, boundary);

            request.ContentLength = data.Length;

            Stream s1 = request.GetRequestStream();
            try
            {
                data.WriteTo(s1);
            }
            finally
            {
                s1.Close();
                data.Close();
            }

            HttpWebResponse response = (HttpWebResponse)request.GetResponse();
            return response;
        }

        private static MemoryStream preparePostData(Dictionary<String, POSTData> values, string boundary)
        {
            MemoryStream stream = new MemoryStream();
            BinaryWriter wr = new BinaryWriter(stream);

            foreach (string key in values.Keys)
            {
                POSTData value = null;
                values.TryGetValue(key, out value);
                if (value is Param)
                {
                    Param param = (Param)value;
                    wr.Write(toByteArray("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + key + "\"\r\n\r\n" + param.value + "\r\n"));
                }
                else if (value is FileAttachment)
                {
                    FileAttachment file = (FileAttachment)value;
                    wr.Write(toByteArray("--" + boundary + "\r\nContent-Disposition: form-data; name=\"" + key + "\"; filename=\"" + file.file + "\"\r\n\r\n"));
                    wr.Write(file.data);
                    wr.Write(toByteArray("\r\n"));
                }
                else
                {
                    throw new Exception("invalid multipart content: " + value);
                }
            }
            wr.Write(toByteArray("--" + boundary + "--"));
            return stream;
        }

        private static byte[] toByteArray(string p)
        {
            return Encoding.UTF8.GetBytes(p);
        }

        #region URL moniker support (required to make HTTP-requests on behalf of the browser)

        /*
            IStream stream = null;
            Urlmon.URLOpenBlockingStreamW(this, url, out stream, 0, IntPtr.Zero);

            System.Runtime.InteropServices.ComTypes.STATSTG streamStats = new System.Runtime.InteropServices.ComTypes.STATSTG();
            stream.Stat(out streamStats, 0);
            byte[] buf = new byte[streamStats.cbSize];
            stream.Read(buf, buf.Length, IntPtr.Zero);
            return System.Text.Encoding.UTF8.GetString(buf);
        */
        /*
        class Urlmon
        {
            [DllImport("urlmon.dll")]
            extern static public Int32 URLOpenBlockingStreamW(
            [MarshalAs(UnmanagedType.IUnknown)] object pCaller,
            [MarshalAs(UnmanagedType.LPWStr)]   string szURL,
            out IStream stream, Int32 dwReserved, IntPtr lpfnCB);
        }
        */


        #endregion
    }
}
