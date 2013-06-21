// WinHttpWrapper.cpp: implementation of the WinHttpWrapper class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#include "stdafx.h"
#include "WinHttpWrapper.h"

#include "WinHttpCookies.h"

#include <WinHttp.h>
#pragma comment(lib, "WinHttp.lib")

#include "..\..\common\TimeHelpers.h"
#include "..\..\common\Tracing.h"
#include "..\..\common\TextHelpers.h"
#include "..\..\common\FileHelpers.h"
#include "..\..\common\array_ptr.h"

#include "..\..\common\debug\SmartProfiler.h"
// #include "..\..\common\encode\crypt_macro2.h"
// #include "..\..\common\compress\CompressHelpers.h"

// =====================================
// STUBS:
#define STR_CRYPT2_str(str)	str
#define CSW(str)			str
//#define PROFILER
//#define PROFILE(str)		str
// =====================================

#ifdef UNITTEST_DEPENDENCY
#include "..\..\common\UnitTest\UnitTest.h"
IMPLEMENT_MODULE(WinHttpWrapper)
#endif

#define MANUAL_REDIRECT

//------------------------------------------------------

//#define WRITE_SPEED_LOG

//------------------------------------------------------

#ifdef WRITE_SPEED_LOG

class HttpSpeedLogger
{
public:
	HttpSpeedLogger()
	{
		CHAR buf[MAX_PATH] = "";
		GetModuleFileNameA(NULL, buf, MAX_PATH);
		PathRenameExtension(buf, ".speed.log");
		m_sFileName = buf;
		QueryPerformanceFrequency(&m_liFreq);
		QueryPerformanceCounter(&m_liPrevTime);
		m_DeltaTimeToWriteLog = m_liFreq.QuadPart * 5; // every 5 seconds
		m_bytesTransmitted = 0;
		m_timeUsed = 0;
		m_timeKernelUsed = 0;
		m_timeUserUsed = 0;
		m_nRequests = 0;
	}

	void AppendHttpRequest(int nBytes,
		LARGE_INTEGER liStart, LARGE_INTEGER liEnd, // m_liFreq ticks/second
		LONG64 deltaKernel, LONG64 deltaUser		// 10000*1000 ticks/second
		)
	{
		SafeCSLock lock(m_protect);
		const LONG64 deltaRequest = liEnd.QuadPart - liStart.QuadPart;
		m_bytesTransmitted += nBytes;
		m_timeUsed += deltaRequest;
		m_timeKernelUsed += deltaKernel;
		m_timeUserUsed += deltaUser;
		m_nRequests++;

		const LONG64 deltaFromPrevSave = liEnd.QuadPart - m_liPrevTime.QuadPart;
		if(deltaFromPrevSave > m_DeltaTimeToWriteLog)
		{
			const SYSTEMTIME st = CurrentTimeAsST();
// 			MYASSERT(0 != m_timeUsed);
// 			MYASSERT(deltaKernel > 0);
// 			MYASSERT(deltaUser > 0);
			m_timeUsed++;
// 			deltaKernel++;
// 			deltaUser++;

			const LONG speedActual = LONG(m_bytesTransmitted * m_liFreq.QuadPart / m_timeUsed);
//			const LONG speedSuperActual = m_bytesTransmitted * (10000*1000) / (deltaKernel + deltaUser);
//			const double fKernel = double(deltaKernel) / double(deltaKernel + deltaUser);
			const LONG speedWhole = LONG(m_bytesTransmitted * m_liFreq.QuadPart / deltaFromPrevSave);
			double fSpeedRequestsActual = double(m_nRequests * m_liFreq.QuadPart) / double(m_timeUsed);
			const double fSpeedRequestsWhole = double(m_nRequests * m_liFreq.QuadPart) / double(deltaFromPrevSave);

// 			const std::string sText = StdString::Format("%d:%02d:%02d\tsuper_actual: %5d bytes/s (%g%% kernel)\tactual: %5d bytes/s\twhole: %5d bytes/s\n",
// 				st.wHour, st.wMinute, st.wSecond, speedSuperActual, fKernel*100.0, speedActual, speedWhole);
			const std::string sText = StdString::Format(
				"%d.%d.%d\t%d:%02d:%02d\tBytes(actual|whole)\t%5d\t%5d\tRequests(actual|whole)\t%.3g\t%.3g\r\n",
				st.wDay, st.wMonth, st.wYear,
				st.wHour, st.wMinute, st.wSecond, speedActual, speedWhole,
				fSpeedRequestsActual, fSpeedRequestsWhole);

#ifdef WRITE_SPEED_LOG
			FILE* f = my_fopen_aplus(m_sFileName, "ab+");
			if(NULL != f)
			{
				fwrite(sText.c_str(), sText.length(), 1, f);
				fclose(f);
			}
#endif

			m_liPrevTime = liEnd;
			m_bytesTransmitted = 0;
			m_timeUsed = 0;
			m_timeKernelUsed = 0;
			m_timeUserUsed = 0;
			m_nRequests = 0;
		}
	}

protected:
	SafeCriticalSection	m_protect;
	// const:
	std::string		m_sFileName;
	LARGE_INTEGER	m_liFreq;
	LONG64			m_DeltaTimeToWriteLog;

	// variables:
	LARGE_INTEGER	m_liPrevTime;
	LONG64			m_bytesTransmitted;
	LONG64			m_timeUsed;
	LONG64			m_timeKernelUsed;
	LONG64			m_timeUserUsed;
	int				m_nRequests;
};

HttpSpeedLogger g_HttpSpeedLogger;

#endif // ifdef WRITE_SPEED_LOG

//------------------------------------------------------

std::wstring REQUEST_RESULT::GetReplyHeader(const std::wstring& sHeaderName, const std::wstring& sDefaultValue) const
{
	std::vector<std::wstring> vectLines;
	BreakStringToLines(vectLines, sReplyHeaders);

	for(size_t i = 0; i < vectLines.size(); i++)
	{
		const std::wstring& sLine = vectLines[i];
		std::wstring sKey;
		std::wstring sValue;
		const bool bFound = BreakStringToKeyValue(sLine, sKey, sValue, L":", L" \t\r\n");
		if(bFound && AreStringsEqualNoCase(sKey, sHeaderName))
		{
			return sValue;
		}
	}

	return sDefaultValue;
}

//------------------------------------------------------

bool WinHttpQueryTextOption(IN const HINTERNET hHandle, IN const DWORD dwOption, OUT std::wstring &sOptionText)
{
	PROFILER;
	sOptionText.clear();

	bool bRes = false;
	DWORD dwTextSize = 0;
	bRes = !!WinHttpQueryOption(hHandle, dwOption, NULL, &dwTextSize);
	if(!bRes && ERROR_INSUFFICIENT_BUFFER != GetLastError())
	{
		MYTRACE("WinHttpQueryOption(%d) failed: %d\n", dwOption, GetLastError());
		return false;
	}

	std::vector<WCHAR> vectText;
	vectText.resize(dwTextSize/sizeof(WCHAR));
	if(vectText.empty())
	{
		return true;
	}

	bRes = !!WinHttpQueryOption(hHandle, dwOption, &vectText[0], &dwTextSize);
	if(!bRes)
	{
		MYTRACE("WinHttpQueryOption(%d) failed: %d\n", dwOption, GetLastError());
		return false;
	}

	if(dwTextSize > 0)
	{
		sOptionText = &vectText[0];
	}
	return true;
}

//------------------------------------------------------

HttpRequest::HttpRequest(HttpConnection* pConnection, HINTERNET hRequest)
{
	m_hRequest = hRequest;
	m_pConnection = pConnection;
	m_ptrParent = pConnection;
}

HttpRequest::~HttpRequest()
{
	WinHttpCloseHandle(m_hRequest);
}

//------------------------------------------------------

