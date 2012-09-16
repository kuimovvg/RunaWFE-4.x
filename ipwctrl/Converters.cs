using System.Collections.Generic;
using System;
using System.Globalization;
using System.Text;
namespace ru.runa.ipwctrl
{

    interface Converter
    {
        string ConvertFromWFEToInfopath(string value);
        WebClientWrapper.POSTData ConvertFromInfopathToWFE(string value);
    }

    abstract class ParamConverter : Converter
    {
        public string ConvertFromWFEToInfopath(string value)
        {
            if (value == "")
                return value;
            return ConvertFromWFEToInfopathImpl(value);
        }

        abstract protected string ConvertFromWFEToInfopathImpl(string value);

        public WebClientWrapper.POSTData ConvertFromInfopathToWFE(string value)
        {
            WebClientWrapper.Param param = new WebClientWrapper.Param();
            if (value == "")
                param.value = value;
            else
                param.value = ConvertFromInfopathToWFEImpl(value);
            return param;
        }

        abstract protected string ConvertFromInfopathToWFEImpl(string value);
    }


    // WFE: dd.MM.yyyy
    // InfoPath: yyyy-MM-dd
    class DateConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            string[] s = value.Split(new char[] { '.' });
            return s[2] + "-" + s[1] + "-" + s[0];
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            string[] s = value.Split(new char[] { '-' });
            return s[2] + "." + s[1] + "." + s[0];
        }
    }

    // WFE: H:mm
    // InfoPath: hh:mm:ss+/-hh:mm
    class TimeConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            string[] s = value.Split(new char[] { ':' });
            if (s[0].Length == 1)
                s[0] = "0" + s[0];
            if (s[1].Length == 1)
                s[1] = "0" + s[1];
            return s[0] + ":" + s[1] + ":00";
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            string[] s = value.Split(new char[] { ':' });
            return s[0] + ":" + s[1];
        }
    }

    // WFE: "dd.MM.yyyy H:mm"
    // InfoPath: "yyyy-MM-ddThh:mm:ss"
    class DateTimeConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            string[] s = value.Split(new char[] { '.', ' ', ':' });
            if (s[3].Length == 1)
                s[3] = "0" + s[3];
            if (s[4].Length == 1)
                s[4] = "0" + s[4];
            return s[2] + "-" + s[1] + "-" + s[0] + "T" + s[3] + ":" + s[4] + ":00";
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            string[] s = value.Split(new char[] { '-', 'T', ':' });
            return s[2] + "." + s[1] + "." + s[0] + " " + s[3] + ":" + s[4];
        }
    }

    // FIXME Handle TRUE/FALSE vs 1/0 issue!
    class BooleanConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            return value;
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            if ("1" == value || "true" == value.ToLower())
                return "true";
            else
                return "false";
        }
    }

    // TODO: check all that '.' vs ',' delimiter problems
    class DoubleConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            Double d = Double.Parse(value);
            return d.ToString(CultureInfo.InvariantCulture);
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            Double d = Double.Parse(value, CultureInfo.InvariantCulture);
            return d.ToString();
        }
    }

    // xsd:string, my:requiredString, xsd:integer
    class DoNothingConverter : ParamConverter
    {
        protected override string ConvertFromWFEToInfopathImpl(string value)
        {
            return value;
        }

        protected override string ConvertFromInfopathToWFEImpl(string value)
        {
            return value;
        }
    }

    // For a description of infopath format for storing files see
    // http://msdn2.microsoft.com/en-us/library/bb608318(office.11).aspx
    class FileConverter : Converter
    {
        public string ConvertFromWFEToInfopath(string value)
        {
            // skip empty ones
            if (value == "")
                return value;

            // suspect this is a file from a WFE server
            byte[] tmp = Convert.FromBase64String(value);
            int index = 0;
            for (; index < tmp.Length; index++)
            {
                if (tmp[index] == 0)
                    break;
            }
            if (index == tmp.Length)
                // give up, this is not a file from the WFE server
                return value;

            string fileName;
            try
            {
               fileName = Encoding.UTF8.GetString(tmp, 0, index);
            }
            catch (Exception)
            {
                // give up again
                return value;
            }

            // assume FileAttachment control

            int dataSize = tmp.Length - index - 1;
            int fileNameSize = fileName.Length * 2 + 2;
            int headerSize = 24;
            byte[] result = new byte[dataSize + fileNameSize + headerSize];
            index++;

            // signature
            result[0] = 0xC7;
            result[1] = 0x49;
            result[2] = 0x46;
            result[3] = 0x41;

            // header size
            result[4] = 0x14;
            result[5] = 0x00;
            result[6] = 0x00;
            result[7] = 0x00;

            // version
            result[8] = 0x01;
            result[9] = 0x00;
            result[10] = 0x00;
            result[11] = 0x00;

            // reserved
            result[12] = 0x00;
            result[13] = 0x00;
            result[14] = 0x00;
            result[15] = 0x00;

            // file size
            result[16] = (byte)dataSize;
            result[17] = (byte)(dataSize >> 8);
            result[18] = (byte)(dataSize >> 16);
            result[19] = (byte)(dataSize >> 24);

            // fileName size
            result[20] = (byte)(fileNameSize >> 1);
            result[21] = (byte)(fileNameSize >> 9);
            result[22] = (byte)(fileNameSize >> 17);
            result[23] = (byte)(fileNameSize >> 25);

            // fileName
            byte[] fileNameBytes = Encoding.Unicode.GetBytes(fileName);
            for (int i = 0; i < fileNameBytes.Length; i++)
                result[24 + i] = fileNameBytes[i];
            result[24 + fileNameBytes.Length] = 0;
            result[24 + fileNameBytes.Length + 1] = 0;

            // fileData
            for (int i = 0; i < dataSize; i++)
                result[24 + i + fileNameBytes.Length + 2] = tmp[index + i];

            // base64 encode
            return Convert.ToBase64String(result);
        }

        public WebClientWrapper.POSTData ConvertFromInfopathToWFE(string value)
        {
            WebClientWrapper.FileAttachment fileAttachment = new WebClientWrapper.FileAttachment();
            fileAttachment.file = "";
            fileAttachment.data = new byte[0];
            if (value != "")
                parse(fileAttachment, value);
            return fileAttachment;
        }

        private void parse (WebClientWrapper.FileAttachment fileAttachement, string infopathValue)
        {
            byte[] value = Convert.FromBase64String(infopathValue);
            // assume FileAttachement

            if (value.Length < 20)
                return;
            // signature
            if (value[0] != 0xC7
                || value[1] != 0x49
                || value[2] != 0x46
                || value[3] != 0x41)
                return;
            // header size
            if (value[4] != 0x14
                || value[5] != 0x00
                || value[6] != 0x00
                || value[7] != 0x00)
                return;
            // version
            if (value[8] != 0x01
                || value[9] != 0x00
                || value[10] != 0x00
                || value[11] != 0x00)
                return;
            // reserved
                //value[12]
                //value[13]
                //value[14]
                //value[15]
            // file size
            int dataSize = (int)value[16] + ((int)value[17] << 8) + ((int)value[18] << 16) + ((int)value[19] << 24);
            if (dataSize < 0)
                return;
            int fileNameSize = (int)value[20] + ((int)value[21] << 8) + ((int)value[22] << 16) + ((int)value[23] << 24);
            fileNameSize *= 2;
            if (fileNameSize < 2)
                return;
            if (fileNameSize + dataSize + 24 != value.Length)
                return;
            fileAttachement.file = Encoding.Unicode.GetString(value, 24, fileNameSize - 2);
            fileAttachement.data = new byte[dataSize];
            for (int i = 0; i < dataSize; i++)
                fileAttachement.data[i] = value[24 + fileNameSize + i];
            // success
        }
    }


    class ConverterFactory
    {
        private static Dictionary<string, Converter> converters;

        private static void init()
        {
            if (converters != null)
                return;
            converters = new Dictionary<string, Converter>();
            converters.Add("xsd:date", new DateConverter());
            converters.Add("xsd:time", new TimeConverter());
            converters.Add("xsd:dateTime", new DateTimeConverter());
            converters.Add("xsd:boolean", new BooleanConverter());
            converters.Add("xsd:double", new DoubleConverter());
            converters.Add("xsd:string", new DoNothingConverter());
            converters.Add("my:requiredString", new DoNothingConverter());
            converters.Add("xsd:integer", new DoNothingConverter());
            converters.Add("xsd:base64Binary", new FileConverter());
            converters.Add("my:requiredBase64Binary", new FileConverter());
        }

        public static Converter GetConverter(string infopathType)
        {
            init();
            Converter result;
            if (!converters.TryGetValue(infopathType, out result))
                throw new ArgumentException("can't find converter for '" + infopathType + "' type");
            return result;
        }
    }
}