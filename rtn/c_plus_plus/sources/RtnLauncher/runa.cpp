//------------------------------------------------------
#include "stdafx.h"
#include "runa.h"

#include "..\common\Tracing.h"
#include "..\common\FileHelpers.h"
#include "..\common\TextHelpers.h"
#include "..\common\TimeHelpers.h"
#include "..\common\network\WinHttpWrapper.h"
#include "..\common\encode\base64.h"

#include "kerberos.h"
#include "Main.h"

//------------------------------------------------------

bool	g_bLoggingEnabled = false;

//------------------------------------------------------

bool IsLoggingEnabled()
{
	return g_bLoggingEnabled;
}

//------------------------------------------------------

void DumpRequestStr(const std::string& out, const int nVal)
{
	const std::wstring sFile = nVal > 0 ?
		StdString::FormatW(L"last_http_request_%02d.log", nVal).c_str() : L"last_http_request.log";

	CMyFile f;
	f.AppendTextWithOpeningFile(GetExeRelativePath(sFile), out);
	f.close();
}

//------------------------------------------------------

void DumpRequest(const REQUEST_RESULTS& results, const int nVal)
{
	{
		REQUEST_RESULT res = results.GetLastResult();
		MYTRACE("DownloadFromUrl got status code: %d; body size: %d bytes\n", res.dwHttpStatusCode, res.sBinaryReplyBody.size());
	}

	if(!IsLoggingEnabled())
		return;

	const std::string sEol = "\r\n";
	std::string out;

	for(size_t i = 0; i < results.vectRedirects.size(); i++)
	{
		REQUEST_RESULT res = results.vectRedirects[i];

		out += UnicodeToUtf8(res.sRequestHeaders);
		out += sEol;
		out += sEol;
		out += res.request.sBinaryRequestBody;
		out += sEol;
		out += sEol;
		out += UnicodeToUtf8(res.sReplyHeaders);
		out += sEol;
		out += sEol;
		out += res.sBinaryReplyBody;
		out += sEol;
		out += "=========================================================================================" + sEol;
		out += sEol;
	}

	DumpRequestStr(out, nVal);
}

//------------------------------------------------------

bool GetXmlStringValue(const std::string& sXml, const std::string& sXmlTagName,
	OUT std::string& sValue, const std::string& sDefaultValue)
{
	const std::string sNotExistValue = "<not-exist-KS0j3zJLXGfrFRs-c-Sergey-Kolomenkin>";

	sValue = ExpandStringByOuterBorders(sXml, "<"+sXmlTagName+">", "</"+sXmlTagName+">", sNotExistValue);
	if (sValue == sNotExistValue)
	{
		sValue = sDefaultValue;
		return false;
	}
	return true;
}

std::wstring GetWebServiceURL(std::wstring serviceName) {
	const std::wstring serverName = GetOption(L"server.name", L"localhost");
	const std::wstring serverPort = GetOption(L"server.port", L"8080");
	const std::wstring serverVersion = GetOption(L"server.version", L"4.0.6");
	const std::wstring serverType = GetOption(L"application.server.type", L"jboss4");
	if (L"jboss4" == serverType) {
		return L"http://" + serverName + L":" + serverPort + L"/runawfe-wfe-service-" + serverVersion + L"/" + serviceName + L"ServiceBean?wsdl";
	} else {
		return L"http://" + serverName + L":" + serverPort + L"/wfe-service-" + serverVersion + L"/" + serviceName + L"WebService/" + serviceName + L"API?wsdl";
	}
}