bool HttpRequest::AddOrReplaceHttpHeaders(const std::wstring& sHeaders)
{
	PROFILER;
	const DWORD dwModifiers = WINHTTP_ADDREQ_FLAG_ADD | WINHTTP_ADDREQ_FLAG_REPLACE;
	bool bRes = !!WinHttpAddRequestHeaders(m_hRequest, sHeaders.c_str(), -1, dwModifiers);
	if(!bRes)
	{
		return false;
	}
	m_vectRequestHeaders.push_back(sHeaders);
	return true;
}

bool HttpRequest::AddIfNewHttpHeaders(const std::wstring& sHeaders)
{
	PROFILER;
	const DWORD dwModifiers = WINHTTP_ADDREQ_FLAG_ADD_IF_NEW;
	bool bRes = !!WinHttpAddRequestHeaders(m_hRequest, sHeaders.c_str(), -1, dwModifiers);
	if(!bRes)
	{
		return false;
	}
	m_vectRequestHeaders.push_back(sHeaders);
	return true;
}

//------------------------------------------------------

bool HttpRequest::SendRequest(
			IN const std::vector<std::wstring>& vectAdditionalHeaders,
			IN const std::string& sBinaryRequestBody)
{
	PROFILE("SendRequest");

	m_vectRequestHeaders.clear();
	m_sCookies.clear();

	bool bRes = false;

	DWORD dwOptionValue = 0;
	dwOptionValue |= WINHTTP_DISABLE_AUTHENTICATION;
	dwOptionValue |= WINHTTP_DISABLE_COOKIES;
#ifdef MANUAL_REDIRECT
	dwOptionValue |= WINHTTP_DISABLE_REDIRECTS;
#endif
	bRes = !!WinHttpSetOption(m_hRequest, WINHTTP_OPTION_DISABLE_FEATURE, &dwOptionValue, sizeof(dwOptionValue));
	if(!bRes)
	{
		MYTRACE("WinHttpSetOption failed!\n");
		return false;
	}

	std::wstring sRequestWholeUrl;
	bRes = QueryTextOption(sRequestWholeUrl, WINHTTP_OPTION_URL);
	if(!bRes)
	{
		MYTRACE("QueryTextOption failed! (err=%d)\n", GetLastError());
		return false;
	}

	INTERNET_SCHEME nInternetScheme = 0;
	std::wstring sHostName;
	INTERNET_PORT nPort = 0;
	std::wstring sUrlPath;
	bRes = HttpSession::CrackUrl(sRequestWholeUrl, nInternetScheme, sHostName, nPort, sUrlPath);
	if(!bRes)
	{
		MYTRACE("CrackUrl failed!, URL = %ls\n", sRequestWholeUrl.c_str());
		return false;
	}

	const bool bHttps = nInternetScheme == INTERNET_SCHEME_HTTPS;

	dwOptionValue = 0;
	DWORD dwOptionValueSize = sizeof(dwOptionValue);
	bRes = !!WinHttpQueryOption(m_hRequest, WINHTTP_OPTION_SECURITY_FLAGS, &dwOptionValue, &dwOptionValueSize);

	if(bHttps) // (dwOptionValue & SECURITY_FLAG_SECURE) // SECURITY_FLAG_SECURE don't work for me in some cases... ???
	{
		MYTRACE("Secure connection (SSL)!\n"); // HTTPS

		dwOptionValue = 0;
		if(!m_pConnection->m_pSession->GetCheckServerSSLCertificateState())
		{
			// Note! It is better to disable ignoring HTTPS certificate errors
			dwOptionValue |= SECURITY_FLAG_IGNORE_UNKNOWN_CA;
			//dwOptionValue |= SECURITY_FLAG_IGNORE_CERT_WRONG_USAGE;
			dwOptionValue |= SECURITY_FLAG_IGNORE_CERT_CN_INVALID;
			//dwOptionValue |= SECURITY_FLAG_IGNORE_CERT_DATE_INVALID;
		}
		bRes = !!WinHttpSetOption(m_hRequest, WINHTTP_OPTION_SECURITY_FLAGS, &dwOptionValue, sizeof(dwOptionValue));
		if(!bRes)
		{
			MYTRACE("WinHttpSetOption(WINHTTP_OPTION_SECURITY_FLAGS) failed!\n");
			return false;
		}

// 		bRes = !!WinHttpSetOption(m_hRequest, WINHTTP_OPTION_CLIENT_CERT_CONTEXT, WINHTTP_NO_CLIENT_CERT_CONTEXT, 0);
// 		if(!bRes)
// 		{
// 			MYTRACE("WinHttpSetOption(WINHTTP_OPTION_CLIENT_CERT_CONTEXT) failed!\n");
// 			return false;
// 		}
	}

	const std::vector<std::wstring>& vectBrowserHeaders = m_pConnection->m_pSession->GetBrowserHeaders();

	{
		// FORMAT different generated headers for logging:
		std::wstring sHeaderValue;
		{
			// Format first header string for logging: ("GET /path/doc.htm HTTP/1.1")
			HTTP_VERSION_INFO httpVer;
			DWORD dwhttpVerSize = sizeof(httpVer);
			memset(&httpVer, 0, sizeof(httpVer));
			bRes = !!WinHttpQueryOption(m_hRequest, WINHTTP_OPTION_HTTP_VERSION, &httpVer, &dwhttpVerSize);
			if(!bRes)
			{
				MYTRACE("WinHttpQueryOption(WINHTTP_OPTION_HTTP_VERSION) failed: %d\n", GetLastError());
				return false;
			}
			bRes = QueryTextHeaders(sHeaderValue, WINHTTP_QUERY_REQUEST_METHOD);
			if(!bRes)
				MYTRACE("QueryTextHeaders(WINHTTP_QUERY_REQUEST_METHOD) failed!\n");
			else
			{
				const std::wstring& sRequestLine =
					StdString::FormatW(CSW(L"%ls %ls HTTP/%d.%d"), sHeaderValue.c_str(),
					sUrlPath.c_str(), httpVer.dwMajorVersion, httpVer.dwMinorVersion);
				m_vectRequestHeaders.push_back(sRequestLine);
			}
		}

		{
			// Format host (domain) header string for logging:
			std::wstring sHost = sHostName;
			if((nInternetScheme == INTERNET_SCHEME_HTTP && nPort != INTERNET_DEFAULT_HTTP_PORT)
				|| (nInternetScheme == INTERNET_SCHEME_HTTPS && nPort != INTERNET_DEFAULT_HTTPS_PORT) )
			{
				sHost += StdString::FormatW(L":%d", nPort);
			}
			m_vectRequestHeaders.push_back(CSW(L"Host: ") + sHost);
		}

		bool bManualUserAgentHeader = false;

		if(!vectBrowserHeaders.empty())
		{
			for(size_t iHeader = 0; iHeader < vectBrowserHeaders.size(); iHeader++)
			{
				const std::wstring& sHeader = vectBrowserHeaders[iHeader];
				if(begins(sHeader, CSW(L"User-Agent:")))
				{
					bManualUserAgentHeader = true;
					break;	// process manual User-Agent field
				}
			}
		}

		if(!bManualUserAgentHeader)
		{
			// Format user agent string for logging:
			bRes = m_pConnection->m_pSession->QueryTextOption(sHeaderValue, WINHTTP_OPTION_USER_AGENT);
			if(!bRes)
			{
				MYTRACE("QueryTextOption(WINHTTP_OPTION_USER_AGENT) failed! (err=%d)\n", GetLastError());
				return false;
			}
			else
				m_vectRequestHeaders.push_back(CSW(L"User-Agent: ") + sHeaderValue);
		}
	}

	if(vectBrowserHeaders.empty())
	{
		//	bRes = AddOrReplaceHttpHeaders(L"Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"); // Firefox 3.0
		bRes = AddOrReplaceHttpHeaders(CSW(L"Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/x-ms-application, application/x-ms-xbap, application/vnd.ms-xpsdocument, application/xaml+xml, */*")); // MSIE8
		if(!bRes)
		{
			MYTRACE("AddOrReplaceHttpHeaders failed!\n");
			return false;
		}
		bRes = AddOrReplaceHttpHeaders(CSW(L"Accept-Language: en-us,en;q=0.5"));
		if(!bRes)
		{
			MYTRACE("AddOrReplaceHttpHeaders failed!\n");
			return false;
		}
// 		bRes = AddOrReplaceHttpHeaders(L"Accept-Encoding: gzip,deflate");
// 		if(!bRes)
// 		{
// 			MYTRACE("AddOrReplaceHttpHeaders failed!\n");
// 			return false;
// 		}
// 		bRes = AddOrReplaceHttpHeaders(L"Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7");
// 		if(!bRes)
// 		{
// 			MYTRACE("AddOrReplaceHttpHeaders failed!\n");
// 			return false;
// 		}
	}
	else
	{
		for(size_t iHeader = 0; iHeader < vectBrowserHeaders.size(); iHeader++)
		{
			const std::wstring& sHeader = vectBrowserHeaders[iHeader];
			if(begins(sHeader, CSW(L"User-Agent:")))
			{
				//continue;	// process manual User-Agent field: it is not overriden by this way, but works!
			}
			bRes = AddOrReplaceHttpHeaders(sHeader);
			if(!bRes)
			{
				MYTRACE("AddOrReplaceHttpHeaders failed! (%ls)\n", sHeader.c_str());
				return false;
			}
		}
	}

// 	bRes = AddOrReplaceHttpHeaders(L"Keep-Alive: 300");
// 	if(!bRes)
// 	{
// 		return false;
// 	}
// 	bRes = AddOrReplaceHttpHeaders(L"Proxy-Connection: keep-alive"); // during using proxy server!!!
// 	if(!bRes)
// 	{
// 		return false;
// 	}

	{
		// Add cookies:

		const std::wstring sCookieHeader = m_pConnection->m_pSession->GetCookiesHeader(
			bHttps, sHostName, sUrlPath);
		m_sCookies = sCookieHeader;
		if(!sCookieHeader.empty())
		{
			bRes = AddOrReplaceHttpHeaders(sCookieHeader);
			if(!bRes)
			{
				MYTRACE("AddOrReplaceHttpHeaders failed!\n");
				return false;
			}
		}
	}

	for(size_t i = 0; i < vectAdditionalHeaders.size(); i++)
	{
		const std::wstring& sHeader = vectAdditionalHeaders[i];
		bRes = AddOrReplaceHttpHeaders(sHeader);
		if(!bRes)
		{
			MYTRACE("AddOrReplaceHttpHeaders failed! (%s)\n", sHeader.c_str());
			return false;
		}
	}

	if(!sBinaryRequestBody.empty())
	{
		const std::wstring sHeader = StdString::FormatW(L"%u", sBinaryRequestBody.size());
		m_vectRequestHeaders.push_back(CSW(L"Content-Length: ") + sHeader);
	}

	LPCVOID lpHttpBody = sBinaryRequestBody.empty() ? NULL : sBinaryRequestBody.data();

	bRes = !!WinHttpSendRequest(m_hRequest,
		WINHTTP_NO_ADDITIONAL_HEADERS, 0,
		(LPVOID)lpHttpBody, sBinaryRequestBody.size(), sBinaryRequestBody.size(), 0);
	if(!bRes)
	{
		const LONG le = GetLastError();
		MYTRACE("WinHttpSendRequest failed: %d (%ls)!\n", le, StdString::FormatApiErrorW(le).c_str());
		return false;
	}

	return bRes;
}

