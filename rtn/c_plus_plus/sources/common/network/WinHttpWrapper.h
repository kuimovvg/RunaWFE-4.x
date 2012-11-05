// WinHttpWrapper.h: interface for the WinHttpWrapper class.
// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------

#if !defined(AFX_WINHTTPWRAPPER_H__3A9F74C0_A26D_4412_A5CE_4D68AAA82BAD__INCLUDED_)
#define AFX_WINHTTPWRAPPER_H__3A9F74C0_A26D_4412_A5CE_4D68AAA82BAD__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include "..\..\common\ComBase.h"
#include <WinHttp.h>
#include <atlbase.h>

#include <string>
#include <vector>

#include "WinHttpCookies.h"

#include "..\..\common\compiler.h"

#ifdef UNITTEST_DEPENDENCY
#include "..\..\common\Modules.h"
LINK_TO_MODULE(WinHttpWrapper)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

#define VERB_GET	L"GET"
#define VERB_POST	L"POST"

//------------------------------------------------------

#define NULL_HTTP_STRINGW	std::wstring(1, L'\0')

inline bool IsHttpNullString(const std::wstring& s)
{
	if(s.size() != 1)
		return false;
	return s[0] == L'\0';
}

//------------------------------------------------------

class HttpConnection;
class HttpSession;

typedef CComPtr<IUnknown> UnknownPtr;

//------------------------------------------------------

class HttpRequest: public CComBase
{
	friend class HttpConnection;
protected:
	HttpRequest(HttpConnection* pConnection, HINTERNET hRequest);
	MYVIRTUAL ~HttpRequest();

protected:
	bool SendRequest(
		IN const std::vector<std::wstring>& vectAdditionalHeaders,
		IN const std::string& sBinaryRequestBody
		);

	bool GetResponse(std::string& sBinaryResponce);

public:
	bool MakeRequest(
		OUT std::string& sBinaryResponce,
		IN const std::vector<std::wstring>& vectAdditionalHeaders = std::vector<std::wstring>(),
		IN const std::string& sBinaryRequestBody = ""
		);

	bool AddOrReplaceHttpHeaders(const std::wstring& sHeaders);
	bool AddIfNewHttpHeaders(const std::wstring& sHeaders);

public:
	bool QueryTextHeaders(OUT std::wstring& sHeaders, IN const DWORD dwInfoLevel,
		IN OUT LPDWORD lpdwHeaderIndex = WINHTTP_NO_HEADER_INDEX);

	bool QueryTextOption(OUT std::wstring &sOptionText, IN const DWORD dwOption);

public:
	DWORD GetRequestHttpStatus();
	static bool IsRedirectStatus(const DWORD dwHttpStatus);

	void GetSavedRequestTextHeaders(OUT std::wstring& sOutputString) const;
	inline std::wstring GetSavedCookies() const
	{
		return m_sCookies;
	}

protected:
	bool ProcessCookiesHeaders();

protected:
	HINTERNET		m_hRequest;
	HttpConnection*	m_pConnection;
	UnknownPtr		m_ptrParent;	// this smart pointer is a duplication of previous pointer

	// data saved during SendRequest(inside of MakeRequest) for using later:
	std::vector<std::wstring>	m_vectRequestHeaders;
	std::wstring				m_sCookies;	// this is also saved in m_vectRequestHeaders
};

typedef CComPtr<HttpRequest> HttpRequestPtr;

//------------------------------------------------------

class HttpConnection: public CComBase
{
	friend class HttpSession;
	friend class HttpRequest;
protected:
	HttpConnection(HttpSession* pSession, HINTERNET hConnection);
	MYVIRTUAL ~HttpConnection();

public:

	HttpRequestPtr OpenRequest(
		IN const std::wstring&	sVerb,	// VERB_GET, VERB_POST, etc.
		IN const std::wstring&	sObjectName,
		IN const DWORD dwFlags = 0
		);

protected:
	HINTERNET		m_hConnection;
	HttpSession*	m_pSession;
	UnknownPtr		m_ptrParent;	// this smart pointer is a duplication of previous pointer
};

typedef CComPtr<HttpConnection> HttpConnectionPtr;

