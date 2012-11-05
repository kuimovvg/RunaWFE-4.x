// (c) Sergey Kolomenkin, sergey.kolomenkin (at server) gmail.com, 2008-2012
//------------------------------------------------------
#pragma once

#include "..\..\common\SafeCriticalSection.h"
#include <string>
#include <map>
#include <list>
#include <vector>
#include <memory.h>	// for memset()

#ifdef UNITTEST_DEPENDENCY
#include "..\..\common\Modules.h"
LINK_TO_MODULE(WinHttpCookiesSuite)
#endif //ifdef UNITTEST_DEPENDENCY

//------------------------------------------------------

struct HttpSetCookieInfo
{
	HttpSetCookieInfo()
	{
		m_bSecure = false;
		m_bHttpOnly = false;
	}

	struct NameValue
	{
		std::wstring	m_sName;
		std::wstring	m_sValue;	// HTTP-encoded cookie value ("val1 '\"\t\r" encoded as "val1+%27%22%09%0D")
	};
	std::vector<NameValue>	m_vectCookies;

	std::wstring	m_sExpires;
	std::wstring	m_sDomain;
	std::wstring	m_sPath;
	bool			m_bSecure;		// to be used only in HTTPS
	bool			m_bHttpOnly;	// NOT to be accessible through JavaScript, etc.
};

bool CrackSetCookieString(LPCWSTR szCookie, HttpSetCookieInfo& info);

//------------------------------------------------------

struct HttpCookie
{
	HttpCookie()
	{
		m_bSecure = false;
		m_bHttpOnly = false;
		m_nCreatedGMT = 0;
		m_nUpdatedGMT = 0;
		m_nExpiresGMT = 0;
		EmptySystemTime(m_stCreated);
		EmptySystemTime(m_stUpdated);
		EmptySystemTime(m_stExpires);
	}

	HttpCookie(const std::wstring& sName, const std::wstring& sValue, const bool bRawHttpValue,
		const std::wstring& sDomain, const SYSTEMTIME& stNow); // local time

	std::wstring	m_sName;
	std::wstring	m_sRawHttpValue;	// HTTP-encoded cookie value ("val1 '\"\t\r" encoded as "val1+%27%22%09%0D")
	std::wstring	m_sDecodedValue;	// value text

	std::wstring	m_sExpires;
	std::wstring	m_sDomain;
	std::wstring	m_sPath;
	bool			m_bSecure;		// to be used only in HTTPS
	bool			m_bHttpOnly;	// NOT to be accessible through JavaScript, etc.

	enum
	{
		EXPIRE_NEVER	= 0x7FFDFFFF,	// Unix Time, ~24 hours before MAX_INT
		EXPIRE_SESSION	= 0x7FFDFFFE,	// Unix Time, ~24 hours before MAX_INT
	};

	time_t			m_nCreatedGMT;	// creation time
	time_t			m_nUpdatedGMT;	// last write time
	time_t			m_nExpiresGMT;	// GMT(UTC), parsed from m_sExpires, Unix Time, seconds elapsed since midnight, January 1, 1970

	SYSTEMTIME		m_stCreated;	// local time
	SYSTEMTIME		m_stUpdated;	// local time, last write time
	SYSTEMTIME		m_stExpires;	// local time, made from m_sExpires

	std::wstring Dump() const;	// for Debug purposes

	static void EmptySystemTime(SYSTEMTIME& st);

	inline bool operator< (const HttpCookie& b) const
	{
		return m_nUpdatedGMT < b.m_nUpdatedGMT;
	}
};

//------------------------------------------------------

struct HttpCookiePath: public std::vector<HttpCookie> // sorted by cookie last update time (from oldest to newest)
{
	iterator GetCookie(const std::wstring& sCookieName); // ignore case
};

struct HttpCookieDomain: public std::map<std::wstring, HttpCookiePath> // map key is domain Path
{
};

struct HttpCookieContainer: public std::map<std::wstring, HttpCookieDomain> // map key is ToLower(domain)
{
};

//------------------------------------------------------

struct HttpCookieNameValueList: public std::list<std::pair<std::wstring, std::wstring> > // list of pairs(cookie_name, value)
{
	std::wstring FormatHttpCookieString() const;
	bool Exists(const std::wstring& sCookieName) const; // ignore case
	std::wstring GetCookieValue(const std::wstring& sCookieName) const; // ignore case
};

struct HttpCookiePathVector: public std::vector<HttpCookie> // sorted by cookie last update time (from oldest to newest)
{
	HttpCookieNameValueList GetNameValueList() const;
};

//------------------------------------------------------

typedef std::vector<HttpCookie> HttpCookieVector;

void PreProcessCookies(const HttpSetCookieInfo& info,
					   const SYSTEMTIME& stNow,		// local time
					   HttpCookieVector& cookies);

std::wstring DumpHttpCookies(const HttpCookieVector& cookies);	// for Debug purposes

//------------------------------------------------------

class WinHttpCookies
{
public:
	WinHttpCookies(void);
	~WinHttpCookies(void);

	void ClearAllCookies();
	void ClearDomainCookies(const std::wstring& sDomain); // with subdomains. Example: "google.com"

	void ClearOutdatedCookies(const time_t nCurrentUnixTimeGMT);	// GMT

	inline time_t GetSecondsSinceLastFlush(const time_t nCurrentUnixTimeGMT) const	// GMT
	{
		return nCurrentUnixTimeGMT - m_nLastFlushUnixTimeGMT;
	}

	static bool CheckSetCookiePolicies(
		const std::wstring& sCookieDomain, const std::wstring& sCookiePath,
		const std::wstring& sRequestDomain, const std::wstring& sRequestPath);

	void SetCookie(const HttpCookie& cookie);

	void GetAllCookies(HttpCookieVector& cookiesResult) const;

	void GetDomainPathCookies(
		const time_t nCurrentUnixTime, const bool bHttps,
		const std::wstring& sRequestDomain, const std::wstring& sRequestPath,
		HttpCookiePathVector& cookiesResult) const;

protected:
	SafeCriticalSection		m_protect;
	HttpCookieContainer		m_cookies;
	time_t					m_nLastFlushUnixTimeGMT;	// GMT
};

//------------------------------------------------------