//------------------------------------------------------

bool HttpRequest::ProcessCookiesHeaders()
{
	PROFILER;
	bool bRes = false;
	std::wstring sRequestWholeUrl;
	bRes = QueryTextOption(sRequestWholeUrl, WINHTTP_OPTION_URL);
	if(!bRes)
	{
		MYTRACE("QueryTextOption failed! (err=%d)\n", GetLastError());
		return false;
	}

	INTERNET_SCHEME nInternetScheme = 0;
	std::wstring sHostName;
	INTERNET_PORT nPort = 0;
	std::wstring sUrlPath;
	bRes = HttpSession::CrackUrl(sRequestWholeUrl, nInternetScheme, sHostName, nPort, sUrlPath);
	if(!bRes)
	{
		MYTRACE("CrackUrl failed!, URL = %ls\n", sRequestWholeUrl.c_str());
		return false;
	}

	// Enumerate all "Set-cookie" headers:
	DWORD dwCookieIndex = 0; // it is changed inside of QueryTextHeaders()
	DWORD nSetCookiesCount = 0;
	while(true)
	{
		std::wstring sSetCookie;
		bRes = QueryTextHeaders(sSetCookie, WINHTTP_QUERY_SET_COOKIE, &dwCookieIndex);
		const LONG error = GetLastError();
		if(!bRes)
		{
			if(ERROR_WINHTTP_HEADER_NOT_FOUND != error)
			{
				MYTRACE("QueryTextHeaders failed: %d\n", GetLastError());
				return false;
			}
			break;
		}

		nSetCookiesCount++;

		{
// Save Set-Cookie string to log file:
// 			const std::string sLog =
// 				StdString::Format("%ls\n%ls\n\n", sRequestWholeUrl.c_str(), sSetCookie.c_str());
// 
// 			CMyFile log;
// 			log.AppendTextWithOpeningFile(GetExeRelativePath("cookies.log"), sLog.c_str());
		}

		HttpSetCookieInfo info;
		bRes = CrackSetCookieString(sSetCookie.c_str(), info);
		if(!bRes)
		{
			MYTRACE("CrackSetCookieString failed! (err=%d)\n", GetLastError());
			return false;
		}

		if(info.m_sDomain.empty())
		{
			info.m_sDomain = sHostName;
		}
		if(info.m_sPath.empty())
		{
			info.m_sPath = sUrlPath;
		}

		const bool bValidCookieSet =
			WinHttpCookies::CheckSetCookiePolicies(info.m_sDomain, info.m_sPath, sHostName, sUrlPath);

		// TODO: COOK
		// TODO: check server time,
		// TODO: check path = '/', domain begins from '.'
		if(!bValidCookieSet)
		{
			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			MYTRACE("WARNING! Cookie! Server cookie violation!\n");
			MYTRACE("Tried to set invalid cookie for '%ls', '%ls' from '%ls', '%ls'\n",
				info.m_sDomain.c_str(), info.m_sPath.c_str(), sHostName.c_str(), sUrlPath.c_str());
			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
//			MYASSERT(FALSE && "Cookie violation!");
		}

		if(!begins(info.m_sPath, L"/"))
		{
			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
			MYTRACE("WARNING! Invalid cookie path! (%ls)\n", info.m_sPath.c_str());
			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
		}
// 		if(!begins(info.m_sDomain, L"."))
// 		{
// 			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
// 			MYTRACE("WARNING! Invalid cookie domain! (%ls)\n", info.m_sDomain.c_str());
// 			MYTRACE("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");
// 		}

//		if(bValidCookieSet)
		{
			const SYSTEMTIME stNow = CurrentTimeAsST();
			HttpCookieVector vect;
			PreProcessCookies(info, stNow, vect);

			for(size_t i = 0; i < vect.size(); i++)
			{
				const HttpCookie& cookie = vect[i];
				MYASSERT(!cookie.m_sName.empty());

				m_pConnection->m_pSession->SetCookie(cookie);
			}
		}
	}
	return true;
}

//------------------------------------------------------