//------------------------------------------------------

struct REQUEST_INFO
{
	inline REQUEST_INFO()
	{
		sContentType = NULL_HTTP_STRINGW;
		sReferer = NULL_HTTP_STRINGW;
	}
	inline REQUEST_INFO(const std::wstring& Verb, const std::wstring& WholeUrl)
	{
		sVerb = Verb;
		sWholeUrl = WholeUrl;
		sContentType = NULL_HTTP_STRINGW;
		sReferer = NULL_HTTP_STRINGW;
	}

	std::wstring		sVerb;	// VERB_GET, VERB_POST, etc.
	std::wstring		sWholeUrl;
	std::string			sBinaryRequestBody;
	std::wstring		sContentType;	// NULL_HTTP_STRINGW, FORM_ENC_URLENCODED, FORM_ENC_FORMDATA
	std::wstring		sReferer;		// Can be NULL_HTTP_STRINGW!
};

//------------------------------------------------------

struct REQUEST_RESULT
{
	inline REQUEST_RESULT()
	{
		dwHttpStatusCode = 0;
		dwOriginalReplyBodySize = 0;
	}

	// Data for HTTP server:
	std::wstring	sProxy;
	std::wstring	sProxyBypass;
	std::string		sContextProxyDesc;

	REQUEST_INFO	request;			// data for request to server
	std::wstring	sRequestHeaders;	// data sent to server
	std::wstring	sCookies;			// part of sRequestHeaders

	// Data from HTTP server:
	std::wstring	sReplyHeaders;

	DWORD			dwHttpStatusCode;	// extracted from sReplyHeaders, 200 - OK, 302 - Moved, 404 - Not found, ...
	std::wstring	sServer;			// extracted from sReplyHeaders, example: Apache 1.0
	std::wstring	sLocation;			// extracted from sReplyHeaders
	std::wstring	sContentType;		// extracted from sReplyHeaders
	std::wstring	sContentEncoding;	// extracted from sReplyHeaders (gzip, ...)
	std::wstring	sTransferEncoding;	// extracted from sReplyHeaders (chunked, ...)
	std::string		sBinaryReplyBody;
	DWORD			dwOriginalReplyBodySize;	// sBinaryReplyBody size before decompression

	std::wstring GetReplyHeader(const std::wstring& sHeaderName, const std::wstring& sDefaultValue = L"") const;
};

//------------------------------------------------------

struct REQUEST_RESULTS
{
	inline REQUEST_RESULTS()
	{
		bTooLongRedirectError = false;
	}

	std::vector<REQUEST_RESULT>		vectRedirects;
	bool							bTooLongRedirectError;

	const REQUEST_RESULT& GetFirstResult() const;
	const REQUEST_RESULT& GetLastResult() const;
};

//------------------------------------------------------

class HttpSession: public CComBase
{
protected:
	HttpSession(HINTERNET hSession);
	MYVIRTUAL ~HttpSession();

public:

#define DEFAULT_USER_AGENT_SZ	L"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; InfoPath.2)"
//#define DEFAULT_USER_AGENT_SZ	L"WinHTTP Browser/1.0"

	static CComPtr<HttpSession> Create(
		IN DWORD				dwAccessType = WINHTTP_ACCESS_TYPE_DEFAULT_PROXY,
		IN const std::wstring&	sProxyName = L"",
		IN const std::wstring&	sProxyBypass = L"",
		IN const std::wstring&	sUserAgent = DEFAULT_USER_AGENT_SZ
		);

	static inline CComPtr<HttpSession> Create(
		IN const std::wstring&	sProxyName,
		IN const std::wstring&	sProxyBypass = L""
		)
	{
		if(sProxyName.empty())
		{
			return Create();
		}
		CComPtr<HttpSession> ptr = Create(
			WINHTTP_ACCESS_TYPE_NAMED_PROXY,
			sProxyName, sProxyBypass,
			DEFAULT_USER_AGENT_SZ);
		return ptr;
	}

	bool SetProxyInformation(
		IN const std::wstring&	sProxyName = L"",
		IN const std::wstring&	sProxyBypass = L""
		);

	bool GetProxyInformation(
		OUT std::wstring&	sProxyName,
		OUT std::wstring&	sProxyBypass
		);