int IsThereAnyTask() {
	MYTRACE("Checking for new tasks ...\n");

	bool bRes = false;
	HttpSessionPtr ptrSession = HttpSession::Create();
	if(!ptrSession) {
		MYTRACE("Can't create session!\n");
		return -1;
	}

	const std::wstring sUserAgent = GetOption(L"UserAgent");
	if(!sUserAgent.empty())	{
		ptrSession->SetUserAgent(sUserAgent);
	}

	{
		// Update logging variable:
		const std::wstring sLogFile = GetOption(L"log_file");
		g_bLoggingEnabled = !sLogFile.empty();
	}

	if(IsLoggingEnabled())
	{
		// Empty files:
		DumpRequestStr("", 0);
		DumpRequestStr("", 1);
	}

	//------------------------------------------------------
	// GET KERBEROS BINARY TICKET:

	const std::wstring sKerberosServerName = GetOption(L"KerberosServerName", L"WFServer");
	const std::string Ticket = GetKerberosTicket(sKerberosServerName);
	if(Ticket.empty())
	{
		MYTRACE("GetKerberosTicket failed!\n");
		return -1;
	}

	// NOTE! It may be important to keep one connection for Kerberos communication!
	HttpConnectionPtr ptrConnection;
	std::string sRunaAuthData;
	bool bFoundTasks = false;

	//------------------------------------------------------
	// POST HTTP SOAP REQUEST TO LOGIN INTO RUNA:

	{
		const std::string sPostXml =
			"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.service.wfe.runa.ru/\">"
			"<SOAP-ENV:Header/><SOAP-ENV:Body>"
			"<impl:authenticateByKerberos><token>"
			+ base64_encode(Ticket, UNLIMITED_LINE_LENGTH) +
			"</token></impl:authenticateByKerberos>"
			"</SOAP-ENV:Body></SOAP-ENV:Envelope>";

		REQUEST_INFO request(VERB_POST, GetWebServiceURL(L"Authentication"));
		request.sBinaryRequestBody = sPostXml;
		request.sContentType = L"text/xml; charset=utf-8";

		REQUEST_RESULTS results;

		ptrSession->AddBrowserHeader(L"SOAPAction: \"\"");

		bRes = ptrSession->DownloadFromUrl(request, results, ptrConnection, true, 5);
		if(!bRes)
		{
			MYTRACE("ERROR: DownloadFromUrl failed: %d\n", GetLastError());
			return -1;
		}

		const REQUEST_RESULT& res = results.GetLastResult();
		// -------------------------------
		DumpRequest(results, 0);
		GetXmlStringValue(res.sBinaryReplyBody, "return", sRunaAuthData, "");
	}

	//------------------------------------------------------

	if(sRunaAuthData.empty())
	{
		MYTRACE("ERROR: Authentication data was not found in SOAP reply!\n");
		return -1;
	}

	//------------------------------------------------------
	// REQUEST TASK LIST FROM RUNA:

	{
		const std::string sPostXml =
			"<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:impl=\"http://impl.service.wfe.runa.ru/\">"
			"<SOAP-ENV:Header/><SOAP-ENV:Body>"
			"<impl:getTasks><user>"
			+ sRunaAuthData +
			"</user></impl:getTasks>"
			"</SOAP-ENV:Body></SOAP-ENV:Envelope>";

		REQUEST_INFO request(VERB_POST, GetWebServiceURL(L"Execution"));
		request.sBinaryRequestBody = sPostXml;
		request.sContentType = L"text/xml; charset=utf-8";

		REQUEST_RESULTS results;

		ptrSession->AddBrowserHeader(L"SOAPAction: \"\"");

		bRes = ptrSession->DownloadFromUrl(request, results, ptrConnection, true, 5);
		if(!bRes)
		{
			MYTRACE("ERROR: DownloadFromUrl failed: %d\n", GetLastError());
			return -1;
		}

		const REQUEST_RESULT& res = results.GetLastResult();
		// -------------------------------
		DumpRequest(results, 1);
		bFoundTasks = contains(res.sBinaryReplyBody, "</ns2:getTasksResponse>");

		if(!contains(res.sBinaryReplyBody, "<ns2:getTasksResponse"))
		{
			MYTRACE("ERROR: Bad getTasks reply!\n");
			return -1;
		}
	}

	if(bFoundTasks)
	{
		MYTRACE("Tasks found!\n");
		return TRUE;
	}

	MYTRACE("No task found\n");
	return FALSE;
}

//------------------------------------------------------