bool HttpRequest::GetResponse(std::string& sBinaryResponce)
{
	PROFILE("GetResponse");
	bool bRes = false;

	sBinaryResponce.clear();

	bRes = !!WinHttpReceiveResponse(m_hRequest, NULL);
	if(!bRes)
	{
		const LONG le = GetLastError();
		MYTRACE("WinHttpReceiveResponse failed: %d (%ls)!\n", le, StdString::FormatApiErrorW(le).c_str());
		return false;
	}

	bRes = ProcessCookiesHeaders();
	if(!bRes)
	{
		MYTRACE("ProcessCookiesHeaders failed! (err=%d)\n", GetLastError());
		return false;
	}

	DWORD dwSize = 0;
	DWORD dwActualData = 0;
	DWORD dwDownloaded = 0;
	std::vector<char>	vectResponce;
	do
	{
		dwSize = 0;
		bRes = !!WinHttpQueryDataAvailable(m_hRequest, &dwSize);
		if(!bRes)
		{
			const LONG le = GetLastError();
			MYTRACE("WinHttpQueryDataAvailable failed: %d (%ls)!\n", le, StdString::FormatApiErrorW(le).c_str());
			return false;
		}

		dwSize = max(dwSize, 8192);
		//dwSize = 8192;

		MYASSERT(dwActualData + dwSize < 10*1024*1024); // just in case

		vectResponce.resize(dwActualData + dwSize);

		dwDownloaded = 0;
		bRes = !!WinHttpReadData(m_hRequest, &vectResponce[dwActualData], dwSize, &dwDownloaded);
		if(!bRes)
		{
			const LONG le = GetLastError();
			MYTRACE("WinHttpReadData failed: %d (%ls)!\n", le, StdString::FormatApiErrorW(le).c_str());
			return false;
		}
		dwActualData += dwDownloaded;
		if(0 == dwDownloaded)
		{
			// downloading complete!
		}
	} while (dwDownloaded > 0);

	//vectResponce.resize(dwActualData); // not necessary here
	if(dwActualData > 0)
	{
		std::wstring sContentEncoding;
		bRes = QueryTextHeaders(sContentEncoding, WINHTTP_QUERY_CONTENT_ENCODING);
		if(!bRes)
		{
			const LONG le = GetLastError();
			if(ERROR_WINHTTP_HEADER_NOT_FOUND != le)
			{
				MYTRACE("WARNING! GetRequestContentEncoding failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
			}
		}

		sBinaryResponce = std::string(&vectResponce[0], dwActualData);
	}

	return true;
}

//------------------------------------------------------

bool HttpRequest::MakeRequest(
						OUT std::string& sBinaryResponce,
						IN const std::vector<std::wstring>& vectAdditionalHeaders,
						IN const std::string& sBinaryRequestBody)
{
	PROFILE("MakeRequest");

	bool bRes = true;
	sBinaryResponce.clear();

#ifdef WRITE_SPEED_LOG
	LONG le = GetLastError();

	LARGE_INTEGER liStart, liEnd;
	const HANDLE hCurrentThread = GetCurrentThread();
	FILETIME ftKernelStart, ftUserStart;
	FILETIME ftKernelEnd, ftUserEnd;
	FILETIME ftDummy;

	QueryPerformanceCounter(&liStart);
	GetThreadTimes(hCurrentThread, &ftDummy, &ftDummy, &ftKernelStart, &ftUserStart);

	SetLastError(le);
#endif

	bRes = SendRequest(vectAdditionalHeaders, sBinaryRequestBody);
	if(bRes)
	{
		bRes = GetResponse(sBinaryResponce);
	}

#ifdef WRITE_SPEED_LOG
	le = GetLastError();

	GetThreadTimes(hCurrentThread, &ftDummy, &ftDummy, &ftKernelEnd, &ftUserEnd);
	QueryPerformanceCounter(&liEnd);

	const LONG64 deltaKernel = FT2LI(ftKernelEnd).QuadPart - FT2LI(ftKernelStart).QuadPart;
	const LONG64 deltaUser = FT2LI(ftUserEnd).QuadPart - FT2LI(ftUserStart).QuadPart;

	g_HttpSpeedLogger.AppendHttpRequest(sBinaryResponce.size(), liStart, liEnd, deltaKernel, deltaUser);

	SetLastError(le);
#endif

	return bRes;
}

//------------------------------------------------------

DWORD HttpRequest::GetRequestHttpStatus()
{
	PROFILER;
	DWORD dwStatusCode = 0;
	DWORD dwSize = sizeof(DWORD);
	const bool bRes = !!WinHttpQueryHeaders( m_hRequest,
		WINHTTP_QUERY_STATUS_CODE | WINHTTP_QUERY_FLAG_NUMBER,
		NULL,
		&dwStatusCode,
		&dwSize,
		WINHTTP_NO_HEADER_INDEX );

	return dwStatusCode;
}

//------------------------------------------------------

bool HttpRequest::IsRedirectStatus(const DWORD dwHttpStatus)
{
	switch(dwHttpStatus)
	{
	case 301:
	case 302:
	case 303:
	case 307:
		return true;
	}
	return false;
}

//------------------------------------------------------

void HttpRequest::GetSavedRequestTextHeaders(OUT std::wstring& sOutputString) const
{
	PROFILER;
	sOutputString = implode(m_vectRequestHeaders, L"\r\n");
}

//------------------------------------------------------

bool HttpRequest::QueryTextHeaders(OUT std::wstring& sHeaders, IN const DWORD dwInfoLevel,
									IN OUT LPDWORD lpdwHeaderIndex)
{
	PROFILER;
	sHeaders.clear();

	bool bRes = false;
	DWORD dwSize = 0;
	// First, use HttpQueryInfo to obtain the size of the buffer.
	bRes = !!WinHttpQueryHeaders(m_hRequest, dwInfoLevel,
		WINHTTP_HEADER_NAME_BY_INDEX, NULL, &dwSize,
		lpdwHeaderIndex);
	const LONG error = GetLastError();

	if(!bRes && error == ERROR_INSUFFICIENT_BUFFER)
	{
		std::array_ptr<WCHAR> ptrBuffer(new WCHAR[dwSize/sizeof(WCHAR)]);

		// Now, use HttpQueryInfo to retrieve the header.
		bRes = !!WinHttpQueryHeaders(m_hRequest, dwInfoLevel,
			WINHTTP_HEADER_NAME_BY_INDEX, ptrBuffer.get(), &dwSize,
			lpdwHeaderIndex);

		if(!bRes)
		{
			return false;
		}

		sHeaders = ptrBuffer.get();
		return true;
	}
	return false;
}

//------------------------------------------------------

bool HttpRequest::QueryTextOption(OUT std::wstring &sOptionText, IN const DWORD dwOption)
{
	return WinHttpQueryTextOption(m_hRequest, dwOption, sOptionText);
}

//------------------------------------------------------

HttpConnection::HttpConnection(HttpSession* pSession, HINTERNET hConnection)
{
	m_hConnection = hConnection;
	m_pSession = pSession;
	m_ptrParent = pSession;
}

//------------------------------------------------------

HttpConnection::~HttpConnection()
{
	WinHttpCloseHandle(m_hConnection);
}

//------------------------------------------------------

HttpRequestPtr HttpConnection::OpenRequest(
		IN const std::wstring&	sVerb,	// VERB_GET, VERB_POST, etc.
		IN const std::wstring&	sObjectName,
		IN const DWORD dwFlags
		)
{
	PROFILER;
	const HINTERNET hRequest = WinHttpOpenRequest(m_hConnection,
		sVerb.c_str(),
		sObjectName.c_str(),
		NULL,				// If this parameter is NULL, the function uses HTTP/1.1.
		WINHTTP_NO_REFERER,	// If this parameter is set to WINHTTP_NO_REFERER, no referring document is specified.
							// Note! Referrer will be sent as an additional header to HttpRequestPtr->MakeRequest().
		WINHTTP_DEFAULT_ACCEPT_TYPES,	// If this parameter is set to WINHTTP_DEFAULT_ACCEPT_TYPES,
							// no types are accepted by the client.
		dwFlags);
	if(NULL == hRequest)
	{
		MYTRACE("WinHttpOpenRequest failed, err = %d!\n", GetLastError());
		return NULL;
	}

	HttpRequest* pRequest = new HttpRequest(this, hRequest);
	if(NULL == pRequest)
	{
		MYTRACE("'new HttpRequest' failed!\n");
		WinHttpCloseHandle(hRequest);
		return NULL;
	}

	return pRequest;
}

//------------------------------------------------------

void CALLBACK HttpSession::StatusCallback(
					HINTERNET hInternet,
					DWORD_PTR dwContext,
					DWORD dwInternetStatus,
					LPVOID lpvStatusInformation,
					DWORD dwStatusInformationLength)
{
	PROFILER;
	// ===============================================================================================
	// NOTE! To handle other internet statuses you should change flags for WinHttpSetStatusCallback()!
	// ===============================================================================================
	switch(dwInternetStatus)
	{
	case WINHTTP_CALLBACK_STATUS_SECURE_FAILURE:
		{
			MYASSERT(dwStatusInformationLength == sizeof(DWORD));
			const DWORD dwSecurityFailureFlags = *(LPDWORD)lpvStatusInformation;
			MYTRACE("WinHttp security error: 0x%08X\n", dwSecurityFailureFlags);
#define PROCESS_SECURITY_FLAG(flag) \
			if(0 != (flag & dwSecurityFailureFlags)) \
			{ \
				MYTRACE("WinHttp security error flag: (0x%08X) %s\n", flag, STR_CRYPT2_str(#flag)); \
			}
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_CERT_REV_FAILED);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_INVALID_CERT);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_CERT_REVOKED);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_INVALID_CA);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_CERT_CN_INVALID);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_CERT_DATE_INVALID);
			PROCESS_SECURITY_FLAG(WINHTTP_CALLBACK_STATUS_FLAG_SECURITY_CHANNEL_ERROR);
		}
		break;
	}
}