	bool SetUserAgent(
		IN const std::wstring&	sUserAgent = DEFAULT_USER_AGENT_SZ
		);

	HttpConnectionPtr Connect(
		IN const std::wstring&	sServerName,
		IN const INTERNET_PORT	nServerPort = INTERNET_DEFAULT_PORT
		);

	static bool CrackUrl(
		IN const std::wstring&	sWholeUrl,
		OUT INTERNET_SCHEME& nInternetScheme,
		OUT std::wstring&	sHostName,
		OUT INTERNET_PORT&	nPort,
		OUT std::wstring&	sUrlPath);

	bool Connect(
		IN const std::wstring&	sVerb,	// VERB_GET, VERB_POST, etc.
		IN const std::wstring&	sWholeUrl,
		OUT HttpConnectionPtr&	ptrConnection,
		OUT HttpRequestPtr&		ptrRequest
		);

	bool DownloadFromUrl(
		IN const REQUEST_INFO&	request,
		OUT REQUEST_RESULTS&	results,
		IN bool					bGenerateOutput = true,
		IN const int			nMaxRedirects = 10
		);

	inline bool DownloadFromUrl(
		IN const std::wstring&	sVerb,	// VERB_GET, VERB_POST, etc.
		IN const std::wstring&	sWholeUrl,
		OUT std::string&		sBinaryResponce,
		IN bool					bGenerateOutput = true,
		IN const int			nMaxRedirects = 10
		)
	{
		REQUEST_INFO request(sVerb, sWholeUrl);
		REQUEST_RESULTS results;
		const bool bRes = DownloadFromUrl(request, results, bGenerateOutput, nMaxRedirects);
		const REQUEST_RESULT& res = results.GetLastResult();
		sBinaryResponce = res.sBinaryReplyBody;
		return bRes;
	}

	bool QueryTextOption(OUT std::wstring &sOptionText, IN const DWORD dwOption);

public:
	void SetCookie(const HttpCookie& cookie);
	void GetCookies(HttpCookieNameValueList& cookies, const bool bHttps, const std::wstring& sDomain, const std::wstring& sPath);
	std::wstring GetCookiesHeader(const bool bHttps, const std::wstring& sDomain, const std::wstring& sPath);
	void EmptyCookies(const std::string& sDomain);

	inline void ClearBrowserHeaders()
	{
		m_vectBrowserHeaders.clear();
	}

	inline void AddBrowserHeader(const std::wstring& sHeader)
	{
		m_vectBrowserHeaders.push_back(sHeader);
	}

	inline const std::vector<std::wstring>& GetBrowserHeaders() const
	{
		return m_vectBrowserHeaders;
	}

	inline bool GetCheckServerSSLCertificateState() const
	{
		return m_bCheckServerSSLCertificate;
	}

	inline void SetCheckServerSSLCertificateState(const bool bCheckCertificates)
	{
		m_bCheckServerSSLCertificate = bCheckCertificates;
	}

protected:
	static void CALLBACK HttpSession::StatusCallback(
		HINTERNET hInternet,
		DWORD_PTR dwContext,
		DWORD dwInternetStatus,
		LPVOID lpvStatusInformation,
		DWORD dwStatusInformationLength);

protected:
// 	class CookieMap: public std::map<std::wstring, std::wstring>, public SafeCriticalSection
// 	{
// 	};
// 	CookieMap		m_Cookies;	// maps cookie name into HTTP-encoded cookie value ("val1 '\"\t\r" encoded as "val1+%27%22%09%0D")
	WinHttpCookies	m_Cookies2;

	std::vector<std::wstring>	m_vectBrowserHeaders;	// if empty, then defaults are used. Usually: Accept, Accept-language, Accept-Encoding, Accept-Charset
	bool			m_bCheckServerSSLCertificate;

protected:
	HINTERNET		m_hSession;
};

typedef CComPtr<HttpSession> HttpSessionPtr;

//------------------------------------------------------

#endif // !defined(AFX_WINHTTPWRAPPER_H__3A9F74C0_A26D_4412_A5CE_4D68AAA82BAD__INCLUDED_)