//------------------------------------------------------

const REQUEST_RESULT g_DummyRequestResult;

const REQUEST_RESULT& REQUEST_RESULTS::GetFirstResult() const
{
	MYASSERT(!vectRedirects.empty());
//	CHECK_EQUAL_WARN(false, vectRedirects.empty());
	if(vectRedirects.empty())
		return g_DummyRequestResult;
	return vectRedirects[0];
}

//------------------------------------------------------

const REQUEST_RESULT& REQUEST_RESULTS::GetLastResult() const
{
	MYASSERT(!vectRedirects.empty());
//	CHECK_EQUAL_WARN(false, vectRedirects.empty());
	if(vectRedirects.empty())
		return g_DummyRequestResult;
	return vectRedirects[vectRedirects.size()-1];
}

//------------------------------------------------------

HttpSession::HttpSession(HINTERNET hSession)
{
	m_bCheckServerSSLCertificate = false;
	m_hSession = hSession;
}

//------------------------------------------------------

HttpSession::~HttpSession()
{
	WinHttpCloseHandle(m_hSession);
}

//------------------------------------------------------

CComPtr<HttpSession> HttpSession::Create(
		IN DWORD				dwAccessType,
		IN const std::wstring&	sProxyName,
		IN const std::wstring&	sProxyBypass,
		IN const std::wstring&	sUserAgent
		)
{
	PROFILER;
	bool bRes = false;
	const bool bWinHttpSupported = !!WinHttpCheckPlatform();
	if(!bWinHttpSupported)
	{
		SetLastError(ERROR_NOT_SUPPORTED);
		return NULL;
	}

	LPCWSTR lpszProxy = sProxyName.empty() ? WINHTTP_NO_PROXY_NAME : sProxyName.c_str();
	LPCWSTR lpszProxyBypass = sProxyBypass.empty() ? WINHTTP_NO_PROXY_BYPASS : sProxyBypass.c_str();

	const HINTERNET hSession = WinHttpOpen(sUserAgent.c_str(),
		dwAccessType, lpszProxy, lpszProxyBypass, 0);
	if(NULL == hSession)
		return NULL;

	const WINHTTP_STATUS_CALLBACK isCallback = 
		WinHttpSetStatusCallback(hSession, HttpSession::StatusCallback,
		WINHTTP_CALLBACK_FLAG_SECURE_FAILURE, //WINHTTP_CALLBACK_FLAG_ALL_NOTIFICATIONS,
		NULL);
	MYASSERT(isCallback != WINHTTP_INVALID_STATUS_CALLBACK);

	{
		const DWORD dwResolveTimeout	= 0;		// default: 0, infinite
		const DWORD dwConnectTimeout	= 60*1000;	// default: 60000 (60 sec)
		const DWORD dwSendTimeout		= 60*1000;	// default: 30000 (30 sec)
		const DWORD dwReceiveTimeout	= 70*1000;	// default: 30000 (30 sec)
		// Use WinHttpSetTimeouts to set a new time-out values.
		//MYTRACE("WinHttpSetTimeouts(%d, %d, %d, %d)...\n",
		//	dwResolveTimeout, dwConnectTimeout, dwSendTimeout, dwReceiveTimeout);
		bRes = !!WinHttpSetTimeouts(hSession,
			dwResolveTimeout, dwConnectTimeout, dwSendTimeout, dwReceiveTimeout);
		if (!bRes)
		{
			const LONG le = GetLastError();
			MYTRACE("WinHttpSetTimeouts failed: %d\n", GetLastError());
			WinHttpCloseHandle(hSession);
			SetLastError(le);
			return NULL;
		}
	}

	DWORD dwValue = 0;

	dwValue = 5;
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_CONNECT_RETRIES, &dwValue, sizeof(dwValue));
	MYASSERT(bRes && "WinHttpSetOption failed!");

/*
	DWORD dwTimeout = 0;
// #define WINHTTP_OPTION_RESOLVE_TIMEOUT                2	// INFINITE
// #define WINHTTP_OPTION_CONNECT_TIMEOUT                3	// 60 secs
// #define WINHTTP_OPTION_CONNECT_RETRIES                4	// 5
// #define WINHTTP_OPTION_SEND_TIMEOUT                   5	// 30 secs
// #define WINHTTP_OPTION_RECEIVE_TIMEOUT                6	// 30 secs
// #define WINHTTP_OPTION_RECEIVE_RESPONSE_TIMEOUT       7	// 90 secs

	dwTimeout = 15*1000;
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_CONNECT_TIMEOUT, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_SEND_TIMEOUT, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_RECEIVE_TIMEOUT, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
	dwTimeout = 45*1000;
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_RECEIVE_RESPONSE_TIMEOUT, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
	dwTimeout = 15*1000;
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_RESOLVE_TIMEOUT, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
	dwTimeout = 3;
	bRes = !!WinHttpSetOption(hSession, WINHTTP_OPTION_CONNECT_RETRIES, &dwTimeout, sizeof(dwTimeout));
	MYASSERT(bRes && "WinHttpSetOption failed!");
*/

	HttpSession* pSession = new HttpSession(hSession);
	if(NULL == pSession)
	{
		WinHttpCloseHandle(hSession);
		SetLastError(ERROR_NOT_ENOUGH_MEMORY);
		return NULL;
	}

	return pSession;
}

//------------------------------------------------------

bool HttpSession::SetProxyInformation(
			IN const std::wstring&	sProxyName,
			IN const std::wstring&	sProxyBypass
			)
{
	PROFILER;
	bool bRes = false;

	WINHTTP_PROXY_INFO info;
	memset(&info, 0, sizeof(info));
	info.dwAccessType = sProxyName.empty() ? WINHTTP_ACCESS_TYPE_DEFAULT_PROXY : WINHTTP_ACCESS_TYPE_NAMED_PROXY;
	info.lpszProxy = sProxyName.empty() ? WINHTTP_NO_PROXY_NAME : (LPWSTR)sProxyName.c_str();
	info.lpszProxyBypass = sProxyBypass.empty() ? WINHTTP_NO_PROXY_BYPASS : (LPWSTR)sProxyBypass.c_str();

	SetLastError(0);
	bRes = !!WinHttpSetOption(m_hSession, WINHTTP_OPTION_PROXY, &info, sizeof(info));
	if(!bRes)
	{
		const LONG le = GetLastError();
		MYTRACE("WinHttpSetOption(WINHTTP_OPTION_PROXY) failed: %d (%ls)\n",
			le, StdString::FormatApiErrorW(le).c_str());
		return false;
	}

	return true;
}

//------------------------------------------------------

bool HttpSession::GetProxyInformation(
						 OUT std::wstring&	sProxyName,
						 OUT std::wstring&	sProxyBypass
						 )
{
	PROFILER;
	bool bRes = false;
	WINHTTP_PROXY_INFO info;
	memset(&info, 0, sizeof(info));
	DWORD dwSize = sizeof(info);

	sProxyName.clear();
	sProxyBypass.clear();

	bRes = !!WinHttpQueryOption(m_hSession, WINHTTP_OPTION_PROXY, &info, &dwSize);
	if(!bRes)
	{
		return false;
	}

	MYASSERT(dwSize == sizeof(info));

	if(NULL != info.lpszProxy)
	{
		sProxyName = info.lpszProxy;
		GlobalFree(info.lpszProxy);
	}

	if(NULL != info.lpszProxyBypass)
	{
		sProxyBypass = info.lpszProxyBypass;
		GlobalFree(info.lpszProxyBypass);
	}

	return true;
}

//------------------------------------------------------

bool HttpSession::SetUserAgent(
		IN const std::wstring&	sUserAgent
		)
{
	bool bRes = false;
	SetLastError(0);
	bRes = !!WinHttpSetOption(m_hSession, WINHTTP_OPTION_USER_AGENT, (LPVOID)sUserAgent.c_str(), sUserAgent.length()+1);
	if(!bRes)
	{
		const LONG le = GetLastError();
		MYTRACE("WinHttpSetOption(WINHTTP_OPTION_USER_AGENT) failed: %d (%ls)\n",
			le, StdString::FormatApiErrorW(le).c_str());
		return false;
	}

	return true;
}

//------------------------------------------------------

HttpConnectionPtr HttpSession::Connect(
		IN const std::wstring&	sServerName,
		IN const INTERNET_PORT	nServerPort
								  )
{
	PROFILER;
	const HINTERNET hConnection = WinHttpConnect(m_hSession, sServerName.c_str(), nServerPort, 0);
	if(NULL == hConnection)
	{
		MYTRACE("WinHttpConnect failed, err = %d!\n", GetLastError());
		return NULL;
	}

	HttpConnection* pConnection = new HttpConnection(this, hConnection);
	if(NULL == pConnection)
	{
		MYTRACE("'new HttpConnection' failed!\n");
		WinHttpCloseHandle(hConnection);
		return NULL;
	}
	return pConnection;
}

//------------------------------------------------------

bool HttpSession::CrackUrl(
						   IN const std::wstring& sWholeUrl,
						   OUT INTERNET_SCHEME& nInternetScheme,
						   OUT std::wstring& sHostName,
						   OUT INTERNET_PORT& nPort,
						   OUT std::wstring& sUrlPath)
{
	PROFILER;
	nInternetScheme = 0;
	sHostName.clear();
	nPort = 0;
	sUrlPath.clear();

	URL_COMPONENTS urlComp;

	// Initialize the URL_COMPONENTS structure.
	ZeroMemory(&urlComp, sizeof(urlComp));
	urlComp.dwStructSize = sizeof(urlComp);

	// Set required component lengths to non-zero so that they are cracked.
	urlComp.dwHostNameLength  = -1;
	urlComp.dwUrlPathLength   = -1;

	// Crack the URL.
	bool bRes = !!WinHttpCrackUrl(sWholeUrl.c_str(), sWholeUrl.length(), 0, &urlComp);
	if(!bRes)
	{
		return false;
	}

	nInternetScheme = urlComp.nScheme;
	sUrlPath = std::wstring(urlComp.lpszUrlPath, urlComp.dwUrlPathLength);
	sHostName = std::wstring(urlComp.lpszHostName, urlComp.dwHostNameLength);
	nPort = urlComp.nPort;
	return true;
}

//------------------------------------------------------

bool HttpSession::Connect(
		IN const std::wstring&	sVerb,	// VERB_GET, VERB_POST, etc.
		IN const std::wstring&	sWholeUrl,
		OUT HttpConnectionPtr&	ptrConnection,
		OUT HttpRequestPtr&		ptrRequest
		)
{
	PROFILER;
	INTERNET_SCHEME nInternetScheme = 0;
	std::wstring sHostName;
	INTERNET_PORT nPort = 0;
	std::wstring sUrlPath;
	bool bRes = HttpSession::CrackUrl(sWholeUrl, nInternetScheme, sHostName, nPort, sUrlPath);
	if(!bRes)
	{
		MYTRACE("CrackUrl failed!, URL = %ls\n", sWholeUrl.c_str());
		return false;
	}

	HttpConnectionPtr ptrConn = Connect(sHostName, nPort);
	if(!ptrConn)
	{
		MYTRACE("Connect() failed!, host = %ls, port = %d\n",
			sHostName.c_str(), nPort);
		return false;
	}

	const DWORD dwOpenRequestFlags =
		nInternetScheme == INTERNET_SCHEME_HTTPS ? WINHTTP_FLAG_SECURE : 0; // HTTPS (SSL) support

	HttpRequestPtr ptrReq = ptrConn->OpenRequest(sVerb, sUrlPath, dwOpenRequestFlags);
	if(!ptrReq)
	{
		MYTRACE("OpenRequest() failed, verb = %ls, URL_path = %ls, flags = 0x%X, scheme = %d!\n",
			sVerb.c_str(), sUrlPath.c_str(), dwOpenRequestFlags, nInternetScheme);
		return false;
	}

	ptrConnection = ptrConn;
	ptrRequest = ptrReq;
	return true;
}

//------------------------------------------------------

std::wstring ConstructUrl(
	const INTERNET_SCHEME nInternetScheme,
	const std::wstring& sHostName,	// i.e. "subd.domain.com"
	const INTERNET_PORT nPort,
	const std::wstring& sUrlPath	// i.e "/dir/script.ext?arg1=val1"
	)
{
	LPCWSTR pszProtocol = nInternetScheme == INTERNET_SCHEME_HTTPS ? L"https" : L"http";
	const bool bIgnorePort =
		(nInternetScheme == INTERNET_SCHEME_HTTP && nPort == INTERNET_DEFAULT_HTTP_PORT) ||
		(nInternetScheme == INTERNET_SCHEME_HTTPS && nPort == INTERNET_DEFAULT_HTTPS_PORT);

	if(bIgnorePort)
		return StdString::FormatW(L"%s://%s%s", pszProtocol, sHostName.c_str(), sUrlPath.c_str());
	else
		return StdString::FormatW(L"%s://%s:%d%s", pszProtocol, sHostName.c_str(), nPort, sUrlPath.c_str());
}

//------------------------------------------------------

bool HttpSession::DownloadFromUrl(
					IN const REQUEST_INFO&	globalRequest,
					OUT REQUEST_RESULTS&	globalResults,
					IN bool					bGenerateOutput,
					IN const int			nMaxRedirects
					)
{
	PROFILE("DownloadFromUrl");
	SetLastError(0);

	globalResults = REQUEST_RESULTS();

	bool bRes = false;

	//------------------------------------------------------

	REQUEST_INFO redirInfo = globalRequest;

	for(int nRedirectIndex = 0; ; nRedirectIndex++)
	{
		globalResults.vectRedirects.resize(globalResults.vectRedirects.size()+1);
		REQUEST_RESULT& redirResults = globalResults.vectRedirects[globalResults.vectRedirects.size()-1];

		//------------------------------------------------------
		// Fill and save pre-request data:

		std::vector<std::wstring> vectAdditionalHeaders;
		if(!IsHttpNullString(redirInfo.sReferer))
		{
			MYASSERT(!redirInfo.sReferer.empty());
			vectAdditionalHeaders.push_back(CSW(L"Referer: ") + redirInfo.sReferer);
		}
		if(!IsHttpNullString(redirInfo.sContentType))
		{
			MYASSERT(!redirInfo.sContentType.empty());
			vectAdditionalHeaders.push_back(CSW(L"Content-Type: ") + redirInfo.sContentType);
		}

		redirResults.request = redirInfo;
		bRes = GetProxyInformation(redirResults.sProxy, redirResults.sProxyBypass);
		MYASSERT(bRes && "GetProxyInformation() failed!");

		//------------------------------------------------------
		// Connect server (possibly through the proxy):

		HttpConnectionPtr ptrConnection;
		HttpRequestPtr ptrRequest;
		bRes = Connect(redirInfo.sVerb, redirInfo.sWholeUrl, ptrConnection, ptrRequest);
		if(!bRes)
		{
			MYTRACE("Connect failed! (redir #%d), verb = %ls, URL = %ls\n",
				nRedirectIndex, redirInfo.sVerb.c_str(), redirInfo.sWholeUrl.c_str());
			return false;
		}

		//------------------------------------------------------
		// MAKE REQUEST:

		const bool bMakeRequestOK = ptrRequest->MakeRequest(redirResults.sBinaryReplyBody,
			vectAdditionalHeaders, redirInfo.sBinaryRequestBody);

		redirResults.dwOriginalReplyBodySize = redirResults.sBinaryReplyBody.size();

		//------------------------------------------------------
		// Get and save post-request data:

		redirResults.sCookies = ptrRequest->GetSavedCookies();

		ptrRequest->GetSavedRequestTextHeaders(redirResults.sRequestHeaders);
		if(bGenerateOutput)
		{
//			MYTRACE("REQUEST HEADERS: ---\n%ls\n---\n", redirResults.sRequestHeaders.c_str());
		}

		//------------------------------------------------------
		// Analyze request results:

		if(!bMakeRequestOK)
		{
			MYTRACE("MakeRequest failed! (redir #%d)\n", nRedirectIndex);
			return false;
		}

		bRes = ptrRequest->QueryTextHeaders(redirResults.sReplyHeaders, WINHTTP_QUERY_RAW_HEADERS_CRLF);
		if(!bRes)
		{
			const LONG le = GetLastError();
			MYTRACE("WARNING! GetRawResponceTextHeaders failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
		}

		std::wstring sContentLength;
		bRes = ptrRequest->QueryTextHeaders(sContentLength, WINHTTP_QUERY_CONTENT_LENGTH);

		bRes = ptrRequest->QueryTextHeaders(redirResults.sContentType, WINHTTP_QUERY_CONTENT_TYPE);
		if(!bRes && sContentLength != L"0")
		{
			const LONG le = GetLastError();
			MYTRACE("WARNING! GetRequestContentType failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
		}

		bRes = ptrRequest->QueryTextHeaders(redirResults.sContentEncoding, WINHTTP_QUERY_CONTENT_ENCODING);
		if(!bRes)
		{
			const LONG le = GetLastError();
			if(ERROR_WINHTTP_HEADER_NOT_FOUND != le)
			{
				MYTRACE("WARNING! GetRequestContentEncoding failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
			}
		}

		bRes = ptrRequest->QueryTextHeaders(redirResults.sTransferEncoding, WINHTTP_QUERY_TRANSFER_ENCODING);
		if(!bRes)
		{
			const LONG le = GetLastError();
			if(ERROR_WINHTTP_HEADER_NOT_FOUND != le)
			{
				MYTRACE("WARNING! GetRequestTransferEncoding failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
			}
		}

		bRes = ptrRequest->QueryTextHeaders(redirResults.sServer, WINHTTP_QUERY_SERVER);
		if(!bRes)
		{
			const LONG le = GetLastError();
			MYTRACE("WARNING! GetRequestServer failed: %d (%ls)\n", le, StdString::FormatApiErrorW(le).c_str());
		}

		redirResults.dwHttpStatusCode = ptrRequest->GetRequestHttpStatus();
		const bool bFoundLocation = ptrRequest->QueryTextHeaders(redirResults.sLocation, WINHTTP_QUERY_LOCATION);

		if(redirResults.sTransferEncoding != L"")
		{
			// RFC2616 3.6 Transfer Codings
			// The Internet Assigned Numbers Authority (IANA) acts as a registry for transfer-coding
			// value tokens. Initially, the registry contains the following tokens:
			// "chunked" (section 3.6.1),
			// "identity" (section 3.6.2),
			// "gzip" (section 3.5),
			// "compress" (section 3.5), and
			// "deflate" (section 3.5).

			if(AreStringsEqualNoCase(redirResults.sTransferEncoding, L"chunked"))
			{
				// it seems like WinHTTP supports automatic chunk decoding
			}
			else
			{
				MYTRACE("WARNING! Unsupported transfer encoding: \"%ls\"\n",
					redirResults.sTransferEncoding.c_str());
			}
		}

		if(redirResults.sContentEncoding != L"")
		{
			MYTRACE("NOTE! Content-Encoding: %ls\n", redirResults.sContentEncoding.c_str());

			// RFC2616 3.5 Content Codings:
			// The Internet Assigned Numbers Authority (IANA) acts as a registry for content-coding value tokens.
			// Initially, the registry contains the following tokens:
			//   gzip An encoding format produced by the file compression program "gzip" (GNU zip)
			//		as described in RFC 1952 [25]. This format is a Lempel-Ziv coding (LZ77) with a 32 bit CRC.
			//   compress The encoding format produced by the common UNIX file compression program "compress".
			//		This format is an adaptive Lempel-Ziv-Welch coding (LZW).
			//   deflate The "zlib" format defined in RFC 1950 [31] in combination with the "deflate"
			//		compression mechanism described in RFC 1951 [29].
			//   identity The default (identity) encoding; the use of no transformation whatsoever.
			//		This content-coding is used only in the Accept- Encoding header, and SHOULD NOT be used
			//		in the Content-Encoding header.

			if(
				AreStringsEqualNoCase(redirResults.sContentEncoding, L"gzip")
				|| AreStringsEqualNoCase(redirResults.sContentEncoding, L"deflate")
				|| AreStringsEqualNoCase(redirResults.sContentEncoding, L"bzip2")
				)
			{
#ifndef WINHTTP_WRAPPER_NO_COMPRESSION_SUPPORT
				const bool bRawInflateData = AreStringsEqualNoCase(redirResults.sContentEncoding, L"deflate");
				std::string sDecompressed;
				const bool bDecompressedOK = Decompress(redirResults.sBinaryReplyBody, sDecompressed,
					bRawInflateData); // support: deflate, gzip, bzip2
				if(bDecompressedOK)
				{
					redirResults.sBinaryReplyBody = sDecompressed;
				}
				else
#endif
				{
					MYTRACE("ERROR! Can't decompress %d bytes with content-encoding: %ls\n",
						redirResults.sBinaryReplyBody.size(), redirResults.sContentEncoding.c_str());
				}
			}
			else
			{
				MYTRACE("WARNING! Unsupported content encoding: \"%ls\"\n",
					redirResults.sTransferEncoding.c_str());
			}
		}

		if(bGenerateOutput)
		{
//			MYTRACE("REPLY HEADERS: ---\n%ls\n---\n", redirResults.sHeaders.c_str());
		}

#ifdef MANUAL_REDIRECT
		if(HttpRequest::IsRedirectStatus(redirResults.dwHttpStatusCode))
		{
			if(!bFoundLocation)
			{
				MYTRACE("Can't get Location header during redirect! Reply headers:\n=====================\n%ls=====================\n",
					redirResults.sReplyHeaders.c_str());
				// MProxy-Connection: Close
				// ContenProxy-Connection: Close
				if(redirResults.sReplyHeaders.find(CSW(L"Proxy-Connection: Close")) == std::wstring::npos)
				{
					MYASSERT(FALSE && "Can't get Location header during redirect!");
				}
				SetLastError(0);
				return false;
			}

			if(nRedirectIndex < nMaxRedirects)
			{
				const std::wstring& sOldLocation = redirInfo.sWholeUrl;
				std::wstring sNewLocation = redirResults.sLocation;

				if(bGenerateOutput)
				{
					if(0 == nRedirectIndex)
					{
						MYTRACE("HTTP status %d: redirect FROM %ls\n", redirResults.dwHttpStatusCode, sOldLocation.c_str());
					}
					MYTRACE("HTTP status %d: redirect to %ls\n", redirResults.dwHttpStatusCode, sNewLocation.c_str());
				}

				if(sNewLocation.find(L"://") == std::wstring::npos)
				{
					INTERNET_SCHEME nOldInternetScheme = 0;
					std::wstring sOldHostName;
					INTERNET_PORT nOldPort = 0;
					std::wstring sOldUrlPath;
					const bool bRes2 = HttpSession::CrackUrl(sOldLocation,
						nOldInternetScheme, sOldHostName, nOldPort, sOldUrlPath);
					if(!bRes2)
					{
						MYTRACE("CrackUrl failed!, URL = %ls\n", sOldLocation.c_str());
						return false;
					}

					if(sNewLocation == L"")
					{
						MYTRACE("WARNING! EMPTY REDIRECT LOCATION!\n");
						//CUSTOM_WAIT("EMPTY REDIRECT LOCATION!");
						sNewLocation = sOldUrlPath;
					}
					MYASSERT(!sNewLocation.empty());
					MYASSERT(sNewLocation[0] == L'/');

					sNewLocation = ConstructUrl(nOldInternetScheme, sOldHostName, nOldPort, sNewLocation);

					if(bGenerateOutput)
					{
						MYTRACE("HTTP status %d: REAL redirect to %ls\n", redirResults.dwHttpStatusCode, sNewLocation.c_str());
					}
				}

				redirInfo.sVerb = VERB_GET;
				redirInfo.sWholeUrl = sNewLocation;
				redirInfo.sBinaryRequestBody.clear();
				redirInfo.sContentType = NULL_HTTP_STRINGW;

				continue;
			}
			else
			{
				MYTRACE("WARNING! HTTP status %d: redirection IS SKIPPED to \"%ls\" because redirection maximum count reached!\n",
					redirResults.dwHttpStatusCode, redirResults.sLocation.c_str());
				//MYASSERT(FALSE && "HTTP redirection IS SKIPPED because redirection maximum count reached!");

				globalResults.bTooLongRedirectError = true;
				return false;
			}
		}
#endif // ifdef MANUAL_REDIRECT
		break; // loop continues above by 'continue'!
	}

	return true;
}

//------------------------------------------------------

bool HttpSession::QueryTextOption(OUT std::wstring &sOptionText, IN const DWORD dwOption)
{
	PROFILER;
	return WinHttpQueryTextOption(m_hSession, dwOption, sOptionText);
}

//------------------------------------------------------

void HttpSession::SetCookie(const HttpCookie& cookie)
{
	PROFILER;
	m_Cookies2.SetCookie(cookie);
	m_Cookies2.ClearOutdatedCookies(CurrentTimeAsTimeT_GMT());

// 	{
// 		SafeCSLock lock(m_Cookies);
// 
// 		const std::wstring& sName = cookie.m_sName;
// 		const std::wstring& sValue = cookie.m_sRawHttpValue;
// 
// 		// TODO: This is not a good code. Some sites set such a value and expire cookie at the same time:
// 		if(AreStringsEqualNoCase(sValue, L"deleted"))
// 		{
// 			//MYTRACE("DBG: Removing cookie \"%ls\"...\n", sName.c_str());
// 			m_Cookies.erase(sName);
// 			return;
// 		}
// 		//MYTRACE("DBG: SetSessionCookie: '%ls'='%ls'\n", sName.c_str(), sValue.c_str());
// 		m_Cookies[sName] = sValue;
// 	}
}

void HttpSession::EmptyCookies(const std::string& sDomain)
{
	PROFILER;
	if(sDomain.empty())
	{
		m_Cookies2.ClearAllCookies();
	}
	else
	{
		m_Cookies2.ClearDomainCookies(Utf8ToUnicode(sDomain));
	}

// 	{
// 		SafeCSLock lock(m_Cookies);
// 		m_Cookies.clear();
// 	}
}

//------------------------------------------------------

void HttpSession::GetCookies(HttpCookieNameValueList& cookies, const bool bHttps, const std::wstring& sDomain, const std::wstring& sPath)
{
	PROFILER;
#if 0
	{
		cookies.clear();
		SafeCSLock lock(m_Cookies);
		for(CookieMap::const_iterator iter = m_Cookies.begin(); iter != m_Cookies.end(); iter++)
		{
			cookies[iter->first] = iter->second;
		}
	}
#else
	{
		HttpCookiePathVector pathCookies;
		m_Cookies2.GetDomainPathCookies(CurrentTimeAsTimeT_GMT(), bHttps, sDomain, sPath, pathCookies);
		cookies = pathCookies.GetNameValueList();
	}
#endif
}

//------------------------------------------------------

std::wstring HttpSession::GetCookiesHeader(const bool bHttps, const std::wstring& sDomain, const std::wstring& sPath)
{
	PROFILER;
// 	std::wstring sCookies1;
	std::wstring sCookies2;
// 	{
// 		HttpCookieNameValueList m;
// 		SafeCSLock lock(m_Cookies);
// 		for(CookieMap::const_iterator iter = m_Cookies.begin(); iter != m_Cookies.end(); iter++)
// 		{
// 			m[iter->first] = iter->second;
// 		}
// 		sCookies1 = m.FormatHttpCookieString();
// 	}
	{
		HttpCookiePathVector pathCookies;
		m_Cookies2.GetDomainPathCookies(CurrentTimeAsTimeT_GMT(), bHttps, sDomain, sPath, pathCookies);
		const HttpCookieNameValueList& cookies2 = pathCookies.GetNameValueList();
		sCookies2 = cookies2.FormatHttpCookieString();
	}
// 	if(sCookies1 != sCookies2)
// 	{
// 		MYTRACE("Warning! Cookie! Different cookie engines return different cookies!\n");	// TODO: COOK
// 		MYTRACE("Cookie engine #1: %ls\n", sCookies1.c_str());
// 		MYTRACE("Cookie engine #2: %ls\n", sCookies2.c_str());
// 		MYDUMP("Different cookie engines return different cookies!");
// 		const DWORD dwSleepMs = 15*60*1000;
// 		MYTRACE("Sleeping %d sec...\n", (dwSleepMs)/1000);
// 		Sleep(dwSleepMs);
// 	}
// 	return sCookies1;
	return sCookies2;
}

//------------------------------------------------------

bool MyWinHttpTimeToSystemTime(const std::wstring& sTimeStamp1, LPSYSTEMTIME pst)
{
	PROFILER;
	if(pst == NULL)
		return false;
	memset(pst, 0, sizeof(SYSTEMTIME));
	if(sTimeStamp1.empty())
		return true;

	std::wstring sTimeStamp2 = sTimeStamp1;

	INT64 nMinutesFromGMT = 0;
	const size_t pos = sTimeStamp2.find_last_of(' ');
	if(pos != std::wstring::npos && pos >= 22) // 22 - is a magic to filter "Fri Sep  4 16:01:05 2009" (no timezone)
	{
		nMinutesFromGMT = ParseTimeZone(ToUpper(UnicodeToUtf8(sTimeStamp2.c_str() + pos + 1)));
		sTimeStamp2 = sTimeStamp1.substr(0, pos+1) + L"GMT"; // replace decoded timezone with GMT
	}

	const bool bOK = !!WinHttpTimeToSystemTime(sTimeStamp2.c_str(), pst);
	if(!bOK)
		return false;

	if(pst->wYear >= 100 && pst->wYear < 200)
		pst->wYear += 1900; // correct "Fri, 09 Nov 102 16:14:55 UT" (illegal year)

	// Revert timezone to GMT:
	{
		LARGE_INTEGER li = ST2LI(*pst);
		li.QuadPart -= GetFileTimeSecondTicks() * 60I64 * nMinutesFromGMT;
		*pst = LI2ST(li);
	}

	return bOK;
}

//------------------------------------------------------
//------------------------------------------------------
